# Multi-Language Cryptographic Signing & Verification System

> Complete implementation for signing and verifying data across Java, Python, and Go using Ed25519 and HMAC-SHA256 algorithms.

## 🚀 Quick Start (5 Minutes)

```bash
# Navigate to root
cd /workspaces/mini-app-java

# Sign with Java
make java-sign

# Verify
make verify

# Try other languages
make py-hmac && make verify
make go-sign && make verify
```

See **[QUICKSTART.md](QUICKSTART.md)** for more examples.

## 📚 Documentation

### For Users
- **[QUICKSTART.md](QUICKSTART.md)** - 5-minute getting started guide
- **[TESTING.md](TESTING.md)** - Complete testing guide with examples
- **[SUMMARY.md](SUMMARY.md)** - Implementation overview

### For Developers
- **[IMPLEMENTATION.md](IMPLEMENTATION.md)** - What was implemented and how
- **[TECHNICAL_REFERENCE.md](TECHNICAL_REFERENCE.md)** - Crypto algorithms, key generation, debugging
- **[VISUAL_GUIDE.md](VISUAL_GUIDE.md)** - Architecture diagrams and flow charts
- **[VERIFICATION_CHECKLIST.md](VERIFICATION_CHECKLIST.md)** - Complete feature checklist

## ✨ Key Features

### Multi-Language Support
- ✅ **Java** - Ed25519 & HMAC signing with Maven
- ✅ **Python** - Ed25519 & HMAC signing with virtual environment
- ✅ **Go** - Ed25519 & HMAC signing with command-line arguments
- ✅ **Go** - Verification that auto-detects algorithm type

### Cryptographic Algorithms
- ✅ **Ed25519** - Asymmetric digital signatures (fast, small keys)
- ✅ **HMAC-SHA256** - Symmetric message authentication (simple, efficient)

### Features
- ✅ Environment-based configuration (no hardcoding)
- ✅ Canonical JSON format (same across all languages)
- ✅ Automatic signature export to environment
- ✅ Cross-language verification
- ✅ Simple Make-based orchestration
- ✅ Comprehensive error handling

## 🎯 Available Commands

```bash
# Signing Commands
make java-sign              # Java Ed25519
make java-hmac              # Java HMAC-SHA256
make py-sign                # Python Ed25519
make py-hmac                # Python HMAC-SHA256
make go-sign                # Go Ed25519
make go-hmac                # Go HMAC-SHA256

# Verification
make verify                 # Verify last signature (auto-detects type)

# Help
make help                   # Show all commands
```

## 🏗️ Architecture

```
Configuration (.env)
    ↓
Signing (Java/Python/Go)
    ├─ Read keys from environment
    ├─ Read payload from environment
    ├─ Sign with selected algorithm
    └─ Export SIGNATURE & PAYLOAD to environment
         ↓
    Verification (Go)
         ├─ Read SIGNATURE & PAYLOAD from environment
         ├─ Read keys from environment
         ├─ Auto-detect algorithm type
         └─ Verify & output result
```

## 📂 Project Structure

```
mini-app-java/
├── .env                          ← Configuration (keys, secrets, payload)
├── makefile                      ← Main orchestration
│
├── java/
│   ├── src/main/java/com/payment/security/
│   │   ├── Sign.java             ← Ed25519 (reads from env)
│   │   └── HmacSigner.java       ← HMAC-SHA256 (reads from env)
│   ├── pom.xml
│   └── makefile
│
├── python/
│   ├── src/payment_security/
│   │   ├── sign.py               ← Ed25519 (reads from env)
│   │   └── hmac_signer.py        ← HMAC-SHA256 (reads from env)
│   ├── requirements.txt
│   └── makefile
│
├── go/
│   ├── main.go                   ← Both algorithms (reads from env)
│   ├── security/
│   │   ├── ed25519_signer.go
│   │   └── hmac_signer.go
│   └── go.mod
│
└── verifier-go/
    ├── main.go                   ← Verification (reads from env)
    └── go.mod
```

