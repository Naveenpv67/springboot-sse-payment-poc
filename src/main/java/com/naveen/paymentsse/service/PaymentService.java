package com.naveen.paymentsse.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaymentService {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();


    public String createTransaction(String refId) {
        SseEmitter emitter = new SseEmitter(0L); // no timeout
        emitters.put(refId, emitter);
        return refId;
    }

    public SseEmitter getStatusStream(String refId) {
        return emitters.get(refId);
    }

    public void completeTransaction(String refId, String finalStatus) {
        SseEmitter emitter = emitters.get(refId);
        if (emitter != null) {
            try {
                emitter.send(finalStatus);
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }
    }
}
