#!/bin/bash

# Multi-Language Signature Testing Helper Script
# This script manages environment variables across signing and verification processes

set -e

# Get the script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Source the .env file
if [ -f .env ]; then
    set -a  # Mark all variables as exported
    source .env
    set +a  # Stop marking variables as exported
fi

# Signature storage file
SIG_FILE="${TMPDIR:-.}/signature_env.sh"

# Function to extract and save signature/payload from output
save_signature() {
    local output="$1"
    local sig_type="$2"
    
    local sig=$(echo "$output" | grep "^export SIGNATURE=" | head -1 || true)
    local payload=$(echo "$output" | grep "^export PAYLOAD=" | head -1 || true)
    
    if [ -z "$sig" ] || [ -z "$payload" ]; then
        echo "❌ Error: Could not extract signature or payload from output" >&2
        echo "Output was:" >&2
        echo "$output" >&2
        return 1
    fi
    
    {
        echo "$sig"
        echo "$payload"
        echo "export SIGNATURE_TYPE=$sig_type"
    } > "$SIG_FILE"
    
    # Also print to stdout for visibility
    echo "$sig"
    echo "$payload"
    echo "✅ Signature saved"
    echo "export SIGNATURE_TYPE=$sig_type"
    
    return 0
}

# Function to run signing
run_signer() {
    local signer=$1
    
    echo "=== $signer Signing ===" >&2
    
    case "$signer" in
        "java-sign")
            output=$(cd java && make sign 2>&1)
            echo "$output" | grep -v "^\[" >&2
            extracted=$(echo "$output" | grep -E "^export|✅" || true)
            save_signature "$extracted" "ed25519" || return 1
            ;;
        "java-hmac")
            output=$(cd java && make hmac 2>&1)
            echo "$output" | grep -v "^\[" >&2
            extracted=$(echo "$output" | grep -E "^export|HMAC" || true)
            save_signature "$extracted" "hmac" || return 1
            ;;
        "py-sign")
            output=$(cd python && make sign 2>&1)
            echo "$output" >&2
            extracted=$(echo "$output" | grep -E "^export|✅" || true)
            save_signature "$extracted" "ed25519" || return 1
            ;;
        "py-hmac")
            output=$(cd python && make hmac 2>&1)
            echo "$output" >&2
            extracted=$(echo "$output" | grep -E "^export|HMAC" || true)
            save_signature "$extracted" "hmac" || return 1
            ;;
        "go-sign")
            output=$(cd go && go run main.go sign 2>&1)
            echo "$output" >&2
            extracted=$(echo "$output" | grep -E "^export|✅" || true)
            save_signature "$extracted" "ed25519" || return 1
            ;;
        "go-hmac")
            output=$(cd go && go run main.go hmac 2>&1)
            echo "$output" >&2
            extracted=$(echo "$output" | grep -E "^export|✅" || true)
            save_signature "$extracted" "hmac" || return 1
            ;;
        *)
            echo "Unknown signer: $signer" >&2
            return 1
            ;;
    esac
}

# Function to run verification
run_verify() {
    echo "=== Verifying Signature ===" >&2
    
    if [ ! -f "$SIG_FILE" ]; then
        echo "❌ Error: No signature available. Run a signing command first" >&2
        return 1
    fi
    
    # Source the signature environment
    set -a
    source "$SIG_FILE"
    set +a
    
    # Run verifier with the signature and payload from environment
    cd verifier-go && go run main.go
}

# Main
case "$1" in
    "java-sign"|"java-hmac"|"py-sign"|"py-hmac"|"go-sign"|"go-hmac")
        run_signer "$1" || exit 1
        ;;
    "verify")
        run_verify || exit 1
        ;;
    "help")
        cat <<EOF
Multi-Language Signature Testing

Usage: $0 [command]

Signing commands:
  java-sign       - Sign with Java Ed25519
  java-hmac       - Sign with Java HMAC
  py-sign         - Sign with Python Ed25519
  py-hmac         - Sign with Python HMAC
  go-sign         - Sign with Go Ed25519
  go-hmac         - Sign with Go HMAC

Verification:
  verify          - Verify the last signature

Configuration:
  Edit .env file to change keys and test payload

Example:
  $0 java-sign
  $0 verify
EOF
        ;;
    *)
        echo "Unknown command: $1" >&2
        echo "Run '$0 help' for usage information" >&2
        exit 1
        ;;
esac

