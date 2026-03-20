package dev.rafex.ether.glowroot.jetty12;

import java.util.List;
import java.util.Objects;

import org.glowroot.agent.api.Glowroot;

import dev.rafex.ether.observability.core.probe.ProbeAggregator;
import dev.rafex.ether.observability.core.probe.ProbeCheck;
import dev.rafex.ether.observability.core.probe.ProbeKind;
import dev.rafex.ether.observability.core.probe.ProbeReport;

/**
 * Bridge utilities that record Ether probe reports into Glowroot transaction
 * attributes.
 */
public final class GlowrootProbeReporter {

    private GlowrootProbeReporter() {
    }

    public static ProbeReport capture(final ProbeKind kind, final List<ProbeCheck> checks) {
        final var report = ProbeAggregator.aggregate(kind, checks);
        record(report);
        return report;
    }

    public static void record(final ProbeReport report) {
        Objects.requireNonNull(report, "report");
        try {
            Glowroot.addTransactionAttribute("probe.kind", report.kind().name());
            Glowroot.addTransactionAttribute("probe.status", report.status().name());
            for (final var result : report.results()) {
                final var key = sanitize(result.name());
                Glowroot.addTransactionAttribute("probe." + key + ".status", result.status().name());
                if (!result.detail().isBlank()) {
                    Glowroot.addTransactionAttribute("probe." + key + ".detail", result.detail());
                }
            }
        } catch (final Throwable ignore) {
            // Glowroot agent not present; do not affect the caller
        }
    }

    private static String sanitize(final String name) {
        return name == null || name.isBlank() ? "unnamed" : name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
