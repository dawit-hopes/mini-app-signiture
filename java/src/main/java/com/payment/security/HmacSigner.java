package com.payment.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HmacSigner {

    /**
     * Generates an HMAC-SHA256 signature matching the Node.js implementation.
     * * @param body   The content map to sign
     * @param secret The shared secret key string
     * @return The Hex-encoded signature string
     */
    public static String signPayloadHMAC(Map<String, Object> body, String secret) throws Exception {
        // 1. Canonical JSON Stringification
        // Configure Jackson to sort keys alphabetically to match 'json-stable-stringify'
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        
        String jsonString = mapper.writeValueAsString(body);

        // 2. Create HMAC using SHA256
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        
        // In Node, passing a string as the secret uses UTF-8 encoding by default
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secretKey);

        // 3. Update with the body content and get bytes
        byte[] signatureBytes = sha256_HMAC.doFinal(jsonString.getBytes(StandardCharsets.UTF_8));

        // 4. Return as Hex string
        return bytesToHex(signatureBytes);
    }

    // Helper method to convert byte array to Hex String (replaces 'hex' encoding in Node)
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    // ==========================================
    // ✅ TEST
    // ==========================================
    public static void main(String[] args) {
        // 1. Read Environment Variables
        String secret = System.getenv("HMAC_SECRET");
        String appCode = System.getenv("APP_CODE");
        String merchantCode = System.getenv("MERCHANT_CODE");
        String merchantReference = System.getenv("MERCHANT_REFERENCE");
        String title = System.getenv("TITLE");
        String totalAmountStr = System.getenv("TOTAL_AMOUNT");
        String currency = System.getenv("CURRENCY");
        String creditAccountNumber = System.getenv("CREDIT_ACCOUNT_NUMBER");

        // Validate environment variables
        if (secret == null || secret.isEmpty()) {
            System.err.println("❌ Error: HMAC_SECRET not set in environment");
            System.exit(1);
        }

        // 2. Build the Content Object from environment variables
        Map<String, Object> contentToSign = new HashMap<>();
        contentToSign.put("app_code", appCode != null ? appCode : "");
        contentToSign.put("merchant_code", merchantCode != null ? merchantCode : "");
        contentToSign.put("merchant_reference", merchantReference != null ? merchantReference : "");
        contentToSign.put("title", title != null ? title : "");
        
        // Try to parse total_amount as a number if possible
        if (totalAmountStr != null && !totalAmountStr.isEmpty()) {
            try {
                contentToSign.put("total_amount", Integer.parseInt(totalAmountStr));
            } catch (NumberFormatException e) {
                contentToSign.put("total_amount", totalAmountStr);
            }
        } else {
            contentToSign.put("total_amount", 0);
        }
        
        contentToSign.put("currency", currency != null ? currency : "");
        contentToSign.put("credit_account_number", creditAccountNumber != null ? creditAccountNumber : "");

        try {
            String signature = signPayloadHMAC(contentToSign, secret);

            // Get JSON string for output
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
            String jsonString = mapper.writeValueAsString(contentToSign);
            
            System.out.println("Payload: " + contentToSign);
            System.out.println("HMAC Signature: " + signature);
            
            // Export to environment (for child processes)
            System.out.println("export SIGNATURE=" + signature);
            System.out.println("export PAYLOAD='" + jsonString.replace("'", "'\\''") + "'");
            
        } catch (Exception e) {
            System.err.println("❌ Error generating HMAC signature: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}