# GooGoo Game Packaging Guide

This guide explains how to package the GooGoo game for **macOS** and **Windows** platforms.

## Quick Start

### macOS Development Build
```bash
./build.sh    # Creates target/GooGoo.app with icon and single-instance checking
./run.sh      # Runs the .app bundle
```

### Distribution Package
```bash
./package.sh  # Creates platform-specific installer in package/output/
```

## Build System Overview

### build.sh (macOS only)
Creates a native Mac `.app` bundle with:
- **Custom icon**: Converted from JPEG to .icns format
- **Single-instance checking**: Prevents multiple copies from running simultaneously
- **Lock file management**: Automatic cleanup on exit
- **Bundled dependencies**: All JavaFX and game dependencies included
- **Native launcher**: Shell script that checks Java version and manages lock files

The resulting `target/GooGoo.app` can be:
- Double-clicked to run
- Opened via `./run.sh`
- Packaged into a DMG installer via `./package.sh`

### run.sh
Simple launcher that:
1. Checks if `.app` bundle exists
2. Builds it if needed (calls `./build.sh`)
3. Opens the application via macOS `open` command

### package.sh
Creates distribution installers for macOS and Windows:
- **macOS**: DMG disk image containing the `.app` bundle
- **Windows**: `.exe` installer with start menu shortcuts

## Prerequisites

### All Platforms
- **Java JDK 17+** for development and building
  - Check your Java version: `java --version`
  - The built application can bundle its own JRE for distribution
- **Maven** installed and in PATH
  - Check: `mvn --version`

### Platform-Specific Requirements

#### macOS
- **Xcode Command Line Tools** (for icon conversion and DMG creation)
  - Install: `xcode-select --install`
- **sips** and **iconutil** commands (included with macOS)
- **hdiutil** (included with macOS) for DMG creation

#### Windows
- **WiX Toolset** for .exe installers (optional, jpackage can work without it)
  - Download from: https://wixtoolset.org/

## Quick Start

1. **Run the packaging script:**
   ```bash
   ./package.sh
   ```

2. **Find your package:**
   ```
   target/package/output/
   ```

## What the Scripts Do

### build.sh (macOS only)
1. **Cleans** previous build artifacts
2. **Builds** the application using Maven (`mvn clean package`)
3. **Creates** Mac .app bundle structure
4. **Converts** icon from JPEG to .icns format (all required sizes)
5. **Copies** JAR and dependencies to Resources directory
6. **Creates** launcher script with:
   - Single-instance checking via lock file (`/tmp/googoo-game.lock`)
   - Java version validation (requires Java 17+)
   - Automatic lock cleanup on exit
7. **Generates** Info.plist with app metadata

### package.sh
1. **Checks** dependencies (Maven, Java, platform tools)
2. **Cleans** previous package artifacts
3. **Builds** application:
   - macOS: Uses `build.sh` to create `.app` bundle
   - Other platforms: Uses Maven to build JAR
4. **Prepares** icon (platform-specific conversion)
5. **Packages** the application:
   - **macOS**: Creates DMG from `.app` bundle using `hdiutil`
   - **Linux**: Creates `.deb` and `.rpm` packages via `jpackage`
   - **Windows**: Creates `.exe` installer via `jpackage`

## Output

### macOS
- **Development**: `target/GooGoo.app` - Mac application bundle
  - Double-click to run
  - Has custom icon
  - Single-instance protection
- **Distribution**: `target/package/output/GooGoo-1.0.dmg` - Disk image installer
  - Install by opening the DMG and dragging to Applications

### Windows
- `target/package/output/GooGoo-1.0.exe` - Windows installer
- Run the .exe and follow the installation wizard

## Customization

Edit the configuration section in the scripts to customize:

### build.sh
- **APP_NAME**: Application name
- **MAIN_CLASS**: Main Java class
- **ICON_SOURCE**: Path to icon image

### package.sh
- **APP_NAME**: Application name
- **APP_VERSION**: Version number
- **ICON_SOURCE**: Path to icon image
- **MAIN_CLASS**: Main Java class
- Package metadata (vendor, description, etc.)

## Icon

The scripts use `src/main/resources/images/googoo-game-icon.jpeg` as the application icon.
- **build.sh** converts JPEG → PNG → .icns (multiple sizes for Mac)
- **package.sh** handles platform-specific icon conversion

To use a different icon, either:
1. Replace the image at that path, or
2. Edit the `ICON_SOURCE` variable in the scripts

## Single-Instance Protection (macOS)

The Mac .app bundle includes automatic single-instance checking:

**How it works:**
- Creates lock file at `/tmp/googoo-game.lock` with process ID
- Checks if another instance is running before launching
- Shows error dialog if instance already exists
- Automatically cleans up lock file on exit (normal or crash)
- Handles stale lock files (from previous crashes)

**User experience:**
- First launch: Game opens normally
- Second launch: Dialog appears: "GooGoo Game is already running!"
- After quitting: Lock file is automatically removed

**For developers:**
- Lock file location: `/tmp/googoo-game.lock`
- Manual cleanup if needed: `rm /tmp/googoo-game.lock`
- Check if running: `cat /tmp/googoo-game.lock` (shows PID)

## Troubleshooting

### "jpackage: command not found"
- You need JDK 16 or higher (jpackage was added in JDK 14, stabilized in 16)
- Download from: https://adoptium.net/ or https://www.oracle.com/java/technologies/downloads/

### "Build failed - JAR file not found"
- Make sure Maven can build the project: `mvn clean package`
- Check that `pom.xml` is configured correctly

### macOS: "App is damaged and can't be opened"
- This happens with apps not signed by Apple Developer certificate
- User can bypass: Right-click → Open → Open anyway
- For distribution, you need to sign the app with an Apple Developer account

### Windows: WiX Toolset warning
- jpackage can create installers without WiX, but WiX provides more features
- Download from: https://wixtoolset.org/

## Distribution

After packaging:

1. **Test** the installer on a clean system
2. **Sign** the application (recommended for public distribution):
   - macOS: Apple Developer certificate
   - Windows: Code signing certificate
3. **Upload** to your distribution platform
4. **Provide** installation instructions to users

## Notes

- The packaged application is **self-contained** and includes its own Java runtime
- Users **do not need Java installed** to run the packaged application
- Package size will be ~50-100MB due to included JRE
- The first run may be slower as the JVM warms up

## Support

For issues with packaging, check:
- Java version: `java --version` (must be 16+)
- Maven version: `mvn --version`
- jpackage availability: `jpackage --version`
- Build output: `mvn clean package` (should create JAR in `target/`)
