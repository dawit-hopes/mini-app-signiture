import base64
import canonicaljson
import os
from nacl.signing import SigningKey

def generate_signature(content, private_key_base64):
    # 1. Canonical JSON Stringification
    json_bytes = canonicaljson.encode_canonical_json(content)
    
    # 2. Decode the Private Key
    decoded_key = base64.b64decode(private_key_base64)
    
    # TweetNaCl keys are 64 bytes (32 byte seed + 32 byte pub key)
    # PyNaCl needs only the 32 byte seed
    seed = decoded_key[:32]
    signing_key = SigningKey(seed)
    
    # 3. Sign the Data (detached signature)
    signed = signing_key.sign(json_bytes)
    
    # 4. Return Base64 encoded signature
    return base64.b64encode(signed.signature).decode('utf-8')

if __name__ == "__main__":
    # Read environment variables
    priv_key = os.getenv("ED25519_PRIVATE_KEY")
    app_code = os.getenv("APP_CODE", "")
    merchant_code = os.getenv("MERCHANT_CODE", "")
    merchant_reference = os.getenv("MERCHANT_REFERENCE", "")
    title = os.getenv("TITLE", "")
    total_amount = os.getenv("TOTAL_AMOUNT", "0")
    currency = os.getenv("CURRENCY", "")
    credit_account_number = os.getenv("CREDIT_ACCOUNT_NUMBER", "")
    
    # Validate required environment variables
    if not priv_key:
        print("❌ Error: ED25519_PRIVATE_KEY not set in environment")
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
    sig = generate_signature(content, priv_key)
    
    # Get JSON string for payload export
    json_payload = canonicaljson.encode_canonical_json(content).decode('utf-8')
    
    print(f"✅ Python Ed25519 Signature: {sig}")
    print(f"export SIGNATURE={sig}")
    print(f"export PAYLOAD='{json_payload}'")
