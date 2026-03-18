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
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GlowrootHttpMiddleware")
class GlowrootHttpMiddlewareTest {

	private final GlowrootHttpMiddleware middleware = new GlowrootHttpMiddleware();

	@Test
	@DisplayName("delegates to the next handler")
	void delegatesToNext() throws Exception {
		final var called = new AtomicBoolean(false);
		final var wrapped = middleware.wrap(ex -> {
			called.set(true);
			return true;
		});

		wrapped.handle(new TestHttpExchange("GET", "/api/users"));

		assertTrue(called.get());
	}

	@Test
	@DisplayName("next handler receives the same exchange")
	void nextReceivesSameExchange() throws Exception {
		final var received = new AtomicReference<String>();
		final var exchange = new TestHttpExchange("POST", "/api/orders");

		final var wrapped = middleware.wrap(ex -> {
			received.set(ex.method() + " " + ex.path());
			return true;
		});
		wrapped.handle(exchange);

		assertEquals("POST /api/orders", received.get());
	}

	@Test
	@DisplayName("propagates exception from next handler")
	void propagatesException() {
		final var wrapped = middleware.wrap(ex -> {
			throw new RuntimeException("handler-error");
		});

		final var ex = assertThrows(RuntimeException.class,
				() -> wrapped.handle(new TestHttpExchange("GET", "/fail")));
		assertEquals("handler-error", ex.getMessage());
	}

	@Test
	@DisplayName("does not swallow checked exception from next handler")
	void propagatesCheckedException() {
		final var wrapped = middleware.wrap(ex -> {
			throw new Exception("checked-error");
		});

		final var ex = assertThrows(Exception.class,
				() -> wrapped.handle(new TestHttpExchange("GET", "/fail")));
		assertEquals("checked-error", ex.getMessage());
	}

	@Test
	@DisplayName("null path does not throw")
	void nullPathDoesNotThrow() throws Exception {
		final var wrapped = middleware.wrap(ex -> true);
		assertDoesNotThrow(() -> wrapped.handle(new TestHttpExchange("GET", null)));
	}

	@Test
	@DisplayName("returns the boolean result from the next handler")
	void returnsHandlerResult() throws Exception {
		final var wrappedTrue = middleware.wrap(ex -> true);
		final var wrappedFalse = middleware.wrap(ex -> false);

		assertTrue(wrappedTrue.handle(new TestHttpExchange("GET", "/ok")));
		assertFalse(wrappedFalse.handle(new TestHttpExchange("GET", "/ok")));
	}
}
