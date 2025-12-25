package main

import (
	"crypto/ed25519"
	"crypto/hmac"
	"crypto/sha256"
	"encoding/base64"
	"encoding/hex"
	"fmt"

	"github.com/gibson042/canonicaljson-go"
)

func verifier(sign, publicKey, reqConfirmPayload, appSecret string) {
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

	if isValid, err := Verify(payload, []byte(sign), publicKey); err != nil || !isValid {
		fmt.Printf("Invalid signature: %v", err)
		return
	}

	confirmPayload := SignPayload(payload, []byte(appSecret))
	if confirmPayload != reqConfirmPayload {
		fmt.Printf("Confirm payload mismatch: expected %s, got %s", confirmPayload, reqConfirmPayload)
		return
	}
}

func Verify(payload, signatureBase64 []byte, publicKeyBase64 string) (bool, error) {
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
