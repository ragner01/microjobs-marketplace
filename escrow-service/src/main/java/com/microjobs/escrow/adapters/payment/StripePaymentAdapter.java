package com.microjobs.escrow.adapters.payment;

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
public class StripePaymentAdapter implements PaymentProvider {

    private final RestTemplate restTemplate;

    @Value("${stripe.secret-key:sk_test_your_stripe_secret_key}")
    private String secretKey;

    @Value("${stripe.publishable-key:pk_test_your_stripe_publishable_key}")
    private String publishableKey;

    @Value("${stripe.base-url:https://api.stripe.com/v1}")
    private String baseUrl;

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        try {
            log.info("Processing Stripe payment for transaction: {}", request.getTransactionId());

            String url = baseUrl + "/payment_intents";

            Map<String, Object> payload = new HashMap<>();
            payload.put("amount", request.getAmount().multiply(BigDecimal.valueOf(100)).intValue()); // Convert to cents
            payload.put("currency", request.getCurrency().toLowerCase());
            payload.put("metadata", Map.of(
                "transaction_id", request.getTransactionId().toString(),
                "description", request.getDescription()
            ));
            payload.put("receipt_email", request.getEmail());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(secretKey, ""); // Stripe uses basic auth with secret key

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                String id = (String) responseBody.get("id");
                String status = (String) responseBody.get("status");
                
                if ("requires_payment_method".equals(status) || "requires_confirmation".equals(status)) {
                    log.info("Stripe payment intent created successfully. ID: {}", id);
                    
                    return PaymentResult.builder()
                            .success(true)
                            .transactionId(request.getTransactionId())
                            .reference(id)
                            .message("Payment intent created. Client secret: " + responseBody.get("client_secret"))
                            .build();
                } else {
                    log.error("Stripe payment intent creation failed. Status: {}", status);
                    return PaymentResult.failure(request.getTransactionId(), "Payment intent creation failed. Status: " + status);
                }
            } else {
                log.error("Stripe API returned unexpected status: {}", response.getStatusCode());
                return PaymentResult.failure(request.getTransactionId(), "Unexpected API response");
            }

        } catch (Exception e) {
            log.error("Error processing Stripe payment for transaction: {}", request.getTransactionId(), e);
            return PaymentResult.failure(request.getTransactionId(), "Payment processing failed: " + e.getMessage());
        }
    }

    @Override
    public PaymentResult verifyPayment(String reference) {
        try {
            log.info("Verifying Stripe payment with reference: {}", reference);

            String url = baseUrl + "/payment_intents/" + reference;

            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(secretKey, "");

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                String status = (String) responseBody.get("status");
                
                if ("succeeded".equals(status)) {
                    log.info("Stripe payment verified successfully for reference: {}", reference);
                    return PaymentResult.builder()
                            .success(true)
                            .reference(reference)
                            .message("Payment verified successfully")
                            .build();
                } else {
                    log.warn("Stripe payment not successful for reference: {}. Status: {}", reference, status);
                    return PaymentResult.failure(UUID.randomUUID(), "Payment not successful. Status: " + status);
                }
            } else {
                log.error("Stripe verification API returned unexpected status: {}", response.getStatusCode());
                return PaymentResult.failure(UUID.randomUUID(), "Verification failed");
            }

        } catch (Exception e) {
            log.error("Error verifying Stripe payment with reference: {}", reference, e);
            return PaymentResult.failure(UUID.randomUUID(), "Payment verification failed: " + e.getMessage());
        }
    }

    @Override
    public PaymentResult refundPayment(RefundRequest request) {
        try {
            log.info("Processing Stripe refund for transaction: {}", request.getTransactionId());

            String url = baseUrl + "/refunds";

            Map<String, Object> payload = new HashMap<>();
            payload.put("payment_intent", request.getOriginalTransactionId());
            payload.put("amount", request.getAmount().multiply(BigDecimal.valueOf(100)).intValue()); // Convert to cents
            payload.put("reason", "requested_by_customer");
            payload.put("metadata", Map.of(
                "reason", request.getReason(),
                "transaction_id", request.getTransactionId().toString()
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(secretKey, "");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                String id = (String) responseBody.get("id");
                String status = (String) responseBody.get("status");
                
                if ("succeeded".equals(status)) {
                    log.info("Stripe refund processed successfully. ID: {}", id);
                    
                    return PaymentResult.builder()
                            .success(true)
                            .transactionId(request.getTransactionId())
                            .reference(id)
                            .message("Refund processed successfully")
                            .build();
                } else {
                    log.error("Stripe refund failed. Status: {}", status);
                    return PaymentResult.failure(request.getTransactionId(), "Refund failed. Status: " + status);
                }
            } else {
                log.error("Stripe refund API returned unexpected status: {}", response.getStatusCode());
                return PaymentResult.failure(request.getTransactionId(), "Refund processing failed");
            }

        } catch (Exception e) {
            log.error("Error processing Stripe refund for transaction: {}", request.getTransactionId(), e);
            return PaymentResult.failure(request.getTransactionId(), "Refund processing failed: " + e.getMessage());
        }
    }
}
