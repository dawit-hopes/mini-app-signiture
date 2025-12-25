# Visual Guide - Multi-Language Signing System

## System Architecture

```
┌────────────────────────────────────────────────────────────────┐
│                         .env Configuration                     │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │ ED25519_PRIVATE_KEY = "..."                              │ │
│  │ ED25519_PUBLIC_KEY = "..."                               │ │
│  │ HMAC_SECRET = "..."                                      │ │
│  │ APP_CODE = "015489"                                      │ │
│  │ MERCHANT_CODE = "MINIMRC-7914388979"                     │ │
│  │ ... (other test payload)                                 │ │
│  └──────────────────────────────────────────────────────────┘ │
└────────────┬─────────────────────────────────────────────────┘
             │
             │ (Loaded by make targets)
             │
    ┌────────┴─────────┬──────────────────┬─────────────────┐
    │                  │                  │                 │
    v                  v                  v                 v
┌──────────┐     ┌──────────┐      ┌──────────┐      ┌────────┐
│   JAVA   │     │  PYTHON  │      │    GO    │      │ VERIFY │
│          │     │          │      │          │      │        │
│ (Java    │     │ (Python  │      │ (Go 1.16)│      │(Go)    │
│  11+)    │     │  3.7+)   │      │   +)     │      │        │
└──────────┘     └──────────┘      └──────────┘      └────────┘
    │                  │                  │                │
    │ (Sign)           │ (Sign)           │ (Sign)         │
    │                  │                  │                │
    └────────┬─────────┴──────────────────┴────────────────┘
             │
             │ (Output export statements)
             │
             v
    ┌────────────────────────────────┐
    │  SIGNATURE & PAYLOAD in ENV    │
    │  ┌──────────────────────────┐  │
    │  │ export SIGNATURE="..."   │  │
    │  │ export PAYLOAD='{...}'   │  │
    │  │ export SIGNATURE_TYPE="# │  │
    │  └──────────────────────────┘  │
    └────────────┬───────────────────┘
                 │
                 │ (Passed to)
                 │
                 v
          ┌─────────────┐
          │  Verifier   │
          │  (Go)       │
          │             │
          │ ✅ or ❌   │
          └─────────────┘
```

## Command Flow Diagram

### Signing Flow

```
User runs: make java-sign
    │
    ├─► Makefile loads .env
    │
    ├─► Calls: cd java && make sign
    │
    ├─► Java Makefile: mvn clean package
    │
    ├─► Runs: mvn exec:java -Dexec.mainClass="com.payment.security.Sign"
    │
    ├─► Sign.java:
    │   ├─ Read ED25519_PRIVATE_KEY from environment
    │   ├─ Read payload (APP_CODE, etc.) from environment
    │   ├─ Create canonical JSON: {"app_code":"...","currency":"...",...}
    │   ├─ Sign with Ed25519
    │   └─ Output to console:
    │       export SIGNATURE="base64..."
    │       export PAYLOAD='{"..."}'
    │
    └─► Makefile saves to temp file for verification
        (for use by 'make verify')
```

### Verification Flow

```
User runs: make verify
    │
    ├─► Makefile checks if temp signature file exists
    │
    ├─► Makefile sources temp file into environment:
    │   ├─ SIGNATURE="..."
    │   ├─ PAYLOAD="..."
    │   └─ SIGNATURE_TYPE="ed25519"
    │
    ├─► Calls: cd verifier-go && go run main.go
    │
    ├─► verifier-go/main.go:
    │   ├─ Read SIGNATURE from environment
    │   ├─ Read PAYLOAD from environment
    │   ├─ Read SIGNATURE_TYPE from environment
    │   ├─ Read ED25519_PUBLIC_KEY or HMAC_SECRET from environment
    │   ├─ Verify: crypto.ed25519.Verify(payload, sig, pubkey)
    │   └─ Output:
    │       ✅ Ed25519 signature verified successfully!
    │       or
    │       ❌ HMAC signature verification failed!
    │
    └─► Exit with success (0) or failure (1)
```

## Language Integration Points

