package com.microjobs.escrow.adapters.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaystackPaymentAdapter implements PaymentProvider {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${paystack.secret-key:sk_test_your_secret_key}")
    private String secretKey;

    @Value("${paystack.public-key:pk_test_your_public_key}")
    private String publicKey;

    @Value("${paystack.base-url:https://api.paystack.co}")
    private String baseUrl;

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        try {
            log.info("Processing Paystack payment for transaction: {}", request.getTransactionId());

            String url = baseUrl + "/transaction/initialize";
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("email", request.getEmail());
            payload.put("amount", request.getAmount().multiply(BigDecimal.valueOf(100)).intValue()); // Convert to kobo
            payload.put("currency", request.getCurrency());
            payload.put("reference", request.getTransactionId().toString());
            payload.put("callback_url", request.getCallbackUrl());
            payload.put("metadata", Map.of(
                "transaction_id", request.getTransactionId().toString(),
                "description", request.getDescription()
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(secretKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                boolean status = (Boolean) responseBody.get("status");
                
                if (status) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    String reference = (String) data.get("reference");
                    String authorizationUrl = (String) data.get("authorization_url");
                    
                    log.info("Paystack payment initialized successfully. Reference: {}", reference);
                    
                    return PaymentResult.builder()
                            .success(true)
                            .transactionId(request.getTransactionId())
                            .reference(reference)
                            .message("Payment initialized. Authorization URL: " + authorizationUrl)
                            .build();
                } else {
                    String message = (String) responseBody.get("message");
                    log.error("Paystack payment initialization failed: {}", message);
                    return PaymentResult.failure(request.getTransactionId(), message);
                }
            } else {
                log.error("Paystack API returned unexpected status: {}", response.getStatusCode());
                return PaymentResult.failure(request.getTransactionId(), "Unexpected API response");
            }

        } catch (Exception e) {
            log.error("Error processing Paystack payment for transaction: {}", request.getTransactionId(), e);
            return PaymentResult.failure(request.getTransactionId(), "Payment processing failed: " + e.getMessage());
        }
    }

    @Override
    public PaymentResult verifyPayment(String reference) {
        try {
            log.info("Verifying Paystack payment with reference: {}", reference);

            String url = baseUrl + "/transaction/verify/" + reference;

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(secretKey);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                boolean status = (Boolean) responseBody.get("status");
                
                if (status) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    String transactionStatus = (String) data.get("status");
                    
                    if ("success".equals(transactionStatus)) {
                        log.info("Paystack payment verified successfully for reference: {}", reference);
                        return PaymentResult.builder()
                                .success(true)
                                .reference(reference)
                                .message("Payment verified successfully")
                                .build();
                    } else {
                        log.warn("Paystack payment not successful for reference: {}. Status: {}", reference, transactionStatus);
                        return PaymentResult.failure(UUID.randomUUID(), "Payment not successful. Status: " + transactionStatus);
                    }
                } else {
                    String message = (String) responseBody.get("message");
                    log.error("Paystack payment verification failed: {}", message);
                    return PaymentResult.failure(UUID.randomUUID(), message);
                }
            } else {
                log.error("Paystack verification API returned unexpected status: {}", response.getStatusCode());
                return PaymentResult.failure(UUID.randomUUID(), "Verification failed");
            }

        } catch (Exception e) {
            log.error("Error verifying Paystack payment with reference: {}", reference, e);
            return PaymentResult.failure(UUID.randomUUID(), "Payment verification failed: " + e.getMessage());
        }
    }

    @Override
    public PaymentResult refundPayment(RefundRequest request) {
        try {
            log.info("Processing Paystack refund for transaction: {}", request.getTransactionId());

            String url = baseUrl + "/refund";

            Map<String, Object> payload = new HashMap<>();
            payload.put("transaction", request.getOriginalTransactionId());
            payload.put("amount", request.getAmount().multiply(BigDecimal.valueOf(100)).intValue()); // Convert to kobo
            payload.put("reason", request.getReason());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(secretKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                boolean status = (Boolean) responseBody.get("status");
                
                if (status) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    String reference = (String) data.get("reference");
                    
                    log.info("Paystack refund processed successfully. Reference: {}", reference);
                    
                    return PaymentResult.builder()
                            .success(true)
                            .transactionId(request.getTransactionId())
                            .reference(reference)
                            .message("Refund processed successfully")
                            .build();
                } else {
                    String message = (String) responseBody.get("message");
                    log.error("Paystack refund failed: {}", message);
                    return PaymentResult.failure(request.getTransactionId(), message);
                }
            } else {
                log.error("Paystack refund API returned unexpected status: {}", response.getStatusCode());
                return PaymentResult.failure(request.getTransactionId(), "Refund processing failed");
            }

        } catch (Exception e) {
            log.error("Error processing Paystack refund for transaction: {}", request.getTransactionId(), e);
            return PaymentResult.failure(request.getTransactionId(), "Refund processing failed: " + e.getMessage());
        }
    }
}