## ⚙️ Configuration

Edit `.env` to configure:

```env
# Cryptographic Keys
ED25519_PRIVATE_KEY=bv2DsjDN/xvx1Jrpmx1SWNPcVW44lkvWnLgRNlWhKMTYXbpwY3e6OKA2f3e9DhwjdDJ5Pok2x0RTi3+Hx8IhjA==
ED25519_PUBLIC_KEY=2F23bY7w3f8b8dSa6Zsdcljb3FVuOJZL1py4ETZV4Sg=
HMAC_SECRET=yuTqIYiOwhTA+ssH8cPZBJ8DZT8fprRbTodpncAn3oseMPDLx256iNENhQREsdKnDrEXfGwR7n2moCDxOWpQTteq4NUiVNmU

# Test Payload
APP_CODE=015489
MERCHANT_CODE=MINIMRC-7914388979
MERCHANT_REFERENCE=txn-2345
TITLE="Forget the church"
TOTAL_AMOUNT=5
CURRENCY=ETB
CREDIT_ACCOUNT_NUMBER=""
```

All values are read from environment variables - **no hardcoding** in code!

## 📊 How It Works

### Signing Process
1. Read keys and payload from `.env`
2. Create canonical JSON (sorted keys, no whitespace)
3. Sign with selected algorithm
4. Output signature and export statements
5. Signature saved to environment for verifier

### Verification Process
1. Read signature and payload from environment
2. Auto-detect algorithm type (Ed25519 or HMAC)
3. Read appropriate key/secret from environment
4. Verify signature matches payload
5. Output success or failure

## 🔐 Security Features

- ✅ **No hardcoded keys** - All keys from environment
- ✅ **Canonical JSON** - Same format across languages
- ✅ **Strong algorithms** - Ed25519 (asymmetric) & HMAC-SHA256 (symmetric)
- ✅ **Clear error messages** - For debugging and validation
- ✅ **Deterministic** - Same input always produces same signature

## 📖 Documentation Map

```
START HERE
    ↓
┌─────────────────────────────────┐
│ QUICKSTART.md (5 min read)     │
│ Try: make java-sign && verify   │
└─────────────────────────────────┘
    ↓
┌─────────────────────────────────┐
│ TESTING.md (detailed guide)     │
│ Learn all commands & workflows   │
└─────────────────────────────────┘
    ↓ (if you want to understand the system)
┌──────────────────────────────────────────┐
│ VISUAL_GUIDE.md (architecture, diagrams) │
│ See how components interact              │
└──────────────────────────────────────────┘
    ↓ (if you want technical details)
┌──────────────────────────────────────────┐
│ TECHNICAL_REFERENCE.md (algorithms, etc) │
│ Crypto details, key generation, debugging│
└──────────────────────────────────────────┘
    ↓ (if you want implementation details)
┌──────────────────────────────────────────┐
│ IMPLEMENTATION.md (what was implemented) │
│ File changes, features added              │
└──────────────────────────────────────────┘
```

## 🧪 Testing Examples

### Test 1: Sign and Verify with Same Language
```bash
make java-sign
make verify
# Output: ✅ Ed25519 signature verified successfully!
```

### Test 2: Cross-Language Verification
```bash
make java-sign    # Sign with Java
make verify       # Verify with Go
# Output: ✅ Ed25519 signature verified successfully!

make py-hmac      # Sign with Python
make verify       # Verify with Go
# Output: ✅ HMAC signature verified successfully!
```

### Test 3: Change Payload
```bash
# Edit .env to change TITLE
nano .env
# Update TITLE="New title"

make go-sign      # Sign with new payload
make verify       # Verify
# Output: ✅ Ed25519 signature verified successfully!
```

## 🔍 Example Output

