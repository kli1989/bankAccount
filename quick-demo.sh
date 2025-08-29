#!/bin/bash

# Quick Demo: Gatling Performance Test for Banking System
# This script demonstrates running a simple performance test

echo "🎯 Banking System Gatling Demo"
echo "=============================="
echo ""

# Check if banking system is running
echo "Step 1: Checking if Banking System is running..."
if curl -s --max-time 5 "http://localhost:8080/api/v1/accounts" > /dev/null; then
    echo "✅ Banking System is running"
else
    echo "❌ Banking System is not running"
    echo ""
    echo "Please start the banking system first:"
    echo "  mvn spring-boot:run"
    echo ""
    echo "Or run it in the background:"
    echo "  nohup mvn spring-boot:run > banking-system.log 2>&1 &"
    exit 1
fi

echo ""
echo "Step 2: Running quick performance test..."
echo "This will create 5 accounts with a 2-second ramp-up"
echo ""

# Run a quick test with minimal load
mvn gatling:test -Dgatling.simulationClass=gatling.CreateAccountSimulation \
  -Dusers=5 \
  -DrampUp=2 \
  -Dduration=10

echo ""
echo "🎉 Demo completed!"
echo ""
echo "📊 Check the results:"
echo "  ls -la target/gatling/"
echo ""
echo "📈 View the HTML report:"
echo "  open target/gatling/\$(ls -t target/gatling | head -1)/index.html"
echo ""
echo "🚀 For full performance testing:"
echo "  ./run-gatling-tests.sh CreateAccountSimulation 20 60"