```
┌─────────────────────────────────────────────────────────────┐
│                    Java Implementation                      │
├─────────────────────────────────────────────────────────────┤
│ INPUT:                                                      │
│  └─ System.getenv("ED25519_PRIVATE_KEY")                   │
│  └─ System.getenv("APP_CODE"), System.getenv("TITLE"), ... │
│                                                             │
│ PROCESS:                                                    │
│  └─ ObjectMapper.configure(ORDER_MAP_ENTRIES_BY_KEYS) ──┐  │
│     Ed25519Signer.generateSignature()                     │  │
│     Base64.getEncoder().encodeToString()                  │  │
│                                                             │
│ OUTPUT:                                                     │
│  └─ System.out.println("export SIGNATURE=" + sig)          │
│  └─ System.out.println("export PAYLOAD=" + json)           │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                   Python Implementation                     │
├─────────────────────────────────────────────────────────────┤
│ INPUT:                                                      │
│  └─ os.getenv("ED25519_PRIVATE_KEY")                       │
│  └─ os.getenv("APP_CODE"), os.getenv("TITLE"), ...         │
│                                                             │
│ PROCESS:                                                    │
│  └─ canonicaljson.encode_canonical_json()              ──┐ │
│     nacl.signing.SigningKey.sign()                       │ │
│     base64.b64encode()                                   │ │
│                                                             │
│ OUTPUT:                                                     │
│  └─ print(f"export SIGNATURE={sig}")                       │
│  └─ print(f"export PAYLOAD='{json_payload}'")              │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                     Go Implementation                       │
├─────────────────────────────────────────────────────────────┤
│ INPUT:                                                      │
│  └─ os.Getenv("ED25519_PRIVATE_KEY")                       │
│  └─ os.Getenv("APP_CODE"), os.Getenv("TITLE"), ...         │
│  └─ os.Args[1] for "sign" or "hmac"                        │
│                                                             │
│ PROCESS:                                                    │
│  └─ json.Marshal() (auto-sorts keys)                  ──┐  │
│     crypto.ed25519.Sign()                               │  │
│     base64.StdEncoding.EncodeToString()                 │  │
│                                                             │
│ OUTPUT:                                                     │
│  └─ fmt.Printf("export SIGNATURE=%s\n", sig)              │
│  └─ fmt.Printf("export PAYLOAD='%s'\n", jsonString)       │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                 Go Verifier Implementation                  │
├─────────────────────────────────────────────────────────────┤
│ INPUT:                                                      │
│  └─ os.Getenv("SIGNATURE")                                 │
│  └─ os.Getenv("PAYLOAD")                                   │
│  └─ os.Getenv("SIGNATURE_TYPE") -> "ed25519" or "hmac"    │
│  └─ os.Getenv("ED25519_PUBLIC_KEY") for Ed25519            │
│  └─ os.Getenv("HMAC_SECRET") for HMAC                      │
│                                                             │
│ PROCESS:                                                    │
│  ├─ IF SIGNATURE_TYPE == "ed25519":                    ──┐ │
│  │   base64.StdEncoding.DecodeString(sig)                │ │
│  │   ed25519.Verify(publicKey, payload, sig)            │ │
│  │                                                         │ │
│  └─ IF SIGNATURE_TYPE == "hmac":                       ──┐ │
│      hmac.New(sha256.New, secret).Write(payload)        │ │
│      computed := hex.EncodeToString(h.Sum(nil))         │ │
│      compare: computed == sig                           │ │
│                                                             │
│ OUTPUT:                                                     │
│  └─ fmt.Printf("✅ [Type] signature verified successfully!") │
│  └─ exit(0) on success, exit(1) on failure                 │
└─────────────────────────────────────────────────────────────┘
```

## Data Flow Example: Java Ed25519 Sign → Verify

