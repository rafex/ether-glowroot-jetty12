package dev.rafex.ether.glowroot.jetty12;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/*-
 * #%L
 * ether-glowroot-jetty12
 * %%
 * Copyright (C) 2025 - 2026 Raúl Eduardo González Argote
 * %%
 * Test helper — not distributed.
 * #L%
 */

import dev.rafex.ether.websocket.core.WebSocketCloseStatus;
import dev.rafex.ether.websocket.core.WebSocketSession;

/**
 * In-memory {@link WebSocketSession} test double.
 *
 * <p>
 * Tracks sent messages and stored attributes so assertions can verify what the
 * endpoint wrote to the session.
 * </p>
 */
final class TestWebSocketSession implements WebSocketSession {

    private final String id;
    private final String path;
    private final Map<String, Object> attributes = new HashMap<>();
    private boolean open = true;

    TestWebSocketSession(final String path) {
        this.id = UUID.randomUUID().toString();
        this.path = path;
    }

    /* ── WebSocketSession implementation ── */

    @Override
    public String id() {
        return id;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public String subprotocol() {
        return null;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public String pathParam(final String name) {
        return null;
    }

    @Override
    public String queryFirst(final String name) {
        return null;
    }

    @Override
    public List<String> queryAll(final String name) {
        return List.of();
    }

    @Override
    public String headerFirst(final String name) {
        return null;
    }

    @Override
    public Object attribute(final String name) {
        return attributes.get(name);
    }

    @Override
    public void attribute(final String name, final Object value) {
        attributes.put(name, value);
    }

    @Override
    public Map<String, String> pathParams() {
        return Map.of();
    }

    @Override
    public Map<String, List<String>> queryParams() {
        return Map.of();
    }

    @Override
    public Map<String, List<String>> headers() {
        return Map.of();
    }

    @Override
    public CompletionStage<Void> sendText(final String text) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletionStage<Void> sendBinary(final ByteBuffer data) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletionStage<Void> close(final WebSocketCloseStatus status) {
        this.open = false;
        return CompletableFuture.completedFuture(null);
    }
}
