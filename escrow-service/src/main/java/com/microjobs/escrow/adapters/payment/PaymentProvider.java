package com.microjobs.escrow.adapters.payment;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentProvider {
    
    PaymentResult processPayment(PaymentRequest request);
    
    PaymentResult verifyPayment(String reference);
    
    PaymentResult refundPayment(RefundRequest request);
    
    @Data
    @Builder
    class PaymentRequest {
        private UUID transactionId;
        private String email;
        private BigDecimal amount;
        private String currency;
        private String callbackUrl;
        private String description;
    }
    
    @Data
    @Builder
    class RefundRequest {
        private UUID transactionId;
        private String originalTransactionId;
        private BigDecimal amount;
        private String reason;
    }
    
    @Data
    @Builder
    class PaymentResult {
        private boolean success;
        private UUID transactionId;
        private String reference;
        private String message;
        private boolean duplicate;
        
        public static PaymentResult success(UUID transactionId, String reference) {
            return PaymentResult.builder()
                    .success(true)
                    .transactionId(transactionId)
                    .reference(reference)
                    .build();
        }
        
        public static PaymentResult failure(UUID transactionId, String message) {
            return PaymentResult.builder()
                    .success(false)
                    .transactionId(transactionId)
                    .message(message)
                    .build();
        }
        
        public static PaymentResult duplicate(UUID transactionId) {
            return PaymentResult.builder()
                    .success(true)
                    .transactionId(transactionId)
                    .duplicate(true)
                    .message("Duplicate request")
                    .build();
        }
    }
}
