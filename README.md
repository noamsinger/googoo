# Classic 2D Game

A classic 2D Java game built with JavaFX.

## Architecture

- **Game.java** - Main game loop and window management using JavaFX
- **GameStateManager.java** - Manages game states (menu, play, config)
- **GameState.java** - Abstract base class for all game states
- **MenuState.java** - Main menu with Start, Config, Exit options
- **PlayState.java** - Game play state (placeholder)
- **ConfigState.java** - Configuration state (placeholder)
- **MenuItem.java** - UI component for menu items

## Controls

- **UP/DOWN Arrow Keys** - Navigate menu
- **ENTER** - Select menu option
- **ESC** - Return to main menu (from other states)

## Build & Run

Using the provided scripts:
```bash
./build.sh    # Build the game
./run.sh      # Run the game (auto-builds if needed)
```

Using Maven directly:
```bash
mvn clean compile
mvn javafx:run
```

## Features

- Smooth AnimationTimer-based game loop
- State-based architecture for easy expansion
- Keyboard input handling
- Classic menu navigation with arrow indicator
- JavaFX Canvas rendering with anti-aliasing
