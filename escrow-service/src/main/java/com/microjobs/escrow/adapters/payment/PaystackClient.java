package com.microjobs.escrow.adapters.payment;

import lombok.Builder;
import lombok.Data;

public interface PaystackClient {
    
    PaystackResponse initializePayment(PaystackPaymentRequest request);
    
    PaystackVerificationResponse verifyPayment(String reference);
    
    PaystackRefundResponse refund(PaystackRefundRequest request);
    
    @Data
    @Builder
    class PaystackPaymentRequest {
        private String email;
        private int amount;
        private String reference;
        private String callbackUrl;
        private String currency;
        private String description;
    }
    
    @Data
    @Builder
    class PaystackResponse {
        private boolean success;
        private String message;
        private String reference;
        private String authorizationUrl;
        private String accessCode;
    }
    
    @Data
    @Builder
    class PaystackVerificationResponse {
        private boolean success;
        private String status;
        private String reference;
        private int amount;
        private String gatewayResponse;
        private String paidAt;
        private String createdAt;
    }
    
    @Data
    @Builder
    class PaystackRefundRequest {
        private String transaction;
        private int amount;
        private String reason;
        private String currency;
    }
    
    @Data
    @Builder
    class PaystackRefundResponse {
        private boolean success;
        private String message;
        private String reference;
        private String status;
    }
}
