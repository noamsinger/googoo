#!/bin/bash

echo "Starting Classic 2D Game..."

# Check if compiled classes exist
if [ ! -d "target/classes" ]; then
    echo "Game not built yet. Running build.sh first..."
    ./build.sh
fi

# Run the game
java -cp target/classes com.game.core.Game
