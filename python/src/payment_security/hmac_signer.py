import hmac
import hashlib
import canonicaljson

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
    content = {
        "app_code": "015489",
        "merchant_code": "MINIMRC-7914388979",
        "total_amount": 5,
        "currency": "ETB"
    }
    my_secret = "yuTqIYiOwhTA+ssH8cPZBJ8DZT8fprRbTodpncAn3oseMPDLx256iNENhQREsdKnDrEXfGwR7n2moCDxOWpQTteq4NUiVNmU"
    print(f"HMAC Signature: {sign_payload_hmac(content, my_secret)}")