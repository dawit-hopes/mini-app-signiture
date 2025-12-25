# Quick Start Guide

## Prerequisites
- Java 11+ (for Java implementations)
- Maven (for Java builds)
- Python 3.7+ (for Python implementations)
- Go 1.16+ (for Go implementations)

## 5-Minute Quick Start

### 1. Navigate to Project Root
```bash
cd /workspaces/mini-app-java
```

### 2. View Configuration
```bash
cat .env
```

### 3. Sign with Java (Ed25519)
```bash
make java-sign
```
You'll see output like:
```
=== Java Ed25519 Signer ===
✅ Generated Signature: MeLUtlmh6VrB/1NERjnVhh4IYnJNqfPOMkzIF/bSM3nn...
export SIGNATURE=MeLUtlmh6VrB/1NERjnVhh4IYnJNqfPOMkzIF/bSM3nn...
export PAYLOAD={"app_code":"015489","credit_account_number":"","currency":"ETB",...}
```

### 4. Verify the Signature
```bash
make verify
```
You'll see:
```
=== Verifying Signature ===
✅ Ed25519 signature verified successfully!
```

### 5. Try Other Languages
```bash
make java-hmac    # Java HMAC
make verify

make py-sign      # Python Ed25519
make verify

make py-hmac      # Python HMAC
make verify

make go-sign      # Go Ed25519
make verify

make go-hmac      # Go HMAC
make verify
```

## All Available Commands

```bash
make help         # Show help
make java-sign    # Java Ed25519
make java-hmac    # Java HMAC
make py-sign      # Python Ed25519
make py-hmac      # Python HMAC
make go-sign      # Go Ed25519
make go-hmac      # Go HMAC
make verify       # Verify last signature
```

## How It Works (Under the Hood)

1. **Signing**: Your language choice reads keys from `.env`, signs the payload, prints the signature
2. **Environment**: Signature & payload are exported as environment variables
3. **Verification**: Go verifier reads signature & payload from environment, verifies them
4. **Output**: Console shows ✅ success or ❌ failure

## Configuration

Edit `.env` to change:
- **Keys**: `ED25519_PRIVATE_KEY`, `ED25519_PUBLIC_KEY`, `HMAC_SECRET`
- **Payload**: `APP_CODE`, `MERCHANT_CODE`, `TITLE`, etc.

All variables are read from `.env` - **no code changes needed**!

## Testing Across Languages

```bash
# Sign with Java, verify
make java-sign
make verify

# Sign with Python, verify  
make py-sign
make verify

# Sign with Go, verify
make go-sign
make verify
```

All produce valid signatures because they use:
- Same canonical JSON format
- Same cryptographic keys
- Same algorithms

## Documentation

- `TESTING.md` - Complete testing guide with examples
- `IMPLEMENTATION.md` - Technical implementation details
- `README.md` - Main project documentation

## Common Tasks

### Change the Test Payload
```bash
# Edit .env
nano .env
# Change APP_CODE, MERCHANT_CODE, TITLE, etc.

# Then sign
make java-sign
make verify
```

### Use Your Own Keys
```bash
# Generate new keys (in Go)
cd go && go run main.go
# This shows how to generate fresh keys

# Update .env with your keys
nano .env
```

### Run All Tests
```bash
bash test-all.sh
```

## Troubleshooting

**Java signing fails:**
```bash
# Ensure Java is installed
java -version

# Ensure Maven is installed
mvn -version

# Rebuild
cd java && mvn clean package
```

**Python signing fails:**
```bash
# Ensure virtual environment is set up
cd python && make install

# Then try again
make py-sign
```

**Go signing fails:**
```bash
# Download dependencies
cd go && go mod tidy
cd ../verifier-go && go mod tidy

# Then try again
make go-sign
```

**Verification fails:**
```bash
# Make sure you ran a signing command first
make java-sign
# Then verify
make verify
```

## Next Steps

1. ✅ Try `make java-sign && make verify`
2. ✅ Try other languages: `make py-sign`, `make go-hmac`, etc.
3. ✅ Edit `.env` and test with different payloads
4. ✅ Review `TESTING.md` for advanced usage
5. ✅ Review `IMPLEMENTATION.md` for technical details

Happy signing! 🚀
