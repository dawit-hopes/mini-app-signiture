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
        // 1. Define the Content
        Map<String, Object> contentToSign = new HashMap<>();
        contentToSign.put("app_code", "015489");
        contentToSign.put("merchant_code", "MINIMRC-7914388979");
        contentToSign.put("merchant_reference", "txn-2345");
        contentToSign.put("title", "Forget the church");
        contentToSign.put("total_amount", 5);
        contentToSign.put("currency", "ETB");
        contentToSign.put("credit_account_number", "");

        String mySecret = "yuTqIYiOwhTA+ssH8cPZBJ8DZT8fprRbTodpncAn3oseMPDLx256iNENhQREsdKnDrEXfGwR7n2moCDxOWpQTteq4NUiVNmU";

        try {
            String signature = signPayloadHMAC(contentToSign, mySecret);

            System.out.println("Payload: " + contentToSign);
            System.out.println("HMAC Signature: " + signature);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}