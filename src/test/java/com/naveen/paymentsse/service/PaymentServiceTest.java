package com.naveen.paymentsse.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceTest {
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService();
    }

    @Test
    void testHappyPath() throws Exception {
        String refId = "12345";
        paymentService.createTransaction(refId);
        SseEmitter emitter = paymentService.getStatusStream(refId);
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] completed = {false};
        emitter.onCompletion(() -> {
            completed[0] = true;
            latch.countDown();
        });
        paymentService.completeTransaction(refId, "SUCCESS");
        assertTrue(latch.await(2, TimeUnit.SECONDS), "Emitter did not complete in time");
        assertTrue(completed[0], "Emitter completion callback not called");
    }

    @Test
    void testCallbackBeforeSseConnect() throws Exception {
        String refId = "ref-callback-first";
        paymentService.createTransaction(refId);
        paymentService.completeTransaction(refId, "SUCCESS");
        SseEmitter emitter = paymentService.getStatusStream(refId);
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] completed = {false};
        emitter.onCompletion(() -> {
            completed[0] = true;
            latch.countDown();
        });
        assertTrue(latch.await(1, TimeUnit.SECONDS), "Emitter did not complete in time");
        assertTrue(completed[0], "Emitter completion callback not called");
    }

    @Test
    void testSseConnectBeforeCallback() throws Exception {
        String refId = "ref-sse-first";
        paymentService.createTransaction(refId);
        SseEmitter emitter = paymentService.getStatusStream(refId);
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] completed = {false};
        emitter.onCompletion(() -> {
            completed[0] = true;
            latch.countDown();
        });
        paymentService.completeTransaction(refId, "SUCCESS");
        assertTrue(latch.await(1, TimeUnit.SECONDS), "Emitter did not complete in time");
        assertTrue(completed[0], "Emitter completion callback not called");
    }

    @Test
    void testMultipleStatusRequests() throws Exception {
        String refId = "ref-multi";
        paymentService.createTransaction(refId);
        SseEmitter emitter1 = paymentService.getStatusStream(refId);
        SseEmitter emitter2 = paymentService.getStatusStream(refId);
        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);
        final boolean[] completed1 = {false};
        final boolean[] completed2 = {false};
        emitter1.onCompletion(() -> {
            completed1[0] = true;
            latch1.countDown();
        });
        emitter2.onCompletion(() -> {
            completed2[0] = true;
            latch2.countDown();
        });
        paymentService.completeTransaction(refId, "SUCCESS");
        assertTrue(latch1.await(1, TimeUnit.SECONDS), "First emitter did not complete in time");
        // Only the first emitter should get the status, second is orphaned
        assertFalse(latch2.await(500, TimeUnit.MILLISECONDS), "Second emitter should not complete");
        assertTrue(completed1[0], "First emitter completion callback not called");
        assertFalse(completed2[0], "Second emitter should not be completed");
    }

    @Test
    void testNoCallback() throws Exception {
        String refId = "ref-nocallback";
        paymentService.createTransaction(refId);
        SseEmitter emitter = paymentService.getStatusStream(refId);
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] completed = {false};
        emitter.onCompletion(() -> {
            completed[0] = true;
            latch.countDown();
        });
        // No callback, should not complete
        assertFalse(latch.await(500, TimeUnit.MILLISECONDS), "Emitter should not complete");
        assertFalse(completed[0], "Emitter completion callback should not be called");
    }

    @Test
    void testInvalidRefId() throws Exception {
        String refId = "ref-invalid";
        SseEmitter emitter = paymentService.getStatusStream(refId);
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] completed = {false};
        emitter.onCompletion(() -> {
            completed[0] = true;
            latch.countDown();
        });
        // No transaction, no callback
        assertFalse(latch.await(500, TimeUnit.MILLISECONDS), "Emitter should not complete");
        assertFalse(completed[0], "Emitter completion callback should not be called");
    }

    @Test
    void testConcurrentRequests() throws Exception {
        int count = 10;
        CountDownLatch latch = new CountDownLatch(count);
        final boolean[] completed = new boolean[count];
        for (int i = 0; i < count; i++) {
            String refId = "ref-concurrent-" + i;
            paymentService.createTransaction(refId);
            SseEmitter emitter = paymentService.getStatusStream(refId);
            final int idx = i;
            emitter.onCompletion(() -> {
                completed[idx] = true;
                latch.countDown();
            });
            paymentService.completeTransaction(refId, "SUCCESS");
        }
        assertTrue(latch.await(2, TimeUnit.SECONDS), "Not all emitters completed in time");
        for (boolean c : completed) assertTrue(c, "Some emitter completion callbacks not called");
    }
}
