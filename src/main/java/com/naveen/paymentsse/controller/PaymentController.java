package com.naveen.paymentsse.controller;

import com.naveen.paymentsse.dto.PaymentRequest;
import com.naveen.paymentsse.dto.PaymentResponse;
import com.naveen.paymentsse.service.PaymentService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> initiate(@RequestBody @Valid PaymentRequest request) {
        String refId = request.getRefId();
        if (refId == null || refId.isBlank()) {
            return ResponseEntity.badRequest().body(new PaymentResponse(null, "FAILED", "refId is required"));
        }
        paymentService.createTransaction(refId);
        return ResponseEntity.ok(new PaymentResponse(refId, "INITIATED", null));
    }

    @GetMapping(value = "/status/{refId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamStatus(@PathVariable String refId) {
        return paymentService.getStatusStream(refId);
    }

    @PostMapping("/callback/{refId}")
    public ResponseEntity<String> receiveCallback(@PathVariable String refId,
                                                  @RequestBody Map<String, String> payload) {
        String status = payload.getOrDefault("status", "FAILURE");
        paymentService.completeTransaction(refId, status);
        return ResponseEntity.ok("Callback received for refId: " + refId);
    }
}
