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
        // 1. Define the Content Object (Your Payload)
        // Using a Map to represent the JSON object
        Map<String, Object> contentToSign = new HashMap<>();
        contentToSign.put("app_code", "015489");
        contentToSign.put("merchant_code", "MINIMRC-7914388979");
        contentToSign.put("merchant_reference", "txn-2345");
        contentToSign.put("title", "Forget the church");
        contentToSign.put("total_amount", 5);
        contentToSign.put("currency", "ETB");
        contentToSign.put("credit_account_number", "");

        // 2. Define the Private Key
        // ⚠️ IMPORTANT: YOU MUST REPLACE THIS WITH YOUR ACTUAL BASE64 PRIVATE KEY.
        String MY_PRIVATE_KEY_BASE64 = "bv2DsjDN/xvx1Jrpmx1SWNPcVW44lkvWnLgRNlWhKMTYXbpwY3e6OKA2f3e9DhwjdDJ5Pok2x0RTi3+Hx8IhjA==";

        try {
            // 3. Call the Method
            String signature = generateSignature(contentToSign, MY_PRIVATE_KEY_BASE64);

            // 4. Output the Result
            System.out.println("--- Input Content ---");
            System.out.println(contentToSign);
            System.out.println("---------------------");
            
            // Re-generate json string purely for display to match Node script output
            ObjectMapper displayMapper = new ObjectMapper();
            displayMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
            System.out.println("JSON String to Sign: " + displayMapper.writeValueAsString(contentToSign));
            
            System.out.println("---------------------");
            System.out.println("✅ Generated Signature: " + signature);

        } catch (Exception e) {
            System.err.println("❌ Error generating signature: " + e.getMessage());
            e.printStackTrace();
        }
    }
}