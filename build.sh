#!/bin/bash

# GooGoo Game Build Script
# Creates a Mac .app bundle with icon and single-instance checking

set -e

echo "========================================="
echo "GooGoo Game - Build Script"
echo "========================================="
echo ""

# Configuration
APP_NAME="GooGoo"
MAIN_CLASS="com.game.core.Game"
ICON_SOURCE="src/main/resources/images/googoo-game-icon.png"
BUILD_DIR="target"
APP_BUNDLE_DIR="$BUILD_DIR/$APP_NAME.app"
CONTENTS_DIR="$APP_BUNDLE_DIR/Contents"
MACOS_DIR="$CONTENTS_DIR/MacOS"
RESOURCES_DIR="$CONTENTS_DIR/Resources"
ICON_DIR="$BUILD_DIR/icon"

# Clean previous build
echo "Cleaning previous builds..."
rm -rf "$APP_BUNDLE_DIR"
rm -rf "$ICON_DIR"
echo "✓ Cleanup complete"
echo ""

# Build with Maven
echo "Building with Maven..."
mvn clean package -DskipTests

if [ ! -f "$BUILD_DIR/googoo-game-remake-2.1.1.jar" ]; then
    echo "ERROR: Maven build failed - JAR not found"
    exit 1
fi
echo "✓ Maven build successful"
echo ""

# Create .app bundle structure
echo "Creating .app bundle structure..."
mkdir -p "$MACOS_DIR"
mkdir -p "$RESOURCES_DIR"
echo "✓ Bundle structure created"
echo ""

# Convert icon to .icns format
echo "Preparing application icon..."
mkdir -p "$ICON_DIR"

if [ -f "$ICON_SOURCE" ]; then
    if command -v sips &> /dev/null && command -v iconutil &> /dev/null; then
        # Create iconset directory
        ICONSET_DIR="$ICON_DIR/AppIcon.iconset"
        mkdir -p "$ICONSET_DIR"

        # Round the corners and add alpha using the Swift helper
        TEMP_PNG="$ICON_DIR/temp_icon.png"
        cp "$ICON_SOURCE" "$TEMP_PNG"

        # Generate different sizes from the PNG
        sips -z 16 16     "$TEMP_PNG" --out "$ICONSET_DIR/icon_16x16.png" &> /dev/null
        sips -z 32 32     "$TEMP_PNG" --out "$ICONSET_DIR/icon_16x16@2x.png" &> /dev/null
        sips -z 32 32     "$TEMP_PNG" --out "$ICONSET_DIR/icon_32x32.png" &> /dev/null
        sips -z 64 64     "$TEMP_PNG" --out "$ICONSET_DIR/icon_32x32@2x.png" &> /dev/null
        sips -z 128 128   "$TEMP_PNG" --out "$ICONSET_DIR/icon_128x128.png" &> /dev/null
        sips -z 256 256   "$TEMP_PNG" --out "$ICONSET_DIR/icon_128x128@2x.png" &> /dev/null
        sips -z 256 256   "$TEMP_PNG" --out "$ICONSET_DIR/icon_256x256.png" &> /dev/null
        sips -z 512 512   "$TEMP_PNG" --out "$ICONSET_DIR/icon_256x256@2x.png" &> /dev/null
        sips -z 512 512   "$TEMP_PNG" --out "$ICONSET_DIR/icon_512x512.png" &> /dev/null
        sips -z 1024 1024 "$TEMP_PNG" --out "$ICONSET_DIR/icon_512x512@2x.png" &> /dev/null

        # Convert to icns
        iconutil -c icns "$ICONSET_DIR" -o "$RESOURCES_DIR/$APP_NAME.icns"

        # Clean up temp file
        rm -f "$TEMP_PNG"

        echo "✓ Created .icns icon"
    else
        echo "WARNING: sips or iconutil not available, skipping icon conversion"
    fi
else
    echo "WARNING: Icon file not found at $ICON_SOURCE"
fi
echo ""

# Copy JAR and dependencies to Resources
echo "Copying application files..."
cp "$BUILD_DIR/googoo-game-remake-2.1.1.jar" "$RESOURCES_DIR/"

# Copy all dependencies from Maven
mvn dependency:copy-dependencies -DoutputDirectory="$RESOURCES_DIR/lib" -DincludeScope=runtime &> /dev/null
echo "✓ Application files copied"
echo ""

# Create the launcher script with single-instance checking
echo "Creating launcher script..."
cat > "$MACOS_DIR/$APP_NAME" << 'LAUNCHER_EOF'
#!/bin/bash

# GooGoo Game Launcher with Single-Instance Check
# Ensures only one instance of the game runs at a time

APP_NAME="GooGoo"
LOCK_FILE="/tmp/googoo-game.lock"
BUNDLE_DIR="$(cd "$(dirname "$0")/.." && pwd)"
RESOURCES_DIR="$BUNDLE_DIR/Resources"
MAIN_JAR="$RESOURCES_DIR/googoo-game-remake-2.1.1.jar"

# Function to check if process is running
is_running() {
    if [ -f "$LOCK_FILE" ]; then
        PID=$(cat "$LOCK_FILE")
        if ps -p "$PID" > /dev/null 2>&1; then
            # Check if it's actually our game
            if ps -p "$PID" -o command= | grep -q "googoo-game-remake"; then
                return 0
            fi
        fi
        # Stale lock file, remove it
        rm -f "$LOCK_FILE"
    fi
    return 1
}

