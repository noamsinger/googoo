# GooGoo Game Remake

Googoo is a remake of an old sinclair spectrum game developed a long time ago. It maintains the game spirit.

This is a  remake of the classic GooGoo space shooter game built with JavaFX featuring procedurally generated sprites, intelligent enemy AI, and dynamic scoring system.

Developed using Vibe-Coding on a MacBook Pro, using Claude-Code and assistance from Gemini.

Developed by: Noam Singer

Spiritual Successor to: GooGoo by Noam Singer, & Raz Shoham (original Sinclair Spectrum game)

## Game Features

### Gameplay
- **Player-controlled spaceship** with smooth physics-based movement
- **Gem collection system** with rainbow-colored score display
- **Heart power-ups** with time-limited effectiveness
- **16 unique enemy types** with AI behavior combinations
- **Three game modes**: Shield Level, Lives, or Live Forever
- **Wormhole mechanics** for strategic escapes
- **Dynamic difficulty** through enemy behavior flags
- **Level progression** with automatic save/resume (Shield and Lives modes)

### Visual Design
- **Procedurally generated sprites** - all enemy sprites created with Java AWT Graphics2D
- **Smooth animations** - 16-frame sprite sheets for all entities
- **Rainbow score display** - cycles through 6 colors every 10 seconds
- **Fade-in effects** - all spawning objects fade in over 1 second
- **Rich color palettes** - unique themes for each enemy type
- **Space-themed backgrounds** - galaxy background on menu and config screens

### Enemy AI System
Enemies use bit-flag combinations for complex behaviors:
- **Base**: Navigate toward player at 0.5 max speed, rotation 3× slower than player
- **0x01 (Wiggle)**: Direction modified by 45° × sin(time × π)
- **0x02 (Fast Rotation)**: Can rotate 3× faster than player (instead of 3× slower)
- **0x04 (Prediction)**: Targets predicted position 0.1-1.5s ahead with roll estimation
- **0x08 (Random Speed)**: Unique random speeds (0.5-1.1× max) with starship's acceleration/deceleration

## Controls

### Menu Navigation
- **UP/DOWN Arrow Keys** - Navigate menu options
- **LEFT/RIGHT Arrow Keys** - Change option values
- **ENTER** - Select menu option / Confirm
- **ESC** - Return to main menu
- **Mouse** - Hover and click menu items

### Gameplay
- **LEFT/RIGHT Arrow Keys** - Roll and turn the spaceship
- **UP Arrow** - Accelerate
- **DOWN Arrow** - Decelerate/brake
- **Mouse Click** - Set navigation target
- **ESC** - Return to main menu

## Architecture

### Core Classes
- **Game.java** - Main game loop and window management using JavaFX
- **GameStateManager.java** - Manages game states (menu, play, config)
- **GameState.java** - Abstract base class for all game states
- **MenuState.java** - Main menu with Start, Config, Exit options
- **PlayState.java** - Main gameplay state with all game mechanics
- **ConfigState.java** - Configuration options

### Sprite Generators
- **AlienSpaceshipSpriteGenerator.java** - Enemy 0 (UFO)
- **AlienCreatureSpriteGenerator.java** - Enemy 1 (Creature)
- **AlienSpaceship2SpriteGenerator.java** - Enemy 2 (Fighter)
- **AlienSpaceship4SpriteGenerator.java** - Enemy 4 (Organic)
- **AlienSpaceship6SpriteGenerator.java** - Enemy 6 (Crystalline)
- **FireballSpriteGenerator.java** - Enemy 15 (Fireball)
- **EnemySpriteLoader.java** - Sprite sheet loading and caching

### UI Components
- **MenuItem.java** - Menu item rendering

## Build & Run

### Quick Start (macOS)
```bash
./build.sh        # Build Mac .app bundle with icon and single-instance checking
./run.sh          # Run the game (auto-builds if needed)
```

The build creates a native macOS application (`target/GooGoo.app`) with:
- Custom application icon
- Single-instance checking (prevents multiple copies from running)
- Automatic lock file cleanup
- No Java installation required for end users (when packaged with JRE)

### Development Mode
```bash
mvn javafx:run    # Run directly with Maven (fast iteration)
```

### Packaging for Distribution
```bash
./package.sh      # Create platform-specific installer (.dmg for macOS)
```
See [PACKAGING.md](PACKAGING.md) for detailed packaging instructions.

### Using Maven
```bash
mvn clean compile # Compile the project
mvn package       # Build JAR file
mvn javafx:run    # Run the application
```

## Technical Features

- **Smooth AnimationTimer-based game loop** at 60 FPS
- **State-based architecture** for easy expansion
- **Velocity and acceleration physics**
- **Distance-based collision detection**
- **Sprite sheet caching** for optimal performance
- **Color interpolation** for smooth rainbow effects
- **Bit-flag behavior system** for modular enemy AI
- **Procedural sprite generation** with gradients and anti-aliasing

## Game Mechanics Details

### Game Modes
- **Shield Level**: Health represented as shield percentage (100% to 0%)
  - Takes 1-10% damage per enemy hit
  - Hearts restore shield percentage based on collection timing
  - Game over when shield reaches 0%
  - Level selection available for reached levels
  
- **Lives**: Start with 3 lives, unlimited maximum
  - 5-second immunity after losing a life
  - Hearts add one additional life (no cap)
  - Game over when lives reach 0
  - Level selection available for reached levels
  
- **Live Forever**: Invincible practice mode
  - No damage from enemies
  - No game over
  - Hearts have no effect
  - Only level 1 available (no progression tracking)

### Scoring
- Collect gems to increase score
- Score displayed in large rainbow text (10-second color cycle)
- Color sequence: Red → Magenta → Blue → Cyan → Green → Yellow → Red

### Hearts
- Spawn after collecting 4 gems
- Last 30 seconds with visual dimming (100% → 10%)
- **Shield Level Mode**: Restores shield based on remaining time (more effective if collected early)
- **Lives Mode**: Adds 1 life (unlimited maximum)
- **Live Forever Mode**: No effect

### Enemy Behavior
- All enemies chase the player at varying speeds
- Speed and rotation adapt to player's movement
- Blind steering when player uses wormholes (0-2 second recovery)
- Each enemy type combines different behavioral flags

## Development

Built with:
- Java 17+
- JavaFX 23
- Maven 3.9+
- Java AWT Graphics2D for sprite generation

See `design.md` for detailed game design documentation.
