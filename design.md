# Game Design Document

## Overview
A 2D space shooter game built with JavaFX featuring a player-controlled spaceship navigating through space, collecting gems, avoiding/destroying enemies, and managing power-ups.

## Core Mechanics

### Player Spaceship
- **Movement**: Arrow keys control rotation and acceleration
  - Left/Right: Roll and turn
  - Up: Accelerate
  - Down: Decelerate
- **Speed**: Starts at 60% of max speed
- **Rotation**: Smooth angular velocity-based turning
- **Rolling**: Visual tilt effect when turning
- **Shooting**: Space bar fires projectiles
- **Shield/Lives**: Two game modes available

### Game Modes
1. **Shield Level Mode**: Player has shield percentage (100%), hearts restore shield based on remaining time
2. **Lives Mode**: Player starts with 3 lives, can collect unlimited lives via hearts
3. **Live Forever Mode**: Invincible practice mode with no damage or game over

### Level Progression

The game progresses through levels with enemies and black holes appearing at specific intervals:

#### Black Holes (appear on EVEN levels)
- **Level 2**: Black hole pair 0 (colorScheme 0)
- **Level 4**: Black hole pair 1 (colorScheme 1)
- **Level 6**: Black hole pair 2 (colorScheme 2)
- **Level 2N**: Black hole pair N-1 (colorScheme (N-1) % 16)

**Formula**: At even level L, black hole pair index = (L/2 - 1), colorScheme = ((L/2 - 1) % 16)

After 16 pairs (level 32), color schemes wrap around: Level 34 uses colorScheme 0 again.

#### Enemies (appear on ODD levels)

Enemies appear in skill-progressive order based on the formula:
```
enemyIndex = (L - 1) / 2
enemyType = APPEARANCE_ORDER[enemyIndex % 16]
```

**Appearance Order**: 0, 1, 2, 4, 8, 3, 5, 6, 9, 10, 12, 7, 11, 13, 14, 15

This creates a progression from 0 skills → 1 skill → 2 skills → 3 skills → 4 skills:
- **Level 1**: Enemy 0 (no skills)
- **Level 3**: Enemy 1 (wiggle only)
- **Level 5**: Enemy 2 (fast rotation only)
- **Level 7**: Enemy 4 (prediction only)
- **Level 9**: Enemy 8 (random speed only)
- **Level 11**: Enemy 3 (wiggle + fast rotation)
- And so on...

After all 16 types cycle (level 31), the pattern repeats.

**Starting at Level K
When starting at level K > 1:
- **Black holes**: Spawn all pairs from levels 2, 4, 6, ... up to floor(K/2)*2
  - Pairs: 0, 1, 2, ..., (K/2 - 1) with colorSchemes modulo 16
- **Enemies**: Spawn all enemies from levels 1, 3, 5, ... following the appearance order
  - Uses APPEARANCE_ORDER array for skill-progressive spawning

**Examples:**
- Level 1: 0 black hole pairs, 1 enemy (type 0 - no skills)
- Level 5: 2 black hole pairs (schemes 0, 1), 3 enemies (types 0, 1, 2 - progression of skills)
- Level 10: 5 black hole pairs (schemes 0-4), 5 enemies (types from appearance order)
- Level 33: 16 black hole pairs (schemes 0-15), 17 enemies (full cycle + type 0 again)

### Scoring System
- **Gem Collection**: Score increases by collecting gems
- **Score Display**: Large rainbow-colored text (48pt font)
  - Color cycles through: Red → Magenta → Blue → Cyan → Green → Yellow → Red
  - Complete cycle takes 10 seconds
  - Smooth interpolation between colors

### Collectibles

#### Gems
- **Spawn Rate**: 4-8 gems active at any time
- **Distance Constraint**: New gems spawn at least 200 pixels from existing gems
- **Fade-in**: 1 second fade-in animation when appearing
- **Collection**: Increases score by 1

#### Hearts
- **Spawn Condition**: Appears after collecting 4 gems
- **Duration**: 30 seconds with visual dimming
- **Dimming**: Brightness reduces from 100% to 10% over the duration
- **Fade-in**: 1 second fade-in animation when appearing
- **Effects**:
  - Shield Level Mode: Restores shield percentage based on remaining time (more effective if collected early)
  - Lives Mode: Adds 1 life (unlimited maximum)
  - Live Forever Mode: No effect (more effective if collected early)

