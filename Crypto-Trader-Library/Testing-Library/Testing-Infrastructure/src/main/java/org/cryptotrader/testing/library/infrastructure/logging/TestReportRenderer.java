package org.cryptotrader.testing.library.infrastructure.logging;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.cryptotrader.testing.library.infrastructure.logging.TestExecutionReport.Counts;
import org.cryptotrader.testing.library.infrastructure.logging.TestExecutionReport.Node;
import org.cryptotrader.testing.library.infrastructure.logging.TestExecutionReport.Outcome;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Help.Ansi.Style;

/** Formats a completed execution report without changing test state or console streams. */
final class TestReportRenderer {
    private static final int MAX_FAILURES = 50;
    private static final double NANOS_PER_SECOND = TimeUnit.SECONDS.toNanos(1);
    private final Ansi ansi;
    private final long slowThresholdNanos;

    TestReportRenderer(final Ansi ansi, final long slowThresholdMillis) {
        this.ansi = ansi;
        this.slowThresholdNanos = TimeUnit.MILLISECONDS.toNanos(Math.max(0, slowThresholdMillis));
    }

    void render(final TestExecutionReport report, final PrintStream out) {
        out.println("\n" + this.styled("Test results", Style.bold));
        final TreeWriter tree = new TreeWriter(report, out);
        Counts total = Counts.EMPTY;
        for (Node root : report.roots()) {
            total = total.plus(report.counts(root, false));
            tree.renderRoot(root);
        }
        this.renderSummary(out, total);
        this.renderFailures(report, out);
        out.println();
    }

    /** Keeps traversal context local to a single report. */
    private final class TreeWriter {
        private final TestExecutionReport report;
        private final PrintStream out;

        private TreeWriter(final TestExecutionReport report, final PrintStream out) {
            this.report = report;
            this.out = out;
        }

        private void renderRoot(final Node root) {
            if (root.engine() && !root.failed()) {
                this.report.children(root).forEach(this::renderRoot);
                return;
            }
            this.renderNode(root, TreePosition.ROOT, false);
        }

        private void renderNode(final Node node, final TreePosition position, final boolean ancestorSkipped) {
            this.out.println(this.formatNode(node, position, ancestorSkipped));
            this.renderChildren(node, position, node.skipped(ancestorSkipped));
        }

        private String formatNode(final Node node, final TreePosition position, final boolean ancestorSkipped) {
            final Outcome outcome = node.outcome(ancestorSkipped);
            final Style style = outcomeStyle(outcome, position.root());
            final String name = node.id().getDisplayName().replace('\n', ' ').replace('\r', ' ');
            final StringBuilder line = new StringBuilder(position.linePrefix());

            line.append(styled(statusLabel(outcome) + name, style));
            if (!node.id().isTest()) {
                final Counts counts = this.report.counts(node, ancestorSkipped);
                line.append(" (").append(groupLabel(counts)).append(')');
            }
            line.append(slowMarker(node, ancestorSkipped));
            line.append(formatDuration(node.durationNanos()));
            node.skipReason().ifPresent(reason -> line.append(" — ").append(reason));
            return line.toString();
        }

        private void renderChildren(final Node parent, final TreePosition position, final boolean parentSkipped) {
            final List<Node> children = this.report.children(parent);
            final int lastChildIndex = children.size() - 1;
            for (int index = 0; index < children.size(); index++) {
                final TreePosition childPosition = position.child(index == lastChildIndex);
                this.renderNode(children.get(index), childPosition, parentSkipped);
            }
        }
    }

    /** Prefixes for this line and its descendants, including ancestor continuation lines. */
    private record TreePosition(String linePrefix, String childrenPrefix, boolean root) {
        private static final TreePosition ROOT = new TreePosition("", "", true);

        private TreePosition child(final boolean lastChild) {
            final String branch = lastChild ? "└─ " : "├─ ";
            final String continuation = lastChild ? "   " : "│  ";
            return new TreePosition(this.childrenPrefix + branch, this.childrenPrefix + continuation, false);
        }
    }

