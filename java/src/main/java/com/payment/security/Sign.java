package com.payment.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Sign {
    public static String generateSignature(Map<String, Object> content, String privateKeyBase64) throws Exception {
        // 1. Canonical JSON Stringification and UTF-8 Encoding
        // We use Jackson configured to ORDER_MAP_ENTRIES_BY_KEYS to mimic 'json-stable-stringify'.
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        
        // This produces a compact JSON string with keys sorted alphabetically
        String jsonString = mapper.writeValueAsString(content);

        // We must use UTF-8 to get a consistent byte array.
        byte[] dataToSign = jsonString.getBytes(StandardCharsets.UTF_8);

        // 2. Decode the Private Key
        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64);

        // TweetNaCl uses a 64-byte secret key (32 bytes seed + 32 bytes public key).
        // Bouncy Castle (and standard Ed25519) only needs the first 32 bytes (the seed) to sign.
        if (privateKeyBytes.length == 64) {
            privateKeyBytes = Arrays.copyOfRange(privateKeyBytes, 0, 32);
        } else if (privateKeyBytes.length != 32) {
             throw new IllegalArgumentException("Invalid private key length. Expected 64 (TweetNaCl format) or 32 bytes.");
        }

        // 3. Sign the Data (using Ed25519)
        Ed25519PrivateKeyParameters keyParams = new Ed25519PrivateKeyParameters(privateKeyBytes, 0);
        Ed25519Signer signer = new Ed25519Signer();
        signer.init(true, keyParams);
        signer.update(dataToSign, 0, dataToSign.length);
        
        byte[] signatureBytes = signer.generateSignature();

        // 4. Base64 Encode the Signature
        return Base64.getEncoder().encodeToString(signatureBytes);
    }

    // ====================================================================
    // ✅ ACTUAL METHOD CALL IMPLEMENTATION
    // ====================================================================
    public static void main(String[] args) {
        // 1. Read Environment Variables
        String privateKeyBase64 = System.getenv("ED25519_PRIVATE_KEY");
        String appCode = System.getenv("APP_CODE");
        String merchantCode = System.getenv("MERCHANT_CODE");
        String merchantReference = System.getenv("MERCHANT_REFERENCE");
        String title = System.getenv("TITLE");
        String totalAmountStr = System.getenv("TOTAL_AMOUNT");
        String currency = System.getenv("CURRENCY");
        String creditAccountNumber = System.getenv("CREDIT_ACCOUNT_NUMBER");

        // Validate environment variables
        if (privateKeyBase64 == null || privateKeyBase64.isEmpty()) {
            System.err.println("❌ Error: ED25519_PRIVATE_KEY not set in environment");
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
            // 3. Call the Method
            String signature = generateSignature(contentToSign, privateKeyBase64);

            // 4. Output the Result
            System.out.println("--- Input Content ---");
            System.out.println(contentToSign);
            System.out.println("---------------------");
            
            // Re-generate json string purely for display to match Node script output
            ObjectMapper displayMapper = new ObjectMapper();
            displayMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
            String jsonString = displayMapper.writeValueAsString(contentToSign);
            System.out.println("JSON String to Sign: " + jsonString);
            
            System.out.println("---------------------");
            System.out.println("✅ Generated Signature: " + signature);
            
            // Export to environment (for child processes)
            System.out.println("export SIGNATURE=" + signature);
            System.out.println("export PAYLOAD='" + jsonString.replace("'", "'\\''") + "'");

        } catch (Exception e) {
            System.err.println("❌ Error generating signature: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}