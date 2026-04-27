# Classic 2D Game

A classic 2D Java game built with Swing.

## Architecture

- **Game.java** - Main game loop and window management
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

Using Maven:
```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="com.game.core.Game"
```

Or compile and run directly:
```bash
javac -d target/classes src/main/java/com/game/**/*.java
java -cp target/classes com.game.core.Game
```

## Features

- 60 FPS game loop
- State-based architecture for easy expansion
- Keyboard input handling
- Classic menu navigation with arrow indicator