    private String formatDuration(final long durationNanos) {
        final double seconds = durationNanos / NANOS_PER_SECOND;
        final String duration = String.format(Locale.ROOT, " - %.3f s", seconds);
        return this.styled(duration, Style.faint);
    }

    private String statusLabel(final Outcome outcome) {
        return switch (outcome) {
            case PASSED -> "[PASS] ";
            case FAILED -> "[FAIL] ";
            case SKIPPED -> "[SKIP] ";
            case NOT_RUN -> "[NOT RUN] ";
            case GROUP -> "";
        };
    }

    private Style outcomeStyle(final Outcome outcome, final boolean root) {
        return switch (outcome) {
            case PASSED -> Style.fg_green;
            case FAILED -> Style.fg_red;
            case SKIPPED -> Style.fg_yellow;
            case NOT_RUN -> Style.faint;
            case GROUP -> root ? Style.bold : Style.fg_cyan;
        };
    }

    private String slowMarker(final Node node, final boolean ancestorSkipped) {
        if (!node.id().isTest() || node.skipped(ancestorSkipped) || this.slowThresholdNanos == 0
                || node.durationNanos() < this.slowThresholdNanos) {
            return "";
        }
        return this.styled(" [SLOW]", Style.fg_yellow);
    }

    private void renderSummary(final PrintStream out, final Counts counts) {
        String summary = "\nTests: " + counts.total() + " | "
                + this.styled(counts.passed() + " passed", Style.fg_green) + " · "
                + this.styled(counts.failed() + " failed", Style.fg_red) + " · "
                + this.styled(counts.skipped() + " skipped", Style.fg_yellow);
        if (counts.containers() > 0) {
            summary += " · " + this.styled(this.containerLabel(counts), Style.fg_red);
        }
        out.println(summary);
    }

    private String groupLabel(final Counts counts) {
        final List<String> parts = new ArrayList<>();
        if (counts.passed() > 0) {
            parts.add(counts.passed() + " passed");
        }
        if (counts.failed() > 0) {
            parts.add(counts.failed() + " failed");
        }
        if (counts.skipped() > 0) {
            parts.add(counts.skipped() + " skipped");
        }
        if (counts.containers() > 0) {
            parts.add(this.containerLabel(counts));
        }
        return parts.isEmpty() ? "0 tests" : String.join(", ", parts);
    }

    private String containerLabel(final Counts counts) {
        return counts.containers() + (counts.containers() == 1 ? " container failed" : " containers failed");
    }

    private void renderFailures(final TestExecutionReport report, final PrintStream out) {
        final List<Node> failures = report.failures();
        if (failures.isEmpty()) {
            return;
        }
        out.println("\nFailed tests/containers (" + failures.size() + "):");
        final Set<String> printedOutput = new HashSet<>();
        for (Node failure : failures.subList(0, Math.min(failures.size(), MAX_FAILURES))) {
            out.println("\n" + this.styled("[FAIL] " + report.path(failure), Style.fg_red));
            failure.failure().ifPresent(cause -> cause.printStackTrace(out));
            this.renderFailureOutput(report, out, failure, printedOutput);
        }
        if (failures.size() > MAX_FAILURES) {
            out.println("… " + (failures.size() - MAX_FAILURES) + " additional failures");
        }
        this.printOutput(out, "Background output", report.backgroundOutput());
    }

    private void renderFailureOutput(final TestExecutionReport report, final PrintStream out,
                                     final Node failure, final Set<String> printedOutput) {
        for (Node ancestor : report.ancestry(failure)) {
            if (printedOutput.add(ancestor.id().getUniqueId())) {
                this.printOutput(out, "Standard out", ancestor.stdout());
                this.printOutput(out, "Standard error", ancestor.stderr());
            }
        }
    }

    private void printOutput(final PrintStream out, final String title, final String output) {
        if (!output.isEmpty()) {
            out.println(title + ":\n" + output);
        }
    }

    private String styled(final String text, final Style... styles) {
        return this.ansi.apply(text, List.of(styles)).toString();
    }
}
