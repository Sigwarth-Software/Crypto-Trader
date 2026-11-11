package org.cryptotrader.testing.library.infrastructure.logging;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/** Buffers test output while passing IntelliJ/TeamCity service messages through unchanged. */
final class TestOutputCapture extends OutputStream {
    private static final String IDE_PREFIX = "##teamcity[";
    private final PrintStream original;
    private final BiConsumer<String, String> capture;
    private final Supplier<String> owner;
    private final Map<Thread, Line> lines = new LinkedHashMap<>();
    private boolean drained;

    TestOutputCapture(final PrintStream original, final BiConsumer<String, String> capture,
                      final Supplier<String> owner) {
        this.original = original;
        this.capture = capture;
        this.owner = owner;
    }

    @Override
    public synchronized void write(final int value) {
        if (this.drained) {
            this.original.write(value);
            return;
        }
        final Line line = this.lines.computeIfAbsent(Thread.currentThread(), ignored -> new Line(this.owner.get()));
        if (line.protocol) {
            this.original.write(value);
        } else {
            line.buffer.write(value);
        }
        if (value == '\n') {
            this.emit(line);
            this.lines.remove(Thread.currentThread());
        } else if (line.buffer.size() >= 65_536) {
            this.emit(line);
        }
    }

    @Override
    public synchronized void write(final byte[] bytes, final int offset, final int length) {
        for (int i = offset; i < offset + length; i++) {
            this.write(bytes[i] & 0xff);
        }
    }

    private void emit(final Line line) {
        final String text = new String(line.buffer.toByteArray(), StandardCharsets.UTF_8);
        if (text.startsWith(IDE_PREFIX)) {
            this.original.write(line.buffer.toByteArray(), 0, line.buffer.size());
            line.protocol = !text.endsWith("\n");
        } else {
            this.capture.accept(line.owner, text);
        }
        line.buffer.reset();
    }

    @Override
    public synchronized void flush() {
        final Line line = this.lines.get(Thread.currentThread());
        if (line != null && new String(line.buffer.toByteArray(), StandardCharsets.UTF_8).startsWith(IDE_PREFIX)) {
            this.emit(line);
        }
        this.original.flush();
    }

    synchronized void drain() {
        this.lines.values().forEach(this::emit);
        this.lines.clear();
        this.drained = true;
        this.original.flush();
    }

    private static final class Line {
        private final String owner;
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        private boolean protocol;

        private Line(final String owner) {
            this.owner = owner;
        }
    }
}
