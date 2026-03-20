package dev.rafex.ether.glowroot.jetty12;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import dev.rafex.ether.observability.core.probe.ProbeCheck;
import dev.rafex.ether.observability.core.probe.ProbeKind;
import dev.rafex.ether.observability.core.probe.ProbeReport;
import dev.rafex.ether.observability.core.probe.ProbeResult;
import dev.rafex.ether.observability.core.probe.ProbeStatus;
import dev.rafex.ether.observability.core.request.RequestIdGenerator;
import dev.rafex.ether.observability.core.timing.TimingSample;

class GlowrootObservabilityBridgeTest {

    @Test
    void requestIdGeneratorShouldReturnIdsWithoutThrowing() {
        final RequestIdGenerator generator = new GlowrootRequestIdGenerator();

        final var requestId = generator.nextId();

        assertNotNull(requestId);
        assertFalse(requestId.isBlank());
    }

    @Test
    void timingRecorderShouldAcceptSamples() {
        final var recorder = new GlowrootTimingRecorder();
        final var sample = new TimingSample("http.request", Instant.now(), Instant.now().plusMillis(12));

        assertDoesNotThrow(() -> recorder.record(sample));
    }

    @Test
    void probeReporterShouldCaptureProbeReports() {
        final ProbeCheck db = () -> new ProbeResult("db", ProbeKind.HEALTH, ProbeStatus.UP, "");
        final ProbeCheck cache = () -> new ProbeResult("cache", ProbeKind.HEALTH, ProbeStatus.DEGRADED, "warmup");

        final ProbeReport report = GlowrootProbeReporter.capture(ProbeKind.HEALTH, List.of(db, cache));

        assertNotNull(report);
        assertDoesNotThrow(() -> GlowrootProbeReporter.record(report));
    }
}