# Check for existing instance
if is_running; then
    # Show error dialog
    osascript -e 'tell app "System Events" to display dialog "GooGoo Game is already running!\n\nOnly one instance can run at a time." buttons {"OK"} default button "OK" with icon caution with title "GooGoo Game"'
    exit 1
fi

# Create lock file with our PID
echo $$ > "$LOCK_FILE"

# Cleanup function to remove lock file on exit
cleanup() {
    rm -f "$LOCK_FILE"
}
trap cleanup EXIT INT TERM

# Change to Resources directory
cd "$RESOURCES_DIR"

# Build module path and classpath
MODULE_PATH=""
CLASSPATH="$MAIN_JAR"

if [ -d "$RESOURCES_DIR/lib" ]; then
    # Separate JavaFX modules for module path and other JARs for classpath
    for jar in "$RESOURCES_DIR/lib"/*.jar; do
        jarname=$(basename "$jar")
        if [[ "$jarname" == javafx-* ]] || [[ "$jarname" == *-mac.jar ]]; then
            if [ -z "$MODULE_PATH" ]; then
                MODULE_PATH="$jar"
            else
                MODULE_PATH="$MODULE_PATH:$jar"
            fi
        else
            CLASSPATH="$CLASSPATH:$jar"
        fi
    done
fi

# Find Java (use system Java or check common locations)
if [ -n "$JAVA_HOME" ]; then
    JAVA="$JAVA_HOME/bin/java"
elif command -v java &> /dev/null; then
    JAVA="java"
else
    osascript -e 'tell app "System Events" to display dialog "Java is not installed.\n\nPlease install Java 17 or later to run GooGoo Game." buttons {"OK"} default button "OK" with icon stop with title "GooGoo Game"'
    exit 1
fi

# Check Java version
JAVA_VERSION=$("$JAVA" -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F. '{print $1}')
if [ "$JAVA_VERSION" -lt 17 ]; then
    osascript -e 'tell app "System Events" to display dialog "Java version is too old.\n\nGooGoo Game requires Java 17 or later." buttons {"OK"} default button "OK" with icon stop with title "GooGoo Game"'
    exit 1
fi

# Launch the game with module path for JavaFX
if [ -n "$MODULE_PATH" ]; then
    exec "$JAVA" --module-path "$MODULE_PATH" --add-modules javafx.controls,javafx.graphics -cp "$CLASSPATH" -Xmx1g -Xdock:name="$APP_NAME" -Xdock:icon="$RESOURCES_DIR/$APP_NAME.icns" com.game.core.Game
else
    exec "$JAVA" -cp "$CLASSPATH" -Xmx1g -Xdock:name="$APP_NAME" -Xdock:icon="$RESOURCES_DIR/$APP_NAME.icns" com.game.core.Game
fi
LAUNCHER_EOF

chmod +x "$MACOS_DIR/$APP_NAME"
echo "✓ Launcher script created"
echo ""

# Create Info.plist
echo "Creating Info.plist..."
cat > "$CONTENTS_DIR/Info.plist" << PLIST_EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleDevelopmentRegion</key>
    <string>English</string>
    <key>CFBundleExecutable</key>
    <string>$APP_NAME</string>
    <key>CFBundleIconFile</key>
    <string>$APP_NAME.icns</string>
    <key>CFBundleIdentifier</key>
    <string>com.game.googoo</string>
    <key>CFBundleInfoDictionaryVersion</key>
    <string>6.0</string>
    <key>CFBundleName</key>
    <string>$APP_NAME</string>
    <key>CFBundleDisplayName</key>
    <string>$APP_NAME</string>
    <key>CFBundlePackageType</key>
    <string>APPL</string>
    <key>CFBundleShortVersionString</key>
    <string>2.1.1</string>
    <key>CFBundleVersion</key>
    <string>2.1.1</string>
    <key>LSMinimumSystemVersion</key>
    <string>10.14</string>
    <key>LSUIElement</key>
    <false/>
    <key>NSHighResolutionCapable</key>
    <true/>
    <key>NSSupportsAutomaticGraphicsSwitching</key>
    <true/>
</dict>
</plist>
PLIST_EOF
echo "✓ Info.plist created"
echo ""

# Create PkgInfo file
echo -n "APPL????" > "$CONTENTS_DIR/PkgInfo"

# Ad-hoc code sign the app bundle so macOS renders its icon
echo "Code signing app bundle..."
codesign --force --deep --sign - "$APP_BUNDLE_DIR" 2>/dev/null && echo "✓ Ad-hoc signed" || echo "  (codesign not available, skipping)"
echo ""

echo "========================================="
echo "✓ Build Complete!"
echo "========================================="
echo ""
echo "Application bundle created at:"
echo "  $APP_BUNDLE_DIR"
echo ""
echo "To run the game:"
echo "  ./run.sh"
echo "  or double-click: $APP_BUNDLE_DIR"
echo ""
echo "Features:"
echo "  - Custom application icon"
echo "  - Single-instance checking"
echo "  - Automatic lock file cleanup"
echo ""