### Enemies

#### Enemy Types (0-15)
Each enemy has a unique sprite animation with distinct visual design:
- **Enemy 0**: Classic UFO with silver hull and cyan dome
- **Enemy 1**: Alien creature with green body and red eyes
- **Enemy 2**: Triangular fighter with orange hull
- **Enemy 4**: Biomechanical organic ship with purple hull
- **Enemy 6**: Crystalline hexagonal ship with cyan body
- **Enemy 15**: Red-yellow blazing fireball

#### Enemy Behavior System
Enemies use bit-flag system for behavior combinations:
- **Base Behavior** (all enemies):
  - Navigate toward player at 0.5 of player's max speed
  - Rotation speed is 1/3 of player's rotation speed
  - Accelerate at half the player's acceleration rate when player accelerates
  - Instantly decelerate to match player speed when player slows down

- **0x01 (Wiggle)**: Direction modified by 45° × sin(time × π)
- **0x02 (Fast Rotation)**: Rotation speed is 3× player's rotation speed (instead of 1/3)
- **0x04 (Prediction)**: Targets predicted position 0.1-1.5 seconds ahead (includes rolling state estimation, randomized every 10s)
- **0x08 (Random Speed)**: Speed randomly varies between 0.5 and 1.1 of max speed with unique intervals (5-15s) per enemy, using starship's acceleration/deceleration rates

#### Blind Steering
- When player enters wormhole, enemies lose tracking
- Each enemy continues on random trajectory for 0-2 seconds after wormhole exit
- Provides escape opportunity for player

#### Spawn System
- **Fade-in**: 1 second fade-in animation when appearing
- Spawns continue throughout gameplay

### Special Mechanics

#### Wormholes
- Player can enter wormholes to teleport
- Causes enemy blind steering effect
- Strategic escape mechanism

#### Debug Mode
- Toggle with 'D' key
- Shows debug information in top-left corner
- Toggle message displays in center of screen
- Shows game state information

## Visual Design

### Background
- **Menu State**: Space-themed galaxy background with nebulae and stars
- **Config State**: Same galaxy background for visual consistency
- **Play State**: Dynamic starfield with parallax effect

### Color Schemes
- **Spaceship**: Blue-themed with white accents
- **Enemies**: Each type has unique color palette
  - UFO: Silver/cyan/magenta
  - Creature: Green/orange/red
  - Fighter: Orange/cyan/pink
  - Organic: Purple/yellow/cyan-green
  - Crystalline: Cyan/magenta/yellow/pink
  - Fireball: Red/yellow
- **Gems**: Colorful collectibles
- **Hearts**: Red/pink health items
- **Score**: Rainbow cycling colors

### Animations
- All sprites: 16-frame animation loops in 4x4 sprite sheets
- Enemy sprites: Various animations (rotation, pulsing, wobbling)
- Player sprites: Rolling/banking animations
- Fade-in effects: 1 second duration for all spawning objects

### Smoke Trails
- Spaceship emits smoke clouds from rear during flight
- Smoke rendered behind spaceship but in front of stars
- Light gray color (RGB: 180, 180, 180)
- Two-phase animation: expansion then fade-out

## Technical Architecture

### State Management
- **MenuState**: Main menu navigation
- **PlayState**: Active gameplay
- **ConfigState**: Configuration options

### Sprite Generation
- Procedurally generated sprites using Java AWT Graphics2D
- 80x80 pixel frames
- 4x4 grid sprite sheets (16 frames total)
- Gradient fills, anti-aliasing, complex paths

### Physics
- Velocity-based movement
- Angular velocity for rotation
- Acceleration/deceleration systems
- Distance-based collision detection

### Performance
- Cached sprite loading
- Efficient rendering with JavaFX Canvas
- Sprite sheet caching to prevent reload stalls

## Detailed Algorithms

### Resolution Scaling
All physics values scale with resolution using geometric mean:
```
scale = √(scaleX × scaleY)
where scaleX = currentWidth / 1920
      scaleY = currentHeight / 1080
```

### Player Spaceship Physics

