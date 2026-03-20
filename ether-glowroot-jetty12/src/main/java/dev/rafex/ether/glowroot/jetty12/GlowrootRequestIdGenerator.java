package dev.rafex.ether.glowroot.jetty12;

import java.util.Objects;

import org.glowroot.agent.api.Glowroot;

import dev.rafex.ether.observability.core.request.RequestIdGenerator;
import dev.rafex.ether.observability.core.request.UuidRequestIdGenerator;

/**
 * {@link RequestIdGenerator} bridge that records generated request IDs into
 * Glowroot transaction attributes.
 */
public final class GlowrootRequestIdGenerator implements RequestIdGenerator {

    private final RequestIdGenerator delegate;

    public GlowrootRequestIdGenerator() {
        this(new UuidRequestIdGenerator());
    }

    public GlowrootRequestIdGenerator(final RequestIdGenerator delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public String nextId() {
        final var requestId = delegate.nextId();
        if (requestId != null && !requestId.isBlank()) {
            try {
                Glowroot.addTransactionAttribute("request.id", requestId);
            } catch (final Throwable ignore) {
                // Glowroot agent not present; do not affect request processing
            }
        }
        return requestId;
    }
}
