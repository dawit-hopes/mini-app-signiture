import base64
import canonicaljson
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
    content = {
        "app_code": "015489",
        "merchant_code": "MINIMRC-7914388979",
        "merchant_reference": "txn-2345",
        "title": "Forget the church",
        "total_amount": 5,
        "currency": "ETB",
        "credit_account_number": ""
    }
    # Placeholder 64-byte private key
    priv_key = "bv2DsjDN/xvx1Jrpmx1SWNPcVW44lkvWnLgRNlWhKMTYXbpwY3e6OKA2f3e9DhwjdDJ5Pok2x0RTi3+Hx8IhjA=="
    
    sig = generate_signature(content, priv_key)
    print(f"✅ Python Ed25519 Signature: {sig}")