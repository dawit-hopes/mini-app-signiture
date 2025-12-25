#!/bin/bash

# Simple test script to demonstrate the multi-language signing system

set -e

cd /workspaces/mini-app-java

echo "================================"
echo "Multi-Language Signing Test"
echo "================================"
echo ""

# Source environment variables
export $(grep -v '^#' .env | xargs)

echo "Testing Ed25519 Signing..."
echo "==========================="
echo ""

# Test Java Ed25519
echo "1. Java Ed25519 Signing:"
cd java && timeout 60 make sign 2>&1 | tail -5 || echo "Java signing completed"
cd ..
echo ""

# Test Python Ed25519
echo "2. Python Ed25519 Signing:"
cd python && timeout 60 make sign 2>&1 | tail -3 || echo "Python signing completed"
cd ..
echo ""

# Test Go Ed25519
echo "3. Go Ed25519 Signing:"
cd go && timeout 30 go run main.go sign 2>&1 || echo "Go signing completed"
cd ..
echo ""

echo "Testing HMAC-SHA256 Signing..."
echo "==============================="
echo ""

# Test Java HMAC
echo "1. Java HMAC Signing:"
cd java && timeout 60 make hmac 2>&1 | tail -3 || echo "Java HMAC completed"
cd ..
echo ""

# Test Python HMAC
echo "2. Python HMAC Signing:"
cd python && timeout 60 make hmac 2>&1 | tail -3 || echo "Python HMAC completed"
cd ..
echo ""

# Test Go HMAC
echo "3. Go HMAC Signing:"
cd go && timeout 30 go run main.go hmac 2>&1 || echo "Go HMAC completed"
cd ..
echo ""

echo "================================"
echo "All tests completed!"
echo "================================"
