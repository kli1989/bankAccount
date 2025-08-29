#!/bin/bash

# Gatling Performance Test Runner for Banking System
# Usage: ./run-gatling-tests.sh [simulation-class] [users] [duration]

# Default values
SIMULATION_CLASS=${1:-"CreateAccountSimulation"}
USERS=${2:-10}
DURATION=${3:-60}

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 Banking System Gatling Performance Test${NC}"
echo "=========================================="
echo "Simulation Class: $SIMULATION_CLASS"
echo "Users: $USERS"
echo "Duration: $DURATION seconds"
echo ""

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}❌ Maven is not installed or not in PATH${NC}"
    exit 1
fi

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo -e "${RED}❌ Java is not installed or not in PATH${NC}"
    exit 1
fi

# Check if banking system is running
echo "Checking if Banking System is running..."
if curl -s --max-time 5 "http://localhost:8080/api/v1/accounts" > /dev/null; then
    echo -e "${GREEN}✅ Banking System is running${NC}"
else
    echo -e "${RED}❌ Banking System is not responding${NC}"
    echo "Please start the banking system first:"
    echo "  mvn spring-boot:run"
    echo "Or run it in the background:"
    echo "  nohup mvn spring-boot:run > banking-system.log 2>&1 &"
    exit 1
fi

echo ""
echo "Starting Gatling performance test..."

# Run Gatling test
mvn gatling:test -Dgatling.simulationClass=gatling.$SIMULATION_CLASS

# Check if test completed successfully
if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}✅ Gatling test completed successfully!${NC}"

    # Show report location
    if [ -d "target/gatling" ]; then
        LATEST_REPORT=$(ls -td target/gatling/*/ | head -1)
        echo ""
        echo -e "${BLUE}📊 Test Report:${NC}"
        echo "  $LATEST_REPORT"
        echo ""
        echo -e "${YELLOW}💡 To view the HTML report:${NC}"
        echo "  open $LATEST_REPORT/index.html"
    fi
else
    echo ""
    echo -e "${RED}❌ Gatling test failed!${NC}"
    echo "Check the logs above for error details."
    exit 1
fi

echo ""
echo -e "${GREEN}🎉 Performance testing completed!${NC}"
