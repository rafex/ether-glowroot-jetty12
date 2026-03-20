package dev.rafex.ether.glowroot.jetty12;

import java.util.Objects;

import org.glowroot.agent.api.Glowroot;

import dev.rafex.ether.observability.core.timing.TimingRecorder;
import dev.rafex.ether.observability.core.timing.TimingSample;

/**
 * {@link TimingRecorder} implementation that publishes timing samples into
 * Glowroot transaction attributes.
 */
public final class GlowrootTimingRecorder implements TimingRecorder {

    @Override
    public void record(final TimingSample sample) {
        Objects.requireNonNull(sample, "sample");
        final var key = sanitize(sample.name());
        final var durationMs = Long.toString(sample.duration().toMillis());
        try {
            Glowroot.addTransactionAttribute("timing.last_name", sample.name());
            Glowroot.addTransactionAttribute("timing." + key + ".ms", durationMs);
            Glowroot.addTransactionAttribute("timing." + key + ".started_at", sample.startedAt().toString());
            Glowroot.addTransactionAttribute("timing." + key + ".finished_at", sample.finishedAt().toString());
        } catch (final Throwable ignore) {
            // Glowroot agent not present; do not affect request processing
        }
    }

    private static String sanitize(final String name) {
        return name == null || name.isBlank() ? "unnamed" : name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
