package com.naveen.paymentsse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusResponse {
    private String refId;
    private String transactionStatus;
    private String transactionTime;
    private String transactionAmount;
    private List<AccountDetail> accounts;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountDetail {
        private String accountNumber;
        private String customerId;
        private String mobileNumber;
        private String availableBalance;
    }
}
