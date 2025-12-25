# Technical Reference Guide

## Cryptographic Algorithms Used

### Ed25519 Digital Signature Algorithm

**Type**: Asymmetric (Public Key)

**Use Case**: Sign data with private key, verify with public key

**Implementation Details**:
- **Algorithm**: EdDSA using Curve25519
- **Key Size**: 32-byte private key (seed), 32-byte public key
- **TweetNaCl Format**: 64-byte private key (32-byte seed + 32-byte public key)
- **Signature Size**: 64 bytes
- **Output Format**: Base64-encoded signature

**Language Implementations**:
- **Java**: Bouncy Castle `Ed25519Signer`
- **Python**: PyNaCl `SigningKey`
- **Go**: Standard library `crypto/ed25519`

**Advantages**:
- Fast signing and verification
- Small key and signature sizes
- Resistant to side-channel attacks
- No padding required

### HMAC-SHA256 Message Authentication Code

**Type**: Symmetric (Shared Secret)

**Use Case**: Sign and verify with same shared secret

**Implementation Details**:
- **Algorithm**: HMAC with SHA-256 hash function
- **Key Size**: Variable (typically 256+ bits)
- **Output Size**: 256 bits (32 bytes)
- **Output Format**: Hex-encoded signature

**Language Implementations**:
- **Java**: `javax.crypto.Mac`
- **Python**: `hmac.new()` with `hashlib.sha256`
- **Go**: `crypto/hmac` with `crypto/sha256`

**Advantages**:
- Simple and widely supported
- Both parties need same secret
- Efficient for high-volume signing

## Canonical JSON Format

### Why Canonical JSON?

Ensures identical JSON representation across all languages and platforms:
- Consistent key ordering
- No extra whitespace
- Deterministic byte representation
- Enables cross-language signature verification

### Canonical JSON Rules

1. **Keys must be sorted alphabetically**
   - Not insertion order
   - Lexicographic (ASCII) order
   - All keys at same level must be sorted

2. **No extra whitespace**
   - No spaces after colons: `"key":"value"` not `"key": "value"`
   - No spaces after commas: `"a","b"` not `"a", "b"`
   - No newlines or indentation

3. **Arrays preserved as-is**
   - Order matters in arrays
   - No special array formatting

4. **String escaping**
   - Must escape special characters
   - Must escape quotes and backslashes
   - Must use Unicode escapes for control characters

### Example

**Non-canonical JSON**:
```json
{
  "total_amount": 5,
  "app_code": "015489",
  "title": "Forget the church",
  "merchant_code": "MINIMRC-7914388979"
}
```

**Canonical JSON**:
```json
{"app_code":"015489","merchant_code":"MINIMRC-7914388979","title":"Forget the church","total_amount":5}
```

### Language Implementations

**Java**:
```java
ObjectMapper mapper = new ObjectMapper();
mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
String json = mapper.writeValueAsString(content);
```

**Python**:
```python
import canonicaljson
json_bytes = canonicaljson.encode_canonical_json(content)
```

**Go**:
```go
import "encoding/json"
jsonBytes, _ := json.Marshal(content)  // Go's Marshal auto-sorts keys
```

**Go (with library)**:
```go
import "github.com/gibson042/canonicaljson-go"
payload, _ := canonicaljson.Marshal(content)
```

## Environment Variable Specifications

### Configuration Variables

| Variable | Format | Example | Required |
|----------|--------|---------|----------|
| `ED25519_PRIVATE_KEY` | Base64 (64 bytes) | `bv2DsjDN/xvx1Jrpmx1SWNPcVW44lkvWnLgRNlWhKMTYXbpwY3e6OKA2f3e9DhwjdDJ5Pok2x0RTi3+Hx8IhjA==` | Yes (for Ed25519) |
| `ED25519_PUBLIC_KEY` | Base64 (32 bytes) | `2F23bY7w3f8b8dSa6Zsdcljb3FVuOJZL1py4ETZV4Sg=` | Yes (for verify) |
| `HMAC_SECRET` | String (UTF-8) | `yuTqIYiOwhTA+ssH8cPZBJ8DZT8fprRbTodpncAn3oseMPDLx256iNENhQREsdKnDrEXfGwR7n2moCDxOWpQTteq4NUiVNmU` | Yes (for HMAC) |

