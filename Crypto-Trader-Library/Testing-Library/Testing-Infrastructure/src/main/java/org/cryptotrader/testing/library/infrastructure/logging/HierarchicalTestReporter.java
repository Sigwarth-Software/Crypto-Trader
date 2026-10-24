package org.cryptotrader.testing.library.infrastructure.logging;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import picocli.CommandLine.Help.Ansi;

/** Coordinates native JUnit reporting. Maven renders its own Surefire/Failsafe trees. */
public final class HierarchicalTestReporter implements TestExecutionListener {
    private static final AtomicBoolean ACTIVE = new AtomicBoolean();

    private final TestExecutionReport report = new TestExecutionReport();
    private final InheritableThreadLocal<String> currentTest = new InheritableThreadLocal<>();
    private final boolean enabled;
    private final PrintStream destination;
    private final TestReportRenderer renderer;
    private PrintStream originalOut;
    private PrintStream originalErr;
    private TestOutputCapture stdout;
    private TestOutputCapture stderr;
    private volatile boolean running;
    private boolean ownsActivation;

    /** Called automatically by the JUnit Platform service loader, including IntelliJ. */
    public HierarchicalTestReporter() {
        this(System.getProperty("surefire.test.class.path") == null
                && !Boolean.getBoolean("cryptotrader.test.console.disabled"), null);
    }

    HierarchicalTestReporter(final boolean enabled, final PrintStream destination) {
        this(enabled, destination, consoleAnsi(
                System.getProperty("cryptotrader.test.console.color", "auto"),
                System.getenv().containsKey("NO_COLOR"), System.getProperty("sun.java.command", ""),
                Ansi.AUTO.enabled()), Long.getLong("cryptotrader.test.slowThresholdMillis", 500L));
    }

    HierarchicalTestReporter(final boolean enabled, final PrintStream destination,
                             final Ansi ansi, final long slowThresholdMillis) {
        this.enabled = enabled;
        this.destination = destination;
        this.renderer = new TestReportRenderer(ansi, slowThresholdMillis);
    }

    // IntelliJ supports ANSI even though its redirected console has no System.console().
    static Ansi consoleAnsi(final String mode, final boolean noColor,
                            final String command, final boolean terminalSupportsAnsi) {
        return switch (mode.toLowerCase(Locale.ROOT)) {
            case "always" -> Ansi.ON;
            case "never" -> Ansi.OFF;
            default -> Ansi.valueOf(!noColor && (command.startsWith("com.intellij.rt.junit.JUnitStarter")
                    || terminalSupportsAnsi));
        };
    }

    @Override
    public synchronized void testPlanExecutionStarted(final TestPlan plan) {
        if (!this.enabled || !this.acquireReporting()) {
            return;
        }
        this.report.clear();
        plan.getRoots().forEach(root -> this.remember(plan, root));
        this.captureConsole();
        this.running = true;
    }

    private boolean acquireReporting() {
        if (this.destination != null) {
            return true;
        }
        if (!ACTIVE.compareAndSet(false, true)) {
            return false;
        }
        this.ownsActivation = true;
        return true;
    }

    private void remember(final TestPlan plan, final TestIdentifier id) {
        this.report.register(id);
        plan.getChildren(id).forEach(child -> this.remember(plan, child));
    }

    private void captureConsole() {
        this.originalOut = System.out;
        this.originalErr = System.err;
        this.stdout = new TestOutputCapture(this.originalOut, this::captureStdout, this.currentTest::get);
        this.stderr = new TestOutputCapture(this.originalErr, this::captureStderr, this.currentTest::get);
        System.setOut(utf8PrintStream(this.stdout));
        System.setErr(utf8PrintStream(this.stderr));
    }

    private static PrintStream utf8PrintStream(final OutputStream stream) {
        try {
            return new PrintStream(stream, true, StandardCharsets.UTF_8.name());
        } catch (final UnsupportedEncodingException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    private synchronized void captureStdout(final String owner, final String text) {
        this.report.capture(owner, text, false);
    }

    private synchronized void captureStderr(final String owner, final String text) {
        this.report.capture(owner, text, true);
    }

    @Override
    public synchronized void dynamicTestRegistered(final TestIdentifier id) {
        if (this.running) {
            this.report.register(id);
        }
    }

    @Override
    public synchronized void executionStarted(final TestIdentifier id) {
        if (this.running) {
            this.report.started(id);
            this.currentTest.set(id.getUniqueId());
        }
    }

    @Override
    public synchronized void executionSkipped(final TestIdentifier id, final String reason) {
        if (this.running) {
            this.report.skipped(id, reason);
        }
    }

    @Override
    public synchronized void executionFinished(final TestIdentifier id, final TestExecutionResult result) {
        if (this.running) {
            this.report.finished(id, result);
            id.getParentId().ifPresentOrElse(this.currentTest::set, this.currentTest::remove);
        }
    }

    @Override
    public void testPlanExecutionFinished(final TestPlan plan) {
        if (!this.running) {
            return;
        }
        // Drain outside the listener lock: application threads may still be writing.
        this.stdout.drain();
        this.stderr.drain();
        synchronized (this) {
            this.running = false;
            System.setOut(this.originalOut);
            System.setErr(this.originalErr);
            this.currentTest.remove();
            try {
                this.renderer.render(this.report, this.destination == null ? this.originalOut : this.destination);
            } finally {
                this.report.clear();
                if (this.ownsActivation) {
                    ACTIVE.set(false);
                    this.ownsActivation = false;
                }
            }
        }
    }
}
