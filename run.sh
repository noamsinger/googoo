#!/bin/bash

# GooGoo Game Run Script
# Runs the Mac .app bundle created by build.sh

APP_BUNDLE="target/GooGoo.app"

echo "Starting GooGoo Game..."
echo ""

# Check if .app bundle exists
if [ ! -d "$APP_BUNDLE" ]; then
    echo "Application bundle not found. Building first..."
    ./build.sh
    if [ $? -ne 0 ]; then
        echo "Build failed!"
        exit 1
    fi
    echo ""
fi

# Run the application
echo "Launching $APP_BUNDLE"
open "$APP_BUNDLE"
