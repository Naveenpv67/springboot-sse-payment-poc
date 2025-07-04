package com.naveen.paymentsse.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;


@Data
public class PaymentRequest {
    @Min(value = 1, message = "Amount must be greater than zero")
    private int amount;
    private String refId;
}
