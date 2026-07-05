import { Terminal } from '@xterm/xterm';

export function createTerminal(): Terminal {
    return new Terminal({
        convertEol: true,
        cursorBlink: true,
        fontSize: 14,
        theme: {
            background: '#171717',
        },
    });
}
