import hmac
import hashlib
import canonicaljson
import os

def sign_payload_hmac(body, secret):
    # 1. Canonical JSON Stringification (Sorted keys, no extra spaces)
    json_bytes = canonicaljson.encode_canonical_json(body)
    
    # 2. Create HMAC SHA256
    signature = hmac.new(
        secret.encode('utf-8'),
        json_bytes,
        hashlib.sha256
    ).hexdigest()
    
    return signature

# --- TEST ---
if __name__ == "__main__":
    # Read environment variables
    secret = os.getenv("HMAC_SECRET")
    app_code = os.getenv("APP_CODE", "")
    merchant_code = os.getenv("MERCHANT_CODE", "")
    merchant_reference = os.getenv("MERCHANT_REFERENCE", "")
    title = os.getenv("TITLE", "")
    total_amount = os.getenv("TOTAL_AMOUNT", "0")
    currency = os.getenv("CURRENCY", "")
    credit_account_number = os.getenv("CREDIT_ACCOUNT_NUMBER", "")
    
    # Validate required environment variables
    if not secret:
        print("❌ Error: HMAC_SECRET not set in environment")
        exit(1)
    
    # Build the content object
    content = {
        "app_code": app_code,
        "merchant_code": merchant_code,
        "merchant_reference": merchant_reference,
        "title": title,
        "total_amount": int(total_amount) if total_amount.isdigit() else total_amount,
        "currency": currency,
        "credit_account_number": credit_account_number
    }
    
    # Generate signature
    sig = sign_payload_hmac(content, secret)
    
    # Get JSON string for payload export
    json_payload = canonicaljson.encode_canonical_json(content).decode('utf-8')
    
    print(f"HMAC Signature: {sig}")
    print(f"export SIGNATURE={sig}")
    print(f"export PAYLOAD='{json_payload}'")
