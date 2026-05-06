#!/bin/bash

echo "Starting GooGoo Remake Game"

# Find and kill any existing JavaFX game processes
pkill -f "javafx:run"
pkill -f "com.game.core.Game"

# Give it a moment to clean up
sleep 0.5

# Run the game using Maven JavaFX plugin
mvn javafx:run
