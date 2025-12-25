package main

import (
	"crypto/ed25519"
	"crypto/hmac"
	"crypto/sha256"
	"encoding/base64"
	"encoding/hex"
	"fmt"
	"os"

	"github.com/gibson042/canonicaljson-go"
)

func main() {
	// Read environment variables set by the signing process
	signature := os.Getenv("SIGNATURE")
	payload := os.Getenv("PAYLOAD")
	publicKeyBase64 := os.Getenv("ED25519_PUBLIC_KEY")
	appSecret := os.Getenv("HMAC_SECRET")
	signatureType := os.Getenv("SIGNATURE_TYPE")

	// Validate required environment variables
	if signature == "" {
		fmt.Fprintf(os.Stderr, "❌ Error: SIGNATURE not set in environment\n")
		os.Exit(1)
	}
	if payload == "" {
		fmt.Fprintf(os.Stderr, "❌ Error: PAYLOAD not set in environment\n")
		os.Exit(1)
	}

	// Default to ed25519 if not specified
	if signatureType == "" {
		signatureType = "ed25519"
	}

	fmt.Printf("Verifying %s signature...\n", signatureType)
	fmt.Printf("Signature: %s\n", signature)
	fmt.Printf("Payload: %s\n", payload)

	switch signatureType {
	case "ed25519":
		if publicKeyBase64 == "" {
			fmt.Fprintf(os.Stderr, "❌ Error: ED25519_PUBLIC_KEY not set in environment\n")
			os.Exit(1)
		}
		verifyEd25519(payload, signature, publicKeyBase64)

	case "hmac":
		if appSecret == "" {
			fmt.Fprintf(os.Stderr, "❌ Error: HMAC_SECRET not set in environment\n")
			os.Exit(1)
		}
		verifyHMAC(payload, signature, appSecret)

	default:
		fmt.Fprintf(os.Stderr, "❌ Unknown signature type: %s (use 'ed25519' or 'hmac')\n", signatureType)
		os.Exit(1)
	}
}

func verifyEd25519(payload, signatureBase64, publicKeyBase64 string) {
	// Decode the public key
	rawPublicKey, err := base64.StdEncoding.DecodeString(publicKeyBase64)
	if err != nil {
		fmt.Fprintf(os.Stderr, "❌ Failed to decode Base64 Public Key: %v\n", err)
		os.Exit(1)
	}

	if len(rawPublicKey) != ed25519.PublicKeySize {
		fmt.Fprintf(os.Stderr, "❌ Invalid Public Key length: expected %d bytes, got %d\n", ed25519.PublicKeySize, len(rawPublicKey))
		os.Exit(1)
	}

	// Decode the signature
	rawSignature := make([]byte, base64.StdEncoding.DecodedLen(len(signatureBase64)))
	n, err := base64.StdEncoding.Decode(rawSignature, []byte(signatureBase64))
	if err != nil {
		fmt.Fprintf(os.Stderr, "❌ Failed to decode Base64 Signature: %v\n", err)
		os.Exit(1)
	}
	rawSignature = rawSignature[:n]

	if len(rawSignature) != ed25519.SignatureSize {
		fmt.Fprintf(os.Stderr, "❌ Invalid Signature length: expected %d bytes, got %d\n", ed25519.SignatureSize, len(rawSignature))
		os.Exit(1)
	}

	// Verify the signature
	isValid := ed25519.Verify(ed25519.PublicKey(rawPublicKey), []byte(payload), rawSignature)
	if !isValid {
		fmt.Fprintf(os.Stderr, "❌ Ed25519 signature verification failed!\n")
		os.Exit(1)
	}

	fmt.Printf("✅ Ed25519 signature verified successfully!\n")
}

func verifyHMAC(payload, expectedSignature, secret string) {
	// Generate HMAC of the payload
	h := hmac.New(sha256.New, []byte(secret))
	h.Write([]byte(payload))
	computedSignature := hex.EncodeToString(h.Sum(nil))

	// Compare signatures
	if computedSignature != expectedSignature {
		fmt.Fprintf(os.Stderr, "❌ HMAC signature verification failed!\n")
		fmt.Fprintf(os.Stderr, "   Expected: %s\n", expectedSignature)
		fmt.Fprintf(os.Stderr, "   Got:      %s\n", computedSignature)
		os.Exit(1)
	}

	fmt.Printf("✅ HMAC signature verified successfully!\n")
}

// Helper function for the old verifier format (for reference)
func verifierOld(sign, publicKey, reqConfirmPayload, appSecret string) {
	tokenPaylod := map[string]interface{}{
		"app_code":              "",
		"merchant_code":         "",
		"merchant_reference":    "",
		"title":                 "",
		"total_amount":          "",
		"currency":              "",
		"credit_account_number": "",
	}

	payload, err := canonicaljson.Marshal(tokenPaylod)
	if err != nil {
		fmt.Println("error", err)
		return
	}

	if isValid, err := VerifyEd25519(payload, []byte(sign), publicKey); err != nil || !isValid {
		fmt.Printf("Invalid signature: %v", err)
		return
	}

	confirmPayload := SignPayload(payload, []byte(appSecret))
	if confirmPayload != reqConfirmPayload {
		fmt.Printf("Confirm payload mismatch: expected %s, got %s", confirmPayload, reqConfirmPayload)
		return
	}
}

func VerifyEd25519(payload, signatureBase64 []byte, publicKeyBase64 string) (bool, error) {
	rawPublicKey, err := base64.StdEncoding.DecodeString(publicKeyBase64)
	if err != nil {
		return false, fmt.Errorf("failed to decode Base64 Public Key: %w", err)
	}

	if len(rawPublicKey) != ed25519.PublicKeySize {
		return false, fmt.Errorf("invalid Public Key length: expected %d bytes, got %d", ed25519.PublicKeySize, len(rawPublicKey))
	}

	rawSignature := make([]byte, base64.StdEncoding.DecodedLen(len(signatureBase64)))
	n, err := base64.StdEncoding.Decode(rawSignature, signatureBase64)
	if err != nil {
		return false, fmt.Errorf("failed to decode Base64 Signature: %w", err)
	}
	rawSignature = rawSignature[:n]

	if len(rawSignature) != ed25519.SignatureSize {
		return false, fmt.Errorf("invalid Signature length: expected %d bytes, got %d", ed25519.SignatureSize, len(rawSignature))
	}

	isValid := ed25519.Verify(rawPublicKey, payload, rawSignature)
	return isValid, nil
}

func SignPayload(body []byte, secret []byte) string {
	h := hmac.New(sha256.New, secret)
	h.Write(body)
	return hex.EncodeToString(h.Sum(nil))
}
