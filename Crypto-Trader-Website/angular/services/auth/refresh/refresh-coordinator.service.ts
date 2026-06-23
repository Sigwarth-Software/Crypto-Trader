import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';





/**
 * Cross-tab refresh coordinator using BroadcastChannel (when available).
 *
 * Goal: ensure only one browser tab performs POST /auth/refresh at a time.
 * Other tabs wait for the resulting access token to be broadcast instead of
 * calling refresh concurrently, which could otherwise cause refresh token
 * reuse and a family revocation.
 */
@Injectable({
    providedIn: 'root',
})
export class RefreshCoordinatorService {
    private readonly channel: BroadcastChannel | null;
    private readonly lockKey: string = 'ct-auth-refresh-lock';
    private readonly lockTtlMs: number = 15000;

    constructor() {
        // BroadcastChannel is widely supported in modern browsers; fall back to
        // no-op (single-tab behavior) if unavailable.
        try {
            this.channel = new BroadcastChannel('ct-auth-refresh');
        } catch {
            this.channel = null;
        }
    }

    /**
     * Execute the given refresh task in a cross-tab single-flight manner.
     * - If BroadcastChannel is unavailable, just executes the task.
     * - If available, elects a leader tab to execute; followers wait for the
     *   leader to broadcast the new token.
     * @param execute
     * @returns Observable that emits the new access token upon completion.
     *          If the leader tab fails to broadcast the token, the observable
     *          errors with an error indicating the failure.
     */
    public singleFlight(execute: () => Observable<string>): Observable<string> {
        if (!this.channel) {
            return execute();
        }
        return new Observable<string>((subscriber): (() => void) => {
            const id = `${Date.now()}:${Math.random().toString(16).slice(2)}`;

            const cleanup = (): void => {
                // Release lock if we still own it
                try {
                    const current = localStorage.getItem(this.lockKey);
                    if (current === id) {
                        localStorage.removeItem(this.lockKey);
                    }
                } catch {
                    /* ignore */
                }
                // Detach listener
                try {
                    if (this.channel !== null) {
                        this.channel.onmessage = null as any;
                    }
                } catch {
                    /* ignore */
                }
            };

            const onMessage = (event: MessageEvent): void => {
                const data: any = event?.data ?? {};
                if (data?.type === 'token' && typeof data?.token === 'string') {
                    // eslint-disable-next-line @typescript-eslint/no-unsafe-argument
                    subscriber.next(data.token);
                    subscriber.complete();
                    cleanup();
                } else if (data?.type === 'failed') {
                    subscriber.error(
                        new Error('Refresh failed in another tab'),
                    );
                    cleanup();
                }
            };
            if (this.channel !== null) {
                this.channel.onmessage = onMessage;
            }

            const becomeLeader = (): boolean => {
                try {
                    const current = localStorage.getItem(this.lockKey);
                    if (this.isStaleLock(current)) {
                        localStorage.removeItem(this.lockKey);
                    }
                    const available = localStorage.getItem(this.lockKey);
                    if (!available) {
                        localStorage.setItem(this.lockKey, id);
                        return true;
                    }
                } catch {
                    /* ignore */
                }
                return false;
            };

            // Attempt leadership
            if (becomeLeader()) {
                // Leader: perform the task, broadcast results
                const subscription = execute().subscribe({
                    next: (token): void => {
                        try {
                            if (this.channel !== null) {
                                this.channel.postMessage({
                                    type: 'token',
                                    token,
                                });
                            }
                        } catch {
                            /* ignore */
                        }
                        subscriber.next(token);
                        subscriber.complete();
                        cleanup();
                    },
                    error: (err): void => {
                        try {
                            if (this.channel !== null) {
                                this.channel.postMessage({
                                    type: 'failed',
                                });
                            }
                        } catch {
                            /* ignore */
                        }
                        subscriber.error(err);
                        cleanup();
                    },
                });
                // Teardown
                return (): void => {
                    try {
                        subscription.unsubscribe();
                    } catch {
                        /* ignore */
                    }
                    cleanup();
                };
            }

            // Follower: wait for broadcast or time out
            const timeout = setTimeout((): void => {
                subscriber.error(
                    new Error('Refresh timed out waiting for leader tab'),
                );
                cleanup();
            }, 10000);

            // Teardown function
            return (): void => {
                clearTimeout(timeout);
                cleanup();
            };
        });
    }

    private isStaleLock(lockValue: string | null): boolean {
        if (!lockValue) {
            return false;
        }
        const [startedAt] = lockValue.split(':');
        const startedAtMs = Number(startedAt);
        return !Number.isFinite(startedAtMs) || Date.now() - startedAtMs > this.lockTtlMs;
    }
}
