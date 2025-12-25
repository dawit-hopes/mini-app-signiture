package main

import (
	"fmt"
	"payment-security/security"
)

func main() {
	content := map[string]interface{}{
		"app_code":              "015489",
		"merchant_code":         "MINIMRC-7914388979",
		"merchant_reference":    "txn-2345",
		"total_amount":          5,
		"currency":              "ETB",
		"credit_account_number": "",
	}
	secret := "yuTqIYiOwhTA+ssH8cPZBJ8DZT8fprRbTodpncAn3oseMPDLx256iNENhQREsdKnDrEXfGwR7n2moCDxOWpQTteq4NUiVNmU"

	sig, _ := security.SignPayloadHMAC(content, secret)
	fmt.Printf("✅ Go HMAC Signature: %s\n", sig)

	privKey := "bv2DsjDN/xvx1Jrpmx1SWNPcVW44lkvWnLgRNlWhKMTYXbpwY3e6OKA2f3e9DhwjdDJ5Pok2x0RTi3+Hx8IhjA=="

	sig, _ = security.GenerateSignature(content, privKey)
	fmt.Printf("✅ Go Ed25519 Signature: %s\n", sig)
}