### Payload Variables

| Variable | Type | Example | Default |
|----------|------|---------|---------|
| `APP_CODE` | String | `015489` | "" |
| `MERCHANT_CODE` | String | `MINIMRC-7914388979` | "" |
| `MERCHANT_REFERENCE` | String | `txn-2345` | "" |
| `TITLE` | String | `"Forget the church"` | "" |
| `TOTAL_AMOUNT` | Integer | `5` | 0 |
| `CURRENCY` | String | `ETB` | "" |
| `CREDIT_ACCOUNT_NUMBER` | String | `` | "" |

### Output Variables (Set by Signers)

| Variable | Format | Example |
|----------|--------|---------|
| `SIGNATURE` | Base64 (Ed25519) or Hex (HMAC) | `MeLUtlmh6VrB/1NERjnVhh4IYnJNqfPOMkzIF/bSM3nn...` |
| `PAYLOAD` | JSON | `{"app_code":"015489","currency":"ETB",...}` |
| `SIGNATURE_TYPE` | String | `ed25519` or `hmac` |

## Key Generation Reference

### Generating Ed25519 Keys

**Go**:
```go
package main
import (
    "crypto/ed25519"
    "encoding/base64"
    "fmt"
)

func main() {
    _, privateKey, _ := ed25519.GenerateKey(nil)
    publicKey := privateKey.Public().(ed25519.PublicKey)
    
    fmt.Printf("ED25519_PRIVATE_KEY=%s\n", 
        base64.StdEncoding.EncodeToString(privateKey))
    fmt.Printf("ED25519_PUBLIC_KEY=%s\n", 
        base64.StdEncoding.EncodeToString(publicKey))
}
```

**Python**:
```python
from nacl.signing import SigningKey
import base64

signing_key = SigningKey.generate()
public_key = signing_key.verify_key

print(f"ED25519_PRIVATE_KEY={base64.b64encode(bytes(signing_key)).decode()}")
print(f"ED25519_PUBLIC_KEY={base64.b64encode(bytes(public_key)).decode()}")
```

**Java**:
```java
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator;
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import java.util.Base64;
import java.security.SecureRandom;

KeyPairGenerator gen = new Ed25519KeyPairGenerator();
gen.init(new Ed25519KeyGenerationParameters(new SecureRandom()));
AsymmetricCipherKeyPair keyPair = gen.generateKeyPair();

Ed25519PrivateKeyParameters privKey = 
    (Ed25519PrivateKeyParameters) keyPair.getPrivate();
Ed25519PublicKeyParameters pubKey = 
    (Ed25519PublicKeyParameters) keyPair.getPublic();

System.out.println("ED25519_PRIVATE_KEY=" + 
    Base64.getEncoder().encodeToString(privKey.getEncoded()));
System.out.println("ED25519_PUBLIC_KEY=" + 
    Base64.getEncoder().encodeToString(pubKey.getEncoded()));
```

### Generating HMAC Secrets

**Go**:
```go
import (
    "crypto/rand"
    "encoding/base64"
    "fmt"
)

func main() {
    secret := make([]byte, 64) // 512 bits
    rand.Read(secret)
    fmt.Printf("HMAC_SECRET=%s\n", base64.StdEncoding.EncodeToString(secret))
}
```

**Python**:
```python
import secrets
import base64

secret = secrets.token_bytes(64)  # 512 bits
print(f"HMAC_SECRET={base64.b64encode(secret).decode()}")
```

**OpenSSL**:
```bash
openssl rand -base64 64
```

## Testing Procedure

### Test 1: Same Language Ed25519
1. Run: `make java-sign`
2. Run: `make verify` 
3. Expected: ✅ Ed25519 signature verified successfully!

### Test 2: Cross-Language Verification
1. Run: `make java-sign` (sign with Java)
2. Note the SIGNATURE and PAYLOAD in output
3. Run: `make verify` (verify with Go)
4. Expected: ✅ Ed25519 signature verified successfully!
5. Repeat with: `make py-hmac && make verify`

