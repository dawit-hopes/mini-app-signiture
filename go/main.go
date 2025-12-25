package main

import (
	"encoding/json"
	"fmt"
	"os"
	"payment-security/security"
	"strconv"
)

func main() {
	// Read environment variables
	privKey := os.Getenv("ED25519_PRIVATE_KEY")
	hmacSecret := os.Getenv("HMAC_SECRET")
	appCode := os.Getenv("APP_CODE")
	merchantCode := os.Getenv("MERCHANT_CODE")
	merchantReference := os.Getenv("MERCHANT_REFERENCE")
	title := os.Getenv("TITLE")
	totalAmountStr := os.Getenv("TOTAL_AMOUNT")
	currency := os.Getenv("CURRENCY")
	creditAccountNumber := os.Getenv("CREDIT_ACCOUNT_NUMBER")

	// Build the content map from environment variables
	totalAmount := 0
	if totalAmountStr != "" {
		if val, err := strconv.Atoi(totalAmountStr); err == nil {
			totalAmount = val
		}
	}

	content := map[string]interface{}{
		"app_code":              appCode,
		"merchant_code":         merchantCode,
		"merchant_reference":    merchantReference,
		"title":                 title,
		"total_amount":          totalAmount,
		"currency":              currency,
		"credit_account_number": creditAccountNumber,
	}

	// Get JSON string for export
	jsonBytes, err := json.Marshal(content)
	if err != nil {
		fmt.Fprintf(os.Stderr, "❌ Error marshaling JSON: %v\n", err)
		os.Exit(1)
	}
	jsonString := string(jsonBytes)

	// Check which signer to use based on command line argument
	if len(os.Args) > 1 {
		signerType := os.Args[1]

		switch signerType {
		case "hmac":
			if hmacSecret == "" {
				fmt.Fprintf(os.Stderr, "❌ Error: HMAC_SECRET not set in environment\n")
				os.Exit(1)
			}
			sig, err := security.SignPayloadHMAC(content, hmacSecret)
			if err != nil {
				fmt.Fprintf(os.Stderr, "❌ Error generating HMAC signature: %v\n", err)
				os.Exit(1)
			}
			fmt.Printf("✅ Go HMAC Signature: %s\n", sig)
			fmt.Printf("export SIGNATURE=%s\n", sig)
			fmt.Printf("export PAYLOAD='%s'\n", jsonString)

		case "sign":
			if privKey == "" {
				fmt.Fprintf(os.Stderr, "❌ Error: ED25519_PRIVATE_KEY not set in environment\n")
				os.Exit(1)
			}
			sig, err := security.GenerateSignature(content, privKey)
			if err != nil {
				fmt.Fprintf(os.Stderr, "❌ Error generating Ed25519 signature: %v\n", err)
				os.Exit(1)
			}
			fmt.Printf("✅ Go Ed25519 Signature: %s\n", sig)
			fmt.Printf("export SIGNATURE=%s\n", sig)
			fmt.Printf("export PAYLOAD='%s'\n", jsonString)

		default:
			fmt.Fprintf(os.Stderr, "❌ Unknown signer type: %s (use 'hmac' or 'sign')\n", signerType)
			os.Exit(1)
		}
	} else {
		// Default behavior: run both signers
		if hmacSecret != "" {
			sig, _ := security.SignPayloadHMAC(content, hmacSecret)
			fmt.Printf("✅ Go HMAC Signature: %s\n", sig)
		}

		if privKey != "" {
			sig, _ := security.GenerateSignature(content, privKey)
			fmt.Printf("✅ Go Ed25519 Signature: %s\n", sig)
		}
	}
}
