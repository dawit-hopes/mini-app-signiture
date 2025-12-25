# Variables
VENV = .venv
PYTHON = $(VENV)/bin/python3
PIP = $(VENV)/bin/pip

# Default task
all: install

# 1. Setup Virtual Environment and Install Dependencies
install:
	python3 -m venv $(VENV)
	$(PIP) install -r requirements.txt

# 2. Run HMAC Signer (exports SIGNATURE and PAYLOAD env vars)
hmac: install
	PYTHONPATH=src $(PYTHON) src/payment_security/hmac_signer.py

# 3. Run Ed25519 Signer (exports SIGNATURE and PAYLOAD env vars)
sign: install
	PYTHONPATH=src $(PYTHON) src/payment_security/sign.py

# 4. Clean up
clean:
	rm -rf $(VENV)
	find . -type d -name "__pycache__" -exec rm -rf {} +