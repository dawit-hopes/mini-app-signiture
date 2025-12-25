package security

import (
	"crypto/ed25519"
	"encoding/base64"
	"encoding/json"
)

func GenerateSignature(content map[string]interface{}, privateKeyBase64 string) (string, error) {
	// 1. Canonical JSON
	jsonBytes, err := json.Marshal(content)
	if err != nil {
		return "", err
	}

	// 2. Decode Private Key
	privateKeyBytes, err := base64.StdEncoding.DecodeString(privateKeyBase64)
	if err != nil {
		return "", err
	}

	// TweetNaCl (Node) uses 64-byte keys. Go's crypto/ed25519 uses the same.
	// If you only have the 32-byte seed, use ed25519.NewKeyFromSeed(seed).
	var priv ed25519.PrivateKey = privateKeyBytes

	// 3. Sign
	signature := ed25519.Sign(priv, jsonBytes)

	// 4. Return Base64
	return base64.StdEncoding.EncodeToString(signature), nil
}
