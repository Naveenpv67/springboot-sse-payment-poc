package com.naveen.paymentsse.service;


import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.naveen.paymentsse.dto.PaymentStatusResponse;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@EnableScheduling
public class PaymentService {
    // Map of refId to SseEmitter
    private final ConcurrentMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    // In-memory cache for status until consumed (refId -> CacheEntry)
    private static class CacheEntry {
        final PaymentStatusResponse response;
        final long createdAt;
        CacheEntry(PaymentStatusResponse response) {
            this.response = response;
            this.createdAt = System.currentTimeMillis();
        }
    }
    private final ConcurrentMap<String, CacheEntry> statusCache = new ConcurrentHashMap<>();

    // TTL in ms for status cache (e.g., 30 seconds)
    private static final long STATUS_CACHE_TTL_MS = 30_000;
    // Timeout for SSE emitters (e.g., 5 seconds)
    private static final long EMITTER_TIMEOUT_MS = 5_000;

    public PaymentService() {}


    public String createTransaction(String refId) {
        // Optionally, store INITIATED status in cache if you want to always show initial state
        // statusCache.put(refId, "INITIATED");
        return "Transaction registered for refId: " + refId;
    }

    public SseEmitter getStatusStream(String refId) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MS);
        emitters.put(refId, emitter);

        // Remove emitter on completion/error/timeout
        Runnable cleanup = () -> emitters.remove(refId);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        // Check in-memory cache for status
        sendAndCompleteIfCached(emitter, refId);
        // else: wait for callback to arrive
        return emitter;
    }

    public void completeTransaction(String refId, PaymentStatusResponse statusResponse) {
        SseEmitter emitter = emitters.remove(refId);
        if (emitter != null) {
            sendAndComplete(emitter, statusResponse);
        } else {
            // Cache the status in memory for later consumption
            statusCache.put(refId, new CacheEntry(statusResponse));
        }
    }

    // Utility method to send and complete emitter with error handling
    private void sendAndComplete(SseEmitter emitter, PaymentStatusResponse response) {
        try {
            emitter.send(response);
            emitter.complete();
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }

    // Utility method to check cache and send/complete if present
    private void sendAndCompleteIfCached(SseEmitter emitter, String refId) {
        CacheEntry cached = statusCache.remove(refId);
        if (cached != null) {
            sendAndComplete(emitter, cached.response);
        }
    }

    // Scheduled cleanup for statusCache (TTL)
    @Scheduled(fixedDelay = 10_000)
    public void cleanupStatusCache() {
        long now = System.currentTimeMillis();
        statusCache.entrySet().removeIf(entry -> now - entry.getValue().createdAt > STATUS_CACHE_TTL_MS);
    }

}