**Base Constants (at 1920×1080):**
- `BASE_SPEED = 200.0` pixels/second
- `BASE_MIN_SPEED = 50.0` pixels/second
- `BASE_MAX_SPEED = 400.0` pixels/second
- `BASE_ACCELERATION_RATE = 100.0` pixels/second²
- `BASE_DECELERATION_RATE = 400.0` pixels/second²
- `rotationSpeed = 2.0` radians/second (not scaled)
- `BASE_SIZE = 30.0` pixels

**Initialization:**
```
speed = maxSpeed × 0.6  (starts at 60% of max speed)
```

**Movement Update (per frame):**
```
// Keyboard rotation
if (LEFT_PRESSED)  angle -= rotationSpeed × deltaTime
if (RIGHT_PRESSED) angle += rotationSpeed × deltaTime

// Keyboard acceleration/deceleration
if (UP_PRESSED)    speed += accelerationRate × deltaTime
if (DOWN_PRESSED)  speed -= decelerationRate × deltaTime

// Apply speed limits
speed = clamp(speed, minSpeed, maxSpeed)

// Calculate velocity
vx = cos(angle) × speed × deltaTime
vy = sin(angle) × speed × deltaTime

// Update position
x += vx
y += vy

// Boundary wrapping
if (x < 0) x += gameWidth
if (x > gameWidth) x -= gameWidth
if (y < 0) y += gameHeight
if (y > gameHeight) y -= gameHeight
```

**Mouse Navigation Mode:**
```
// Calculate desired angle to mouse position
desiredAngle = atan2(mouseY - y, mouseX - x)

// Normalize angle difference to [-π, π]
rotationDiff = desiredAngle - angle
while (rotationDiff > π)  rotationDiff -= 2π
while (rotationDiff < -π) rotationDiff += 2π

// Apply rotation with scaling near target
maxRotation = rotationSpeed × deltaTime
thirtyDegrees = π/6

if (|rotationDiff| < thirtyDegrees)
    rotationScale = |rotationDiff| / thirtyDegrees
    maxRotation *= rotationScale

if (|rotationDiff| < maxRotation)
    angle = desiredAngle
else
    angle += sign(rotationDiff) × maxRotation

// Accelerate based on alignment
if (MOUSE_PRESSED and alignment > 0)
    speed += accelerationRate × deltaTime × alignment
```

**Wormhole Effect:**
- Shrink duration: 0.5 seconds
- Expand duration: 0.5 seconds
```
if (shrinking)
    progress = wormholeTimer / 0.5
    size = normalSize × (1.0 - progress)
    x = startX + (wormholeX - startX) × progress
    y = startY + (wormholeY - startY) × progress
else (expanding)
    progress = (wormholeTimer - 0.5) / 0.5
    size = normalSize × progress
    x = wormholeTargetX
    y = wormholeTargetY
```

### Enemy AI Physics

**Base Constants (at 1920×1080):**
- `BASE_MIN_SPEED = 10.0` (for black holes)
- `BASE_MAX_SPEED = 50.0` (for black holes)
- `BASE_ACCELERATION = 2.0` (for black holes)

**Enemy Speed Calculation:**
```
// Base target speed (all enemies without skill 0x08)
baseTargetSpeed = spaceship.maxSpeed × 0.5

// Skill 0x08: Random Speed Variation (per enemy)
if (enemyType & 0x08)
    // Each enemy has unique target speed and update interval
    speedChangeTimer += deltaTime
    if (speedChangeTimer >= speedChangeInterval)  // 5-15 seconds, unique per enemy
        targetSpeed = spaceship.maxSpeed × (0.5 + random(0, 0.6))  // 0.5 to 1.1
        speedChangeInterval = 5.0 + random(0, 10.0)  // New random interval
    
    // Accelerate/decelerate using starship's rates
    if (speed < targetSpeed)
        speed += spaceship.accelerationRate × deltaTime
    else if (speed > targetSpeed)
        speed -= spaceship.decelerationRate × deltaTime
else
    // Normal enemies: adaptive speed matching
    if (spaceship_accelerating)
        spaceshipAccelRate = (spaceship.speed - previousSpeed) / deltaTime
        speed += (spaceshipAccelRate × 0.5) × deltaTime
        speed = min(speed, baseTargetSpeed)  // Cap at 0.5 of max
    else if (spaceship_decelerating)
        speed = spaceship.speed  // Instant match
    else
        speed += (baseTargetSpeed - speed) × min(1.0, deltaTime × 2.0)
```

**Enemy Rotation:**
```
// Calculate angle to target
targetAngle = atan2(targetY - y, targetX - x)

// Rotation speed based on skill 0x02
if (enemyType & 0x02)
    rotationSpeed = spaceship.rotationSpeed × 3.0  // Fast rotation
else
    rotationSpeed = spaceship.rotationSpeed / 3.0  // Normal (slow)

// Normalize angle difference
angleDiff = targetAngle - angle
while (angleDiff > π)  angleDiff -= 2π
while (angleDiff < -π) angleDiff += 2π

// Apply rotation
maxRotation = rotationSpeed × deltaTime
if (|angleDiff| < maxRotation)
    angle = targetAngle
else
    angle += sign(angleDiff) × maxRotation
```

**Skill 0x01 (Wiggle):**
```
wiggle = 45° × sin(gameTime × π)
moveAngle = angle + wiggle
```
Effect: Sinusoidal direction variation of ±45 degrees

**Skill 0x04 (Predictive Targeting):**
```
// Prediction time randomized every 10 seconds
predictionTime = 0.1 + random(0, 1.4)  // Range: 0.1 to 1.5 seconds

// Base prediction from velocity
predictedX = spaceshipX + cos(spaceship.angle) × spaceship.speed × predictionTime
predictedY = spaceshipY + sin(spaceship.angle) × spaceship.speed × predictionTime

// Add rolling drift (30% of forward speed)
if (LEFT_PRESSED)
    rollInfluence = spaceship.speed × 0.3
    predictedX += cos(spaceship.angle - π/2) × rollInfluence × predictionTime
    predictedY += sin(spaceship.angle - π/2) × rollInfluence × predictionTime
else if (RIGHT_PRESSED)
    rollInfluence = spaceship.speed × 0.3
    predictedX += cos(spaceship.angle + π/2) × rollInfluence × predictionTime
    predictedY += sin(spaceship.angle + π/2) × rollInfluence × predictionTime

targetX = predictedX
targetY = predictedY
```

**Blind Steering (Wormhole):**
```
// When spaceship enters wormhole
blindSteeringDuration = random(0.0, 2.0)  // Per enemy
blindTargetX = 100 + random(0, gameWidth - 200)
blindTargetY = 100 + random(0, gameHeight - 200)

// Enemy continues to blind target until timer expires
// Picks new random target when within 50 pixels
```

### Black Hole (Wormhole) Physics

**Base Constants (at 1920×1080):**
- `BASE_MIN_SPEED = 10.0` pixels/second
- `BASE_MAX_SPEED = 50.0` pixels/second
- `BASE_ACCELERATION = 2.0` pixels/second²
- `rotationRate = 0.3` radians/second

**Movement Priorities:**

1. **Boundary Avoidance** (highest priority):
```
edgeMargin = 150.0 × scale
distToEdgeX = min(x, gameWidth - x)
distToEdgeY = min(y, gameHeight - y)

if (distToEdgeX < edgeMargin OR distToEdgeY < edgeMargin)
    edgeFactor = min(distToEdgeX, distToEdgeY) / edgeMargin
    targetSpeed = speed × (1.0 - edgeFactor × 0.7)  // Reduce up to 70%
    targetAngle = atan2(centerY - y, centerX - x)  // Point toward center
```

2. **Pairing Behavior**:
Black holes move in pairs and influence each other's movement patterns

**Exit Animation:**
```
duration = 1.0 second
if (time < 0.5)
    progress = time / 0.5
    currentScale = normalScale + (3.0 - normalScale) × progress  // Expand to 3×
else if (time < 1.0)
    progress = (time - 0.5) / 0.5
    currentScale = 3.0 - (3.0 - normalScale) × progress  // Shrink to normal
```

### Score Rainbow Animation

**Color Cycle:**
6 colors cycling over 10 seconds total:
- Red → Magenta (0-1.67s)
- Magenta → Blue (1.67-3.33s)
- Blue → Cyan (3.33-5.0s)
- Cyan → Green (5.0-6.67s)
- Green → Yellow (6.67-8.33s)
- Yellow → Red (8.33-10.0s)

