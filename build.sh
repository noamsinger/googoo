#!/bin/bash

echo "Building Classic 2D Game with JavaFX..."

# Build using Maven
mvn clean compile

if [ $? -eq 0 ]; then
    echo "Build successful!"
else
    echo "Build failed!"
    exit 1
fi