### Signing
```
=== Java Ed25519 Signer ===
--- Input Content ---
{app_code=015489, merchant_code=MINIMRC-7914388979, ...}
---------------------
JSON String to Sign: {"app_code":"015489","credit_account_number":"","currency":"ETB","merchant_code":"MINIMRC-7914388979","merchant_reference":"txn-2345","title":"Forget the church","total_amount":5}
---------------------
✅ Generated Signature: MeLUtlmh6VrB/1NERjnVhh4IYnJNqfPOMkzIF/bSM3nn/d0/djAUzfpNftDFsYIsqt3nf9wnfHg23kCxbViYBg==
export SIGNATURE=MeLUtlmh6VrB/1NERjnVhh4IYnJNqfPOMkzIF/bSM3nn/d0/djAUzfpNftDFsYIsqt3nf9wnfHg23kCxbViYBg==
export PAYLOAD={"app_code":"015489","credit_account_number":"","currency":"ETB","merchant_code":"MINIMRC-7914388979","merchant_reference":"txn-2345","title":"Forget the church","total_amount":5}
```

### Verification
```
=== Verifying Signature ===
Verifying ed25519 signature...
Signature: MeLUtlmh6VrB/1NERjnVhh4IYnJNqfPOMkzIF/bSM3nn...
Payload: {"app_code":"015489",...}
✅ Ed25519 signature verified successfully!
```

## ⚡ Requirements

- **Java 11+** - For Java implementations
- **Maven** - For Java builds
- **Python 3.7+** - For Python implementations
- **Go 1.16+** - For Go implementations
- **Make** - For orchestration

All are pre-installed in the development environment.

## 📝 Getting Started Checklist

- [ ] Read `QUICKSTART.md` (5 min)
- [ ] Run `make java-sign && make verify`
- [ ] Try `make py-hmac && make verify`
- [ ] Try `make go-sign && make verify`
- [ ] Read `TESTING.md` for more examples
- [ ] Edit `.env` to customize keys/payload
- [ ] Read `TECHNICAL_REFERENCE.md` for details

## 🎓 Learn More

### About Ed25519
- IETF RFC 8032: Edwards-Curve Digital Signature Algorithm
- Fast, secure, deterministic signatures
- No padding required

### About HMAC-SHA256
- IETF RFC 2104: HMAC Message Authentication Code
- Symmetric algorithm (shared secret)
- Fast and efficient

### About Canonical JSON
- RFC 8785: JSON Canonicalization Scheme (JCS)
- Ensures same JSON representation across platforms
- Critical for cross-language signatures

## 💡 Tips

1. **Always source .env before running commands**
   ```bash
   source .env
   make java-sign
   ```

2. **Check current environment variables**
   ```bash
   echo "SIGNATURE_TYPE: ${SIGNATURE_TYPE}"
   echo "SIGNATURE length: ${#SIGNATURE}"
   ```

3. **Generate new keys**
   - See `TECHNICAL_REFERENCE.md` for key generation code

4. **Debug signing failures**
   - Check `.env` has correct keys
   - Check all required variables are set
   - Check payload fields are not null

5. **Test cross-language compatibility**
   - Sign with each language
   - Always verify with same verification command
   - Results should all be valid

## 🐛 Troubleshooting

### Common Issues

**"ED25519_PRIVATE_KEY not set"**
- Solution: Ensure you're in root directory and `.env` exists
  ```bash
  cd /workspaces/mini-app-java
  cat .env | head -3
  ```

**"Maven not found"**
- Solution: Install Java and Maven
  ```bash
  java -version
  mvn -version
  ```

**"Signature verification failed"**
- Solution: Ensure same payload for sign & verify
  ```bash
  # Check .env hasn't changed since signing
  cat .env
  ```

For more troubleshooting, see `TECHNICAL_REFERENCE.md`.

## 📞 Support

- See documentation in this directory
- Check `.env` configuration
- Review Make targets with `make help`
- Read error messages carefully

---

**Ready to get started?** Start with [QUICKSTART.md](QUICKSTART.md)! 🚀
