package com.naveen.paymentsse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentResponse {
    private String refId;
    private String status;        // "INITIATED", "FAILED"
    private String errorMessage;  // null if success
}