### Test 3: Payload Change Detection
1. Run: `make java-sign`
2. Manually edit `.env` to change TITLE
3. Observe PAYLOAD changes in next signature
4. Run: `make py-sign` (with new payload)
5. Manual test: Modify SIGNATURE environment variable slightly
6. Run: `make verify`
7. Expected: ❌ Ed25519 signature verification failed!

### Test 4: HMAC Verification
1. Run: `make java-hmac`
2. Run: `make verify`
3. Expected: ✅ HMAC signature verified successfully!

## Debugging

### Enable Verbose Output

**Java**:
```bash
cd java
mvn exec:java -Dexec.mainClass="com.payment.security.Sign" -X
```

**Python**:
```bash
cd python
PYTHONPATH=src python3 -u src/payment_security/sign.py
```

**Go**:
```bash
cd go
go run -v main.go sign 2>&1
```

**Go Verifier**:
```bash
cd verifier-go
go run -v main.go
```

### Check Environment Variables

```bash
# Source the .env and check
source .env
echo "Private Key: ${ED25519_PRIVATE_KEY}"
echo "Public Key: ${ED25519_PUBLIC_KEY}"
echo "HMAC Secret: ${HMAC_SECRET}"
echo "Payload: APP_CODE=${APP_CODE}, TITLE=${TITLE}"
```

### Check Signature File

```bash
# On Unix/Linux/Mac
cat /tmp/signature_env.sh

# Shows current signature and payload saved by Makefile
```

### Validate JSON

```bash
# Check if JSON is valid
echo '{"app_code":"015489",...}' | python3 -m json.tool

# Pretty print canonical JSON
echo '{"app_code":"015489",...}' | python3 -m json.tool --no-ensure-ascii
```

## Performance Characteristics

### Ed25519
- **Signing**: ~0.5 ms per signature
- **Verification**: ~0.5 ms per verification
- **Key Size**: 64 bytes (private) + 32 bytes (public)
- **Signature Size**: 64 bytes

### HMAC-SHA256
- **Signing**: ~0.1 ms per signature  
- **Verification**: ~0.1 ms per verification
- **Key Size**: Variable (typically 256 bits / 32 bytes)
- **Signature Size**: 32 bytes

### Comparison

| Feature | Ed25519 | HMAC-SHA256 |
|---------|---------|------------|
| Algorithm Type | Asymmetric | Symmetric |
| Key Count | 2 (pub+priv) | 1 (shared) |
| Speed | Fast | Faster |
| Key Management | Complex | Simple |
| Key Distribution | Easy (pub key ok) | Hard (secret only) |
| Signature Size | 64 bytes | 32 bytes |

## Security Considerations

### Ed25519 Security
- ✅ 128-bit security level (equivalent to 256-bit symmetric key)
- ✅ No padding needed
- ✅ Deterministic (same input always produces same signature)
- ✅ Resistant to side-channel attacks
- ✅ No entropy required for signing (deterministic)

### HMAC-SHA256 Security
- ✅ 256-bit security if secret is cryptographically random
- ✅ Simple and well-analyzed
- ⚠️ Secret must be kept truly secret
- ⚠️ Requires secure key distribution
- ⚠️ Vulnerable to brute force if secret is weak

### General Best Practices
1. **Never share private keys** - Ed25519 only
2. **Use strong secrets** - HMAC secrets should be 256+ bits
3. **Rotate keys regularly** - Especially HMAC secrets
4. **Use HTTPS for distribution** - If keys transmitted over network
5. **Store in environment variables** - Not hardcoded
6. **Use canonical JSON** - Ensures signature reproducibility
7. **Verify signatures always** - Before trusting data

## Troubleshooting Guide

| Issue | Cause | Solution |
|-------|-------|----------|
| "ED25519_PRIVATE_KEY not set" | Missing .env or not sourced | Run from root dir, check .env exists |
| Key length error | Wrong key format | Generate correct 64-byte key |
| JSON parsing error | Non-canonical JSON | Use json library with sorting enabled |
| Signature mismatch | Payload changed | Ensure same payload in sign and verify |
| HMAC verification fails | Secret mismatch | Check HMAC_SECRET is same in sign & verify |
| Base64 decode error | Corrupted signature | Ensure SIGNATURE not modified |

---

For more information, see the other documentation files!
