package dev.rafex.ether.glowroot.jetty12;

/*-
 * #%L
 * ether-glowroot-jetty12
 * %%
 * Copyright (C) 2025 - 2026 Raúl Eduardo González Argote
 * %%
 * Test — not distributed.
 * #L%
 */

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GlowrootSlowThresholdMiddleware")
class GlowrootSlowThresholdMiddlewareTest {

	/* ── basic delegation ── */

	@Test
	@DisplayName("delegates to the next handler for any path")
	void delegatesToNext() throws Exception {
		final var called = new AtomicBoolean(false);
		final var middleware = GlowrootSlowThresholdMiddleware.builder()
				.defaultThreshold(2_000)
				.build();

		final var wrapped = middleware.wrap(ex -> {
			called.set(true);
			return true;
		});
		wrapped.handle(new TestHttpExchange("GET", "/api/users"));
		assertTrue(called.get());
	}

	@Test
	@DisplayName("returns the next handler's boolean result")
	void returnsNextResult() throws Exception {
		final var middleware = GlowrootSlowThresholdMiddleware.builder().build();

		assertTrue(middleware.wrap(ex -> true).handle(new TestHttpExchange("GET", "/x")));
		assertFalse(middleware.wrap(ex -> false).handle(new TestHttpExchange("GET", "/x")));
	}

	/* ── per-route threshold lookup ── */

	@Test
	@DisplayName("path-specific threshold is applied (Glowroot call is no-op without agent)")
	void pathSpecificThresholdIsApplied() throws Exception {
		final var called = new AtomicBoolean(false);
		final var middleware = GlowrootSlowThresholdMiddleware.builder()
				.defaultThreshold(2_000)
				.threshold("/api/export/:id", 30_000)
				.build();

		final var wrapped = middleware.wrap(ex -> {
			called.set(true);
			return true;
		});

		// UUID path → normalized to /api/export/:id → 30s threshold (no-op without agent)
		wrapped.handle(new TestHttpExchange("GET",
				"/api/export/550e8400-e29b-41d4-a716-446655440000"));
		assertTrue(called.get());
	}

	@Test
	@DisplayName("default threshold is used for unregistered paths")
	void defaultThresholdUsedForUnknownPath() throws Exception {
		final var called = new AtomicBoolean(false);
		final var middleware = GlowrootSlowThresholdMiddleware.builder()
				.defaultThreshold(500)
				.threshold("/api/export/:id", 30_000)
				.build();

		final var wrapped = middleware.wrap(ex -> {
			called.set(true);
			return true;
		});

		wrapped.handle(new TestHttpExchange("GET", "/api/users"));
		assertTrue(called.get());
	}

	@Test
	@DisplayName("plain path without variables matches directly")
	void plainPathMatchesDirect() throws Exception {
		final var called = new AtomicBoolean(false);
		final var middleware = GlowrootSlowThresholdMiddleware.builder()
				.threshold("/api/auth/login", 300)
				.build();

		final var wrapped = middleware.wrap(ex -> {
			called.set(true);
			return true;
		});

		wrapped.handle(new TestHttpExchange("POST", "/api/auth/login"));
		assertTrue(called.get());
	}

	/* ── edge cases ── */

	@Test
	@DisplayName("null path does not throw")
	void nullPathDoesNotThrow() throws Exception {
		final var middleware = GlowrootSlowThresholdMiddleware.builder()
				.defaultThreshold(2_000)
				.build();
		final var wrapped = middleware.wrap(ex -> true);

		assertDoesNotThrow(() -> wrapped.handle(new TestHttpExchange("GET", null)));
	}

	@Test
	@DisplayName("exception from next handler is propagated")
	void exceptionPropagated() {
		final var middleware = GlowrootSlowThresholdMiddleware.builder().build();
		final var wrapped = middleware.wrap(ex -> {
			throw new RuntimeException("boom");
		});

		assertThrows(RuntimeException.class,
				() -> wrapped.handle(new TestHttpExchange("GET", "/api/x")));
	}

	/* ── Builder ── */

	@Test
	@DisplayName("Builder.defaultThreshold() is chainable")
	void builderIsChainable() {
		assertNotNull(GlowrootSlowThresholdMiddleware.builder()
				.defaultThreshold(1_000)
				.threshold("/api/x", 5_000)
				.threshold("/api/y", 10_000)
				.build());
	}
}
