.PHONY: java-sign java-hmac py-sign py-hmac go-sign go-hmac verify help verify-ed25519 verify-hmac

# Load environment variables from .env file
# NOTE: We don't use 'include .env' because it breaks with spaces in values
# Instead, we source .env in subshells where needed

# Java - Ed25519 and HMAC signing (both)
java-sign:
	@bash -c 'set -a; source .env; set +a; echo "=== Java Ed25519 Signer ==="; cd java && $(MAKE) sign 2>&1 | tee /tmp/java_ed25519.txt'
	@bash -c 'set -a; source .env; set +a; echo ""; echo "=== Java HMAC Signer ==="; cd java && $(MAKE) hmac 2>&1 | tee /tmp/java_hmac.txt'
	@bash -c 'grep "export SIGNATURE=" /tmp/java_ed25519.txt | sed "s/.*stdout. //" > /tmp/sig_ed25519.sh 2>/dev/null || true; grep "export PAYLOAD=" /tmp/java_ed25519.txt | sed "s/.*stdout. //" >> /tmp/sig_ed25519.sh 2>/dev/null || true; echo "export SIGNATURE_TYPE=ed25519" >> /tmp/sig_ed25519.sh; grep "export SIGNATURE=" /tmp/java_hmac.txt | sed "s/.*stdout. //" > /tmp/sig_hmac.sh 2>/dev/null || true; grep "export PAYLOAD=" /tmp/java_hmac.txt | sed "s/.*stdout. //" >> /tmp/sig_hmac.sh 2>/dev/null || true; echo "export SIGNATURE_TYPE=hmac" >> /tmp/sig_hmac.sh'

# Java - HMAC signing
java-hmac:
	@echo "=== Java HMAC Signer ==="
	@cd java && $(MAKE) hmac > /tmp/signing_output.txt 2>&1
	@grep -E "^(export|HMAC)" /tmp/signing_output.txt || cat /tmp/signing_output.txt
	@grep "^export SIGNATURE=" /tmp/signing_output.txt > /tmp/sig.sh 2>/dev/null || true
	@grep "^export PAYLOAD=" /tmp/signing_output.txt >> /tmp/sig.sh 2>/dev/null || true
	@echo "export SIGNATURE_TYPE=hmac" >> /tmp/sig.sh
	@. /tmp/sig.sh 2>/dev/null || true

# Python - Ed25519 and HMAC signing (both)
py-sign:
	@echo "=== Python Ed25519 Signer ==="
	@cd python && $(MAKE) sign 2>&1 | tee /tmp/py_ed25519.txt
	@echo ""
	@echo "=== Python HMAC Signer ==="
	@cd python && $(MAKE) hmac 2>&1 | tee /tmp/py_hmac.txt
	@bash -c 'grep "export SIGNATURE=" /tmp/py_ed25519.txt > /tmp/sig_ed25519.sh 2>/dev/null || true; grep "export PAYLOAD=" /tmp/py_ed25519.txt >> /tmp/sig_ed25519.sh 2>/dev/null || true; echo "export SIGNATURE_TYPE=ed25519" >> /tmp/sig_ed25519.sh; grep "export SIGNATURE=" /tmp/py_hmac.txt > /tmp/sig_hmac.sh 2>/dev/null || true; grep "export PAYLOAD=" /tmp/py_hmac.txt >> /tmp/sig_hmac.sh 2>/dev/null || true; echo "export SIGNATURE_TYPE=hmac" >> /tmp/sig_hmac.sh'

# Python - HMAC signing
py-hmac:
	@echo "=== Python HMAC Signer ==="
	@cd python && $(MAKE) hmac > /tmp/signing_output.txt 2>&1
	@grep -E "^(export|HMAC)" /tmp/signing_output.txt || cat /tmp/signing_output.txt
	@grep "^export SIGNATURE=" /tmp/signing_output.txt > /tmp/sig.sh 2>/dev/null || true
	@grep "^export PAYLOAD=" /tmp/signing_output.txt >> /tmp/sig.sh 2>/dev/null || true
	@echo "export SIGNATURE_TYPE=hmac" >> /tmp/sig.sh
	@. /tmp/sig.sh 2>/dev/null || true