```
Step 1: User Command
┌─────────────────┐
│ make java-sign  │
└────────┬────────┘
         │
Step 2: Load Configuration from .env
┌──────────────────────────────────────────────────┐
│ ED25519_PRIVATE_KEY = "bv2DsjDN/xvx..."         │
│ TITLE = "Forget the church"                      │
│ MERCHANT_CODE = "MINIMRC-7914388979"             │
│ ... (other fields)                               │
└────────┬─────────────────────────────────────────┘
         │
Step 3: Build Payload in Java
┌──────────────────────────────────────────────────┐
│ Map<String, Object> content = new HashMap<>();   │
│ content.put("app_code", "015489");               │
│ content.put("merchant_code", "MINIMRC-...");     │
│ content.put("title", "Forget the church");       │
│ ... (sorted alphabetically)                      │
└────────┬─────────────────────────────────────────┘
         │
Step 4: Canonical JSON (Sorted)
┌──────────────────────────────────────────────────┐
│ {                                                 │
│   "app_code":"015489",                          │
│   "credit_account_number":"",                   │
│   "currency":"ETB",                             │
│   "merchant_code":"MINIMRC-7914388979",         │
│   "merchant_reference":"txn-2345",              │
│   "title":"Forget the church",                  │
│   "total_amount":5                              │
│ }                                                │
└────────┬─────────────────────────────────────────┘
         │
Step 5: Sign with Ed25519
┌──────────────────────────────────────────────────┐
│ byte[] signature = signer.generateSignature();   │
│ Result:                                          │
│ MeLUtlmh6VrB/1NERjnVhh4IYnJNqfPOMkzIF/bSM3nn... │
└────────┬─────────────────────────────────────────┘
         │
Step 6: Output Export Statements
┌──────────────────────────────────────────────────┐
│ export SIGNATURE="MeLUtlmh6VrB/1NERjnVhh4IYnJNq" │
│ export PAYLOAD='{"app_code":"015489",...}'       │
│ export SIGNATURE_TYPE=ed25519                    │
└────────┬─────────────────────────────────────────┘
         │
Step 7: Makefile Saves to Temp
┌──────────────────────────────────────────────────┐
│ File: /tmp/signature_env.sh                      │
│ Content: (same as export statements above)       │
└────────┬─────────────────────────────────────────┘
         │
Step 8: User Command
┌─────────────────┐
│ make verify     │
└────────┬────────┘
         │
Step 9: Source Signature from Temp File
┌──────────────────────────────────────────────────┐
│ source /tmp/signature_env.sh                     │
│ Now environment contains:                        │
│ SIGNATURE = "MeLUtlmh6VrB/1NERjnVhh4IYnJNq..."   │
│ PAYLOAD = '{"app_code":"015489",...}'            │
│ SIGNATURE_TYPE = "ed25519"                       │
└────────┬─────────────────────────────────────────┘
         │
Step 10: Run Go Verifier
┌──────────────────────────────────────────────────┐
│ go run verifier-go/main.go                       │
│ Reads all above from environment                 │
└────────┬─────────────────────────────────────────┘
         │
Step 11: Verify
┌──────────────────────────────────────────────────┐
│ 1. Decode public key from ED25519_PUBLIC_KEY     │
│ 2. Decode signature from SIGNATURE (base64)      │
│ 3. Verify: ed25519.Verify(pubkey, payload, sig) │
│ 4. Result: true                                  │
└────────┬─────────────────────────────────────────┘
         │
Step 12: Output Result
┌──────────────────────────────────────────────────┐
│ ✅ Ed25519 signature verified successfully!      │
│                                                  │
│ Exit code: 0 (success)                          │
└──────────────────────────────────────────────────┘
```

## File Organization

```
mini-app-java/
│
├── Root Level (Configuration & Orchestration)
│   ├── .env                          ← All keys, secrets, payload
│   ├── makefile                      ← Main orchestration
│   │
│   ├── Documentation
│   │   ├── QUICKSTART.md             ← 5-min guide
│   │   ├── TESTING.md                ← Full testing guide
│   │   ├── IMPLEMENTATION.md         ← Technical details
│   │   ├── SUMMARY.md                ← Implementation summary
│   │   └── VERIFICATION_CHECKLIST.md ← This document
│   │
│   └── Helper Scripts
│       ├── sign.sh                   ← Signing helper
│       └── test-all.sh               ← Test all implementations
│
├── Java Module
│   ├── makefile                      ← Java build targets
│   ├── pom.xml                       ← Maven configuration
│   └── src/main/java/com/payment/security/
│       ├── Sign.java                 ← Ed25519 (env-aware)
│       └── HmacSigner.java           ← HMAC-SHA256 (env-aware)
│
├── Python Module
│   ├── makefile                      ← Python targets
│   ├── requirements.txt              ← Dependencies
│   └── src/payment_security/
│       ├── __init__.py
│       ├── sign.py                   ← Ed25519 (env-aware)
│       └── hmac_signer.py            ← HMAC-SHA256 (env-aware)
│
├── Go Module
│   ├── go.mod                        ← Go module definition
│   ├── main.go                       ← Ed25519 & HMAC (env-aware)
│   └── security/
│       ├── ed25519_signer.go         ← Ed25519 logic
│       └── hmac_signer.go            ← HMAC logic
│
└── Go Verifier Module
    ├── go.mod                        ← Go module definition
    └── main.go                       ← Verification logic (env-aware)
```

## Command Reference Quick Card

```
┌──────────────────────────────────────┐
│  SIGNING COMMANDS                    │
├──────────────────────────────────────┤
│ make java-sign     → Java Ed25519    │
│ make java-hmac     → Java HMAC       │
│ make py-sign       → Python Ed25519  │
│ make py-hmac       → Python HMAC     │
│ make go-sign       → Go Ed25519      │
│ make go-hmac       → Go HMAC         │
└──────────────────────────────────────┘

┌──────────────────────────────────────┐
│  VERIFICATION COMMANDS               │
├──────────────────────────────────────┤
│ make verify        → Verify last sig │
│ make help          → Show help       │
└──────────────────────────────────────┘

┌──────────────────────────────────────┐
│  TYPICAL WORKFLOW                    │
├──────────────────────────────────────┤
│ 1. make java-sign                   │
│ 2. make verify                      │
│ 3. make py-hmac                     │
│ 4. make verify                      │
│ 5. make go-sign                     │
│ 6. make verify                      │
└──────────────────────────────────────┘
```

---

For more details, see the other documentation files!
