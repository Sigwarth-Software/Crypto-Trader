package org.cryptotrader.testing.library.infrastructure.logging;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestIdentifier;

/** Execution state and captured output, accessed under the listener's lock. */
final class TestExecutionReport {
    private static final int OUTPUT_LIMIT = 65_536;
    private final Map<String, Node> nodes = new LinkedHashMap<>();
    private final Map<String, List<Node>> children = new LinkedHashMap<>();
    private final StringBuilder background = new StringBuilder();

    Node register(final TestIdentifier id) {
        return this.nodes.computeIfAbsent(id.getUniqueId(), ignored -> {
            final Node node = new Node(id);
            this.children.computeIfAbsent(id.getParentId().orElse(""), key -> new ArrayList<>()).add(node);
            return node;
        });
    }

    void started(final TestIdentifier id) {
        this.register(id).started = System.nanoTime();
    }

    void skipped(final TestIdentifier id, final String reason) {
        this.register(id).skipReason = reason;
    }

    void finished(final TestIdentifier id, final TestExecutionResult result) {
        final Node node = this.register(id);
        node.finished = System.nanoTime();
        node.result = result;
    }

    void capture(final String owner, final String text, final boolean error) {
        final Node node = this.nodes.get(owner);
        if (node == null) {
            append(this.background, text);
        } else if (error) {
            append(node.stderr, text);
        } else {
            append(node.stdout, text);
        }
    }

    private static void append(final StringBuilder buffer, final String text) {
        if (buffer.length() > OUTPUT_LIMIT) {
            return;
        }
        final int remaining = OUTPUT_LIMIT - buffer.length();
        buffer.append(text, 0, Math.min(text.length(), remaining));
        if (text.length() > remaining) {
            buffer.append("\n[additional test output truncated]\n");
        }
    }

    List<Node> roots() {
        return this.children.getOrDefault("", List.of());
    }

    List<Node> children(final Node node) {
        return this.children.getOrDefault(node.id().getUniqueId(), List.of());
    }

    List<Node> failures() {
        return this.nodes.values().stream().filter(Node::failed).collect(Collectors.toList());
    }

    List<Node> ancestry(final Node node) {
        final List<Node> path = new ArrayList<>();
        for (Node ancestor = node; ancestor != null; ancestor = this.parent(ancestor)) {
            path.add(0, ancestor);
        }
        return path;
    }

    String path(final Node node) {
        final Node parent = this.parent(node);
        if (parent == null || parent.engine()) {
            return node.id().getDisplayName();
        }
        return this.path(parent) + " > " + node.id().getDisplayName();
    }

    private Node parent(final Node node) {
        return this.nodes.get(node.id().getParentId().orElse(""));
    }

    Counts counts(final Node node, final boolean ancestorSkipped) {
        final boolean skipped = node.skipped(ancestorSkipped);
        Counts counts = Counts.EMPTY;
        if (node.id().isTest()) {
            counts = switch (node.outcome(ancestorSkipped)) {
                case PASSED -> new Counts(1, 0, 0, 0);
                case FAILED -> new Counts(0, 1, 0, 0);
                case SKIPPED -> new Counts(0, 0, 1, 0);
                default -> Counts.EMPTY;
            };
        } else if (node.failed()) {
            counts = new Counts(0, 0, 0, 1);
        }
        for (Node child : this.children(node)) {
            counts = counts.plus(this.counts(child, skipped));
        }
        return counts;
    }

    String backgroundOutput() {
        return this.background.toString();
    }

    void clear() {
        this.nodes.clear();
        this.children.clear();
        this.background.setLength(0);
    }

    enum Outcome { PASSED, FAILED, SKIPPED, NOT_RUN, GROUP }

    record Counts(int passed, int failed, int skipped, int containers) {
        static final Counts EMPTY = new Counts(0, 0, 0, 0);

        Counts plus(final Counts other) {
            return new Counts(this.passed + other.passed, this.failed + other.failed,
                    this.skipped + other.skipped, this.containers + other.containers);
        }

        int total() {
            return this.passed + this.failed + this.skipped;
        }
    }

    static final class Node {
        private final TestIdentifier id;
        private final StringBuilder stdout = new StringBuilder();
        private final StringBuilder stderr = new StringBuilder();
        private long started;
        private long finished;
        private TestExecutionResult result;
        private String skipReason;

        private Node(final TestIdentifier id) {
            this.id = id;
        }

        TestIdentifier id() { return this.id; }
        String stdout() { return this.stdout.toString(); }
        String stderr() { return this.stderr.toString(); }

        boolean engine() {
            return this.id.getUniqueIdObject().getLastSegment().getType().equals("engine");
        }

        boolean failed() {
            return this.result != null && this.result.getStatus() == TestExecutionResult.Status.FAILED;
        }

        boolean skipped(final boolean ancestorSkipped) {
            return ancestorSkipped || this.skipReason != null
                    || this.result != null && this.result.getStatus() == TestExecutionResult.Status.ABORTED;
        }

        Outcome outcome(final boolean ancestorSkipped) {
            if (this.failed()) {
                return Outcome.FAILED;
            }
            if (this.skipped(ancestorSkipped)) {
                return Outcome.SKIPPED;
            }
            if (!this.id.isTest()) {
                return Outcome.GROUP;
            }
            return this.result == null ? Outcome.NOT_RUN : Outcome.PASSED;
        }

        long durationNanos() {
            return this.started == 0 ? 0 : Math.max(0, this.finished - this.started);
        }

        Optional<Throwable> failure() {
            return this.result == null ? Optional.empty() : this.result.getThrowable();
        }

        Optional<String> skipReason() {
            if (this.skipReason != null) {
                return Optional.of(this.skipReason);
            }
            if (this.result != null && this.result.getStatus() == TestExecutionResult.Status.ABORTED) {
                return Optional.of(this.failure().map(Throwable::getMessage).orElse("aborted"));
            }
            return Optional.empty();
        }
    }
}