# Go - Ed25519 and HMAC signing (both)
go-sign:
	@echo "=== Go Ed25519 Signer ==="
	@cd go && go run main.go sign 2>&1 | tee /tmp/go_ed25519.txt
	@echo ""
	@echo "=== Go HMAC Signer ==="
	@cd go && go run main.go hmac 2>&1 | tee /tmp/go_hmac.txt
	@bash -c 'grep "export SIGNATURE=" /tmp/go_ed25519.txt > /tmp/sig_ed25519.sh 2>/dev/null || true; grep "export PAYLOAD=" /tmp/go_ed25519.txt >> /tmp/sig_ed25519.sh 2>/dev/null || true; echo "export SIGNATURE_TYPE=ed25519" >> /tmp/sig_ed25519.sh; grep "export SIGNATURE=" /tmp/go_hmac.txt > /tmp/sig_hmac.sh 2>/dev/null || true; grep "export PAYLOAD=" /tmp/go_hmac.txt >> /tmp/sig_hmac.sh 2>/dev/null || true; echo "export SIGNATURE_TYPE=hmac" >> /tmp/sig_hmac.sh'

# Go - HMAC signing
go-hmac:
	@echo "=== Go HMAC Signer ==="
	@cd go && go run main.go hmac > /tmp/signing_output.txt 2>&1
	@grep -E "^(export|✅)" /tmp/signing_output.txt || cat /tmp/signing_output.txt
	@grep "^export SIGNATURE=" /tmp/signing_output.txt > /tmp/sig.sh 2>/dev/null || true
	@grep "^export PAYLOAD=" /tmp/signing_output.txt >> /tmp/sig.sh 2>/dev/null || true
	@echo "export SIGNATURE_TYPE=hmac" >> /tmp/sig.sh
	@. /tmp/sig.sh 2>/dev/null || true

# Verify Ed25519 signature
verify-ed25519:
	@echo "=== Verifying Ed25519 Signature ==="
	@if [ ! -f /tmp/sig.sh ] || ! grep -q "SIGNATURE=" /tmp/sig.sh; then \
		echo "❌ Error: No signature available. Run a signing command first (e.g., 'make java-sign')"; \
		exit 1; \
	fi
	@. /tmp/sig.sh 2>/dev/null; \
	export ED25519_PUBLIC_KEY HMAC_SECRET; \
	cd verifier-go && go run main.go

# Verify HMAC signature
verify-hmac:
	@echo "=== Verifying HMAC Signature ==="
	@if [ ! -f /tmp/sig.sh ] || ! grep -q "SIGNATURE=" /tmp/sig.sh; then \
		echo "❌ Error: No signature available. Run a signing command first (e.g., 'make java-hmac')"; \
		exit 1; \
	fi
	@. /tmp/sig.sh 2>/dev/null; \
	export ED25519_PUBLIC_KEY HMAC_SECRET; \
	cd verifier-go && go run main.go

# Unified verify command - verifies both Ed25519 and HMAC signatures
verify:
	@echo "=== Verifying All Signatures ==="
	@bash -c '\
		set -e; \
		found_any=0; \
		root_dir="$$(pwd)"; \
		\
		if [ -f /tmp/sig_ed25519.sh ] && grep -q "SIGNATURE=" /tmp/sig_ed25519.sh; then \
			echo ""; \
			echo "Verifying Ed25519 Signature..."; \
			source /tmp/sig_ed25519.sh; \
			source $$root_dir/.env; \
			cd $$root_dir/verifier-go && go run main.go; \
			found_any=1; \
		fi; \
		\
		if [ -f /tmp/sig_hmac.sh ] && grep -q "SIGNATURE=" /tmp/sig_hmac.sh; then \
			echo ""; \
			echo "Verifying HMAC Signature..."; \
			source /tmp/sig_hmac.sh; \
			source $$root_dir/.env; \
			cd $$root_dir/verifier-go && go run main.go; \
			found_any=1; \
		fi; \
		\
		if [ $$found_any -eq 0 ]; then \
			echo "❌ Error: No signatures found. Run a signing command first (e.g., make java-sign)"; \
			exit 1; \
		fi; \
	'

# Help command
help:
	@echo "Multi-Language Signature Testing"
	@echo ""
	@echo "Available commands:"
	@echo ""
	@echo "Java:"
	@echo "  make java-sign      - Sign with Java Ed25519"
	@echo "  make java-hmac      - Sign with Java HMAC"
	@echo ""
	@echo "Python:"
	@echo "  make py-sign        - Sign with Python Ed25519"
	@echo "  make py-hmac        - Sign with Python HMAC"
	@echo ""
	@echo "Go:"
	@echo "  make go-sign        - Sign with Go Ed25519"
	@echo "  make go-hmac        - Sign with Go HMAC"
	@echo ""
	@echo "Verification (run after any signing command):"
	@echo "  make verify         - Verify the last signature"
	@echo "  make verify-ed25519 - Verify Ed25519 signature"
	@echo "  make verify-hmac    - Verify HMAC signature"
	@echo ""
	@echo "Example workflow:"
	@echo "  make java-sign"
	@echo "  make verify"
	@echo ""
	@echo "Configuration:"
	@echo "  Edit .env file to change keys and test payload"