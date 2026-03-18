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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GlowrootHealthExclusionMiddleware")
class GlowrootHealthExclusionMiddlewareTest {

	/* ── defaults() factory ── */

	@ParameterizedTest(name = "default exclusion: {0}")
	@ValueSource(strings = {"/health", "/ready", "/live", "/metrics"})
	@DisplayName("defaults() excludes the standard health paths")
	void defaultPathsAreExcluded(final String path) throws Exception {
		final var called = new AtomicBoolean(false);
		final var middleware = GlowrootHealthExclusionMiddleware.defaults();
		final var wrapped = middleware.wrap(ex -> {
			called.set(true);
			return true;
		});

		// The handler is still called (we don't block); Glowroot threshold is raised silently
		wrapped.handle(new TestHttpExchange("GET", path));
		assertTrue(called.get(), "Next handler must still be called for " + path);
	}

	@Test
	@DisplayName("non-health path still calls next handler")
	void nonHealthPathCallsNext() throws Exception {
		final var called = new AtomicBoolean(false);
		final var middleware = GlowrootHealthExclusionMiddleware.defaults();
		final var wrapped = middleware.wrap(ex -> {
			called.set(true);
			return true;
		});

		wrapped.handle(new TestHttpExchange("GET", "/api/users"));
		assertTrue(called.get());
	}

	/* ── of() factory ── */

	@Test
	@DisplayName("of() uses the provided custom paths")
	void customPaths() throws Exception {
		final var called = new AtomicBoolean(false);
		final var middleware = GlowrootHealthExclusionMiddleware.of("/ping", "/status");
		final var wrapped = middleware.wrap(ex -> {
			called.set(true);
			return true;
		});

		wrapped.handle(new TestHttpExchange("GET", "/ping"));
		assertTrue(called.get());
	}

	@Test
	@DisplayName("path not in custom set still calls next")
	void pathNotInCustomSetCallsNext() throws Exception {
		final var called = new AtomicBoolean(false);
		final var middleware = GlowrootHealthExclusionMiddleware.of("/ping");
		final var wrapped = middleware.wrap(ex -> {
			called.set(true);
			return true;
		});

		// /health is NOT in the custom set
		wrapped.handle(new TestHttpExchange("GET", "/health"));
		assertTrue(called.get());
	}

	/* ── null / edge cases ── */

	@Test
	@DisplayName("null path does not throw")
	void nullPathDoesNotThrow() throws Exception {
		final var middleware = GlowrootHealthExclusionMiddleware.defaults();
		final var wrapped = middleware.wrap(ex -> true);
		assertDoesNotThrow(() -> wrapped.handle(new TestHttpExchange("GET", null)));
	}

	@Test
	@DisplayName("exception from next handler is propagated")
	void exceptionPropagated() {
		final var middleware = GlowrootHealthExclusionMiddleware.defaults();
		final var wrapped = middleware.wrap(ex -> {
			throw new RuntimeException("boom");
		});

		assertThrows(RuntimeException.class,
				() -> wrapped.handle(new TestHttpExchange("GET", "/api/x")));
	}

	/* ── DEFAULT_PATHS constant ── */

	@Test
	@DisplayName("DEFAULT_PATHS contains the expected entries")
	void defaultPathsConstant() {
		assertTrue(GlowrootHealthExclusionMiddleware.DEFAULT_PATHS.contains("/health"));
		assertTrue(GlowrootHealthExclusionMiddleware.DEFAULT_PATHS.contains("/ready"));
		assertTrue(GlowrootHealthExclusionMiddleware.DEFAULT_PATHS.contains("/live"));
		assertTrue(GlowrootHealthExclusionMiddleware.DEFAULT_PATHS.contains("/metrics"));
	}
}
