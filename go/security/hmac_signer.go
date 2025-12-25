package security

import (
	"crypto/hmac"
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
)

func SignPayloadHMAC(body map[string]interface{}, secret string) (string, error) {
	// 1. Canonical JSON (Go's json.Marshal automatically sorts Map keys)
	jsonBytes, err := json.Marshal(body)
	if err != nil {
		return "", err
	}

	// 2. Create HMAC-SHA256
	h := hmac.New(sha256.New, []byte(secret))
	h.Write(jsonBytes)

	// 3. Return Hex string
	return hex.EncodeToString(h.Sum(nil)), nil
}
