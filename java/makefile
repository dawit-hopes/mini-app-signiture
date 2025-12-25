# Variables
GROUP_PATH = com.payment.security

# Default task: Clean and build the project
all: build

# 1. Clean and Package the JAR
build:
	mvn clean package

# 2. Run the Ed25519 Signer (exports SIGNATURE and PAYLOAD env vars)
sign: build
	mvn exec:java -Dexec.mainClass="$(GROUP_PATH).Sign"

# 3. Run the HMAC Signer (exports SIGNATURE and PAYLOAD env vars)
hmac: build
	mvn exec:java -Dexec.mainClass="$(GROUP_PATH).HmacSigner"

# 4. Clean only
clean:
	mvn clean

# Help command to see available options
help:
	@echo "Available commands:"
	@echo "  make build  - Run mvn clean package"
	@echo "  make sign   - Run the Ed25519 Signer class"
	@echo "  make hmac   - Run the HMAC Signer class"
	@echo "  make clean  - Run mvn clean"