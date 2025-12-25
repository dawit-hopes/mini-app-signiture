# Multi-Language Signature Testing System

This system allows you to test signing and verification across multiple languages (Java, Python, and Go) using both Ed25519 and HMAC-SHA256 algorithms.

## Setup

All environment variables (keys, secrets, and test payload) are defined in the `.env` file at the root directory.

### Configuration (.env)

Edit the `.env` file to customize keys and test payload:

```env
# Ed25519 Keys (TweetNaCl format - 64 bytes in base64)
ED25519_PRIVATE_KEY=bv2DsjDN/xvx1Jrpmx1SWNPcVW44lkvWnLgRNlWhKMTYXbpwY3e6OKA2f3e9DhwjdDJ5Pok2x0RTi3+Hx8IhjA==
ED25519_PUBLIC_KEY=2F23bY7w3f8b8dSa6Zsdcljb3FVuOJZL1py4ETZV4Sg=

# HMAC Secret
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

All keys and secrets are read from environment variables - **no hardcoding**!

## Usage

### From Root Directory

All commands should be run from the root folder:

```bash
cd /workspaces/mini-app-java
```

### Signing Commands

#### Java

```bash
make java-sign    # Sign with Java using Ed25519
make java-hmac    # Sign with Java using HMAC-SHA256
```

#### Python

```bash
make py-sign      # Sign with Python using Ed25519
make py-hmac      # Sign with Python using HMAC-SHA256
```

#### Go

```bash
make go-sign      # Sign with Go using Ed25519
make go-hmac      # Sign with Go using HMAC-SHA256
```

### Verification Commands

After running any signing command, verify the signature:

```bash
make verify       # Verify the last generated signature
```

Auto-detects the signature type (Ed25519 or HMAC) and uses the appropriate public key/secret.

### Complete Workflow Example

```bash
# Sign with Java Ed25519
make java-sign

# Console output shows:
# === Java Ed25519 Signer ===
# ✅ Generated Signature: MeLUtlmh6VrB/1NERjnVhh4IYnJNqfPOMkzIF/bSM3nn/d0/djAUzfpNftDFsYIsqt3nf9wn...
# export SIGNATURE=MeLUtlmh6VrB/1NERjnVhh4IYnJNqfPOMkzIF/bSM3nn/d0/djAUzfpNftDFsYIsqt3nf9wn...
# export PAYLOAD={"app_code":"015489",...}

# Verify the signature
make verify

# Console output shows:
# === Verifying Signature ===
# ✅ Ed25519 signature verified successfully!
```

## How It Works

### Signing Process

1. **Load Environment Variables**: Keys and payload data are read from `.env`
2. **Canonical JSON**: Payload is converted to canonical JSON (sorted keys, no extra whitespace)
3. **Sign**: Payload is signed using either:
   - **Ed25519**: Asymmetric signature (private key signs, public key verifies)
   - **HMAC-SHA256**: Symmetric signature (shared secret for both signing and verification)
4. **Output**: Signature and payload are printed to console and exported as environment variables:
   - `SIGNATURE`: The generated signature (Base64 for Ed25519, Hex for HMAC)
   - `PAYLOAD`: The canonical JSON payload
   - `SIGNATURE_TYPE`: Either "ed25519" or "hmac"

### Verification Process

1. **Read Environment**: Retrieves `SIGNATURE`, `PAYLOAD`, and `SIGNATURE_TYPE` from environment
2. **Decode Keys**: Decodes public key (Ed25519) or reads shared secret (HMAC)
3. **Verify**: Checks if signature matches the payload
4. **Output**: Success or failure message

## Project Structure

```
/workspaces/mini-app-java/
├── .env                          # Environment configuration (keys, secrets, payload)
├── makefile                      # Root makefile (orchestrates all commands)
├── go/                           # Go signing implementation
│   ├── main.go                  # Ed25519 & HMAC signing with env var support
│   ├── security/
│   │   ├── ed25519_signer.go
│   │   └── hmac_signer.go
│   └── makefile
├── java/                         # Java signing implementation
│   ├── src/main/java/com/payment/security/
│   │   ├── Sign.java            # Ed25519 signing with env var support
│   │   └── HmacSigner.java      # HMAC signing with env var support
│   ├── pom.xml
│   └── makefile
├── python/                       # Python signing implementation
│   ├── src/payment_security/
│   │   ├── sign.py              # Ed25519 signing with env var support
│   │   └── hmac_signer.py       # HMAC signing with env var support
│   ├── requirements.txt
│   └── makefile
└── verifier-go/                  # Go verification implementation
    ├── main.go                  # Reads from env and verifies signatures
    └── go.mod
```

## Available Make Targets

```
make help         # Show this help message
make java-sign    # Java Ed25519 signing
make java-hmac    # Java HMAC signing
make py-sign      # Python Ed25519 signing
make py-hmac      # Python HMAC signing
make go-sign      # Go Ed25519 signing
make go-hmac      # Go HMAC signing
make verify       # Verify the last signature
```

## Implementation Details

### Environment Variable Export

Each language implementation:
- Reads keys/secrets from environment variables
- Reads payload parameters from environment variables
- Outputs `export` statements for `SIGNATURE`, `PAYLOAD`, and `SIGNATURE_TYPE`

This allows signatures to be passed to the verifier automatically.

### Canonical JSON

All implementations use canonical JSON representation:
- Keys sorted alphabetically
- No extra whitespace
- Consistent across all languages

This ensures signatures generated in one language can be verified in another.

### Signature Formats

- **Ed25519**: Base64-encoded signature
- **HMAC-SHA256**: Hex-encoded signature

## Cross-Language Verification

You can sign with one language and verify with the verifier:

```bash
# Sign with Java
make java-sign

# Verify with Go verifier (verifier-go)
make verify

# Try another language
make py-hmac
make verify

# And another
make go-sign
make verify
```

All combinations work because they use:
- Same canonical JSON format
- Same key/secret data
- Same cryptographic algorithms

## Troubleshooting

### Error: "Not set in environment"

Ensure `.env` file exists in the root directory and contains the required variables.

### Error during Java compilation

Ensure Maven is installed:
```bash
mvn --version
```

### Error during Python execution

Ensure virtualenv is created:
```bash
cd python && make install
```

### Error during Go execution

Ensure Go modules are initialized:
```bash
cd go && go mod tidy
cd verifier-go && go mod tidy
```

## Notes

- All cryptographic keys are loaded from environment variables - never hardcoded
- Each signing command also prints the export statements, so you can manually source them if needed
- The verifier automatically detects whether to verify Ed25519 or HMAC based on the `SIGNATURE_TYPE` variable
- Test payload can be modified in `.env` to test different data