**Linear Interpolation:**
```
segmentDuration = 10.0 / 6.0 = 1.6667 seconds
segmentIndex = floor(scoreColorTimer / segmentDuration) mod 6
segmentProgress = (scoreColorTimer mod segmentDuration) / segmentDuration

r = r1 + (r2 - r1) × segmentProgress
g = g1 + (g2 - g1) × segmentProgress
b = b1 + (b2 - b1) × segmentProgress
```

### Heart System

**Spawn Condition:**
```
if (gemsCollected mod 4 == 0 AND gemsCollected > 0)
    spawn_heart()
```

**Dimming Effect:**
```
duration = 30.0 seconds
brightness = 100% - (timer / duration) × 90%
brightness = clamp(brightness, 10%, 100%)
```

**Shield Restoration (Shield Level Mode):**
```
remainingTime = 30.0 - heartTimer
restorationAmount = (remainingTime / 30.0) × 100.0
shieldPercentage = min(100.0, shieldPercentage + restorationAmount)
```
Formula: Earlier collection = more restoration (linear)

### Collision Detection

**Distance-Based Collision:**
```
dx = object1.x - object2.x
dy = object1.y - object2.y
distance = √(dx² + dy²)
collisionRadius = (object1.size + object2.size) / 2

if (distance < collisionRadius)
    collision_detected()
```

### Damage System

**Shield Mode:**
- Damage per hit: 1-10% (randomized)
- Game over when shield reaches 0%

**Lives Mode:**
- Lose 1 life per hit
- 5-second immunity after losing a life
- Immunity provides invulnerability with visual shield

**Immunity Shield Animation:**
```
if (LIVES mode and immune)
    pulseProgress = (gameTime mod 1.0) / 1.0  // 1 second cycle
    pulseScale = 0.9 + sin(pulseProgress × 2π) × 0.1  // Scale: 0.9-1.1
    circleRadius = spaceship.size × 1.5 × pulseScale
    color = Blue with 40% opacity
else if (LIVE_FOREVER mode)
    pulseProgress = (gameTime mod 2.0) / 2.0  // 2 second cycle
    pulseScale = 0.95 + sin(pulseProgress × 2π) × 0.05  // Scale: 0.95-1.05
    circleRadius = spaceship.size × 1.5 × pulseScale
    color = Golden yellow with 30% opacity
```

### Animation Timing

**Sprite Animations:**
- 16 frames per animation (4×4 sprite sheet)
- Frame change interval: 0.1 seconds
- Complete cycle: 1.6 seconds

**Fade-In Effects:**
- All spawning objects: 1.0 second linear fade from 0% to 100% opacity
- Black holes: 2.0 second fade-in

**Explosions:**
- Enemy explosion: 2.0 seconds (32 frames)
- Starship explosion: 10.0 seconds (256 frames)

### Smoke Trail System

**Emission:**
```
smokeInterval = 0.05 seconds (20 puffs per second)
emissionPosition = spaceship.position - cos(angle) × size × 0.3
                                      - sin(angle) × size × 0.3
// Emits from 30% behind spaceship center
```

**Lifetime Calculation:**
```
maxLifetime = 0.4 + sin(creationTime × 2π) × 0.2
// Range: 0.2 to 0.6 seconds (sinusoidal variation)
```

**Size and Opacity (Two-Phase Animation):**
```
BASE_SIZE = 8.0 pixels (scaled by resolution)
halfLife = maxLifetime / 2.0

Phase 1 (0 to halfLife): Expansion + Density reduction
    progress = lifetime / halfLife
    size = initialSize + (initialSize × 3.0) × progress  // 1× → 4×
    opacity = 0.8 - (0.4 × progress)                      // 0.8 → 0.4

Phase 2 (halfLife to maxLifetime): Constant size + Fade out
    progress = (lifetime - halfLife) / halfLife
    size = initialSize × 4.0                              // Stay at 4×
    opacity = 0.4 × (1.0 - progress)                      // 0.4 → 0.0
```

**Visual Properties:**
- Color: Light gray RGB(180, 180, 180)
- Shape: Circular (oval)
- Render order: Behind spaceship, in front of stars
- Removal: Automatic when `lifetime ≥ maxLifetime`
