#!/bin/bash

echo "Building Classic 2D Game..."

# Create target directory if it doesn't exist
mkdir -p target/classes

# Compile all Java files
javac -d target/classes src/main/java/com/game/**/*.java

if [ $? -eq 0 ]; then
    echo "Build successful!"
else
    echo "Build failed!"
    exit 1
fi
