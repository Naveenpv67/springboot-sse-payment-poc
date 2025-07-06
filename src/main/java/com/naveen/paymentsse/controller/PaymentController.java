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
        // Build a hardcoded PaymentStatusResponse with dummy data
        com.naveen.paymentsse.dto.PaymentStatusResponse response = new com.naveen.paymentsse.dto.PaymentStatusResponse();
        response.setRefId(refId);
        response.setTransactionStatus(status);
        response.setTransactionTime(java.time.LocalDateTime.now().toString());
        response.setTransactionAmount("100.00");
        java.util.List<com.naveen.paymentsse.dto.PaymentStatusResponse.AccountDetail> accounts = new java.util.ArrayList<>();
        for (int i = 0; i < 20; i++) {
            accounts.add(new com.naveen.paymentsse.dto.PaymentStatusResponse.AccountDetail(
                    "123456789" + i, "CUST00" + (i + 1), "987654321" + i, "5000.00"
            ));
        }
        response.setAccounts(accounts);
        paymentService.completeTransaction(refId, response);
        return ResponseEntity.ok("Callback received for refId: " + refId);
    }
}
