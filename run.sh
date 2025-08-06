#!/bin/bash

# JSON Socket Connector Launcher Script

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    echo "Please install Maven 3.6 or higher"
    exit 1
fi

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    echo "Please install Java 11 or higher"
    exit 1
fi

# Get script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
cd "$SCRIPT_DIR"

echo "Building JSON Socket Connector..."
mvn clean compile -q

if [ $? -ne 0 ]; then
    echo "Error: Build failed"
    exit 1
fi

echo "Starting JSON Socket Connector..."
mvn javafx:run

echo "Application closed."