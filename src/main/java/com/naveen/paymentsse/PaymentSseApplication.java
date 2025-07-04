package com.naveen.paymentsse;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(title = "Payment SSE API", version = "1.0",
                 description = "Streaming payment status via Server-Sent Events")
)
public class PaymentSseApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentSseApplication.class, args);
    }
}
