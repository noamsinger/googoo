#!/bin/bash

# GooGoo Game Packaging Script
# Creates platform-specific executable packages for macOS and Windows

set -e  # Exit on error

echo "========================================="
echo "GooGoo Game - Packaging Script"
echo "========================================="
echo ""

# Configuration
APP_NAME="GooGoo"
APP_VERSION="1.0-SNAPSHOT"
MAIN_CLASS="com.game.core.Game"
MAIN_JAR="googoo-game-remake-${APP_VERSION}.jar"
ICON_SOURCE="src/main/resources/images/googoo-game-icon.jpeg"

# Directories
BUILD_DIR="target"
PACKAGE_DIR="${BUILD_DIR}/package"
ICON_DIR="${PACKAGE_DIR}/icons"
APP_BUNDLE_DIR="${BUILD_DIR}/${APP_NAME}.app"

# Detect OS
OS_TYPE="$(uname -s)"
case "${OS_TYPE}" in
    Darwin*)    OS="macos";;
    MINGW*|MSYS*|CYGWIN*)     OS="windows";;
    *)
        echo "ERROR: Unsupported OS: ${OS_TYPE}"
        echo "This script only supports macOS and Windows"
        exit 1
        ;;
esac

echo "Detected OS: ${OS}"
echo ""

# Check for required tools
check_dependencies() {
    echo "Checking dependencies..."

    if ! command -v mvn &> /dev/null; then
        echo "ERROR: Maven (mvn) is not installed or not in PATH"
        exit 1
    fi

    if ! command -v jpackage &> /dev/null; then
        echo "ERROR: jpackage is not available. Please use JDK 16+ that includes jpackage."
        echo "You can check with: java --version"
        exit 1
    fi

    echo "✓ All dependencies found"
    echo ""
}

# Build the application
build_app() {
    echo "Building application..."

    # Check if we should use build.sh for macOS
    if [ "$OS" = "macos" ] && [ -f "build.sh" ]; then
        echo "Using build.sh to create .app bundle..."
        ./build.sh

        if [ ! -d "${APP_BUNDLE_DIR}" ]; then
            echo "ERROR: build.sh failed - .app bundle not found"
            exit 1
        fi

        echo "✓ .app bundle created successfully"
    else
        echo "Building with Maven..."
        mvn clean package -DskipTests

        if [ ! -f "${BUILD_DIR}/${MAIN_JAR}" ]; then
            echo "ERROR: Build failed - JAR file not found at ${BUILD_DIR}/${MAIN_JAR}"
            exit 1
        fi

        echo "✓ Build successful"
    fi
    echo ""
}

# Prepare icon
prepare_icon() {
    echo "Preparing application icon..."

    # Skip icon preparation for macOS since build.sh already handled it
    if [ "$OS" = "macos" ]; then
        echo "✓ Icon already prepared by build.sh"
        echo ""
        return
    fi

    mkdir -p "${ICON_DIR}"

    if [ ! -f "${ICON_SOURCE}" ]; then
        echo "WARNING: Icon file not found at ${ICON_SOURCE}"
        echo "Continuing without custom icon..."
        return
    fi

    # For Windows, we need .ico format

    if [ "$OS" = "windows" ]; then
        # For Windows, use PNG (jpackage can convert)
        cp "${ICON_SOURCE}" "${ICON_DIR}/app-icon.png"
        ICON_PATH="${ICON_DIR}/app-icon.png"
        echo "✓ Using PNG icon (jpackage will convert for Windows)"
    fi
    echo ""
}

# Package the application
package_app() {
    echo "Packaging application for ${OS}..."

    # Common jpackage arguments
    JPACKAGE_ARGS=(
        --name "${APP_NAME}"
        --app-version "1.0"
        --input "${BUILD_DIR}"
        --main-jar "${MAIN_JAR}"
        --main-class "${MAIN_CLASS}"
        --dest "${PACKAGE_DIR}/output"
        --vendor "GooGoo Game Team"
        --description "GooGoo - A thrilling space adventure game"
    )

    # Add icon if available
    if [ -n "${ICON_PATH}" ] && [ -f "${ICON_PATH}" ]; then
        JPACKAGE_ARGS+=(--icon "${ICON_PATH}")
    fi

    # Platform-specific packaging
    case "${OS}" in
        macos)
            echo "Creating macOS .dmg from .app bundle..."

            # Check if .app bundle exists
            if [ ! -d "${APP_BUNDLE_DIR}" ]; then
                echo "ERROR: .app bundle not found at ${APP_BUNDLE_DIR}"
                exit 1
            fi

            # Create output directory
            mkdir -p "${PACKAGE_DIR}/output"

            # Create DMG using hdiutil
            DMG_TEMP="${PACKAGE_DIR}/temp.dmg"
            DMG_FINAL="${PACKAGE_DIR}/output/${APP_NAME}-1.0.dmg"

            # Remove old DMG if exists
            rm -f "$DMG_TEMP" "$DMG_FINAL"

            # Create temporary DMG
            hdiutil create -srcfolder "${APP_BUNDLE_DIR}" -volname "${APP_NAME}" -fs HFS+ -format UDRW "$DMG_TEMP"

            # Mount it using an explicit mount point
            MOUNT_DIR="/Volumes/${APP_NAME}"
            # Detach any leftover mount from a previous failed run
            hdiutil detach "$MOUNT_DIR" 2>/dev/null || true
            hdiutil attach "$DMG_TEMP" -mountpoint "$MOUNT_DIR"

            # Create Applications symlink
            ln -s /Applications "$MOUNT_DIR/Applications"

            # Set the volume icon using the .app's icns
            ICNS_FILE="${APP_BUNDLE_DIR}/Contents/Resources/${APP_NAME}.icns"
            if [ -f "$ICNS_FILE" ]; then
                cp "$ICNS_FILE" "$MOUNT_DIR/.VolumeIcon.icns"
                /usr/bin/SetFile -a C "$MOUNT_DIR" 2>/dev/null || true
            fi

            # Unmount
            hdiutil detach "$MOUNT_DIR"

            # Convert to compressed DMG
            hdiutil convert "$DMG_TEMP" -format UDZO -o "$DMG_FINAL"

            # Clean up temp
            rm -f "$DMG_TEMP"

            # Set the custom icon on the .dmg file itself
            if [ -f "$ICNS_FILE" ]; then
                sips -i "$ICNS_FILE" &>/dev/null || true
                DeRez -only icns "$ICNS_FILE" > /tmp/googoo_icns.rsrc 2>/dev/null
                Rez -append /tmp/googoo_icns.rsrc -o "$DMG_FINAL" 2>/dev/null
                /usr/bin/SetFile -a C "$DMG_FINAL" 2>/dev/null || true
                rm -f /tmp/googoo_icns.rsrc
            fi
            ;;
        windows)
            echo "Creating Windows .exe installer..."
            JPACKAGE_ARGS+=(
                --type exe
                --win-dir-chooser
                --win-menu
                --win-shortcut
            )
            ;;
        *)
            echo "ERROR: Unsupported OS: ${OS}"
            exit 1
            ;;
    esac

    # Run jpackage only for Windows (macOS already created DMG)
    if [ "$OS" = "windows" ]; then
        jpackage "${JPACKAGE_ARGS[@]}"

        if [ $? -eq 0 ]; then
            echo "✓ Packaging successful"
        else
            echo "ERROR: Packaging failed"
            exit 1
        fi
    else
        echo "✓ DMG creation successful"
    fi

    echo ""
    echo "Package created in: ${PACKAGE_DIR}/output/"
    ls -lh "${PACKAGE_DIR}/output/"
}

# Cleanup old packages
cleanup() {
    echo "Cleaning up old packages..."
    rm -rf "${PACKAGE_DIR}"
    echo "✓ Cleanup complete"
    echo ""
}

# Main execution
main() {
    check_dependencies
    cleanup
    build_app
    prepare_icon
    package_app

    echo ""
    echo "========================================="
    echo "✓ Packaging Complete!"
    echo "========================================="
    echo ""
    echo "Your packaged application is ready in:"
    echo "  ${PACKAGE_DIR}/output/"
    echo ""

    case "${OS}" in
        macos)
            echo "To install on macOS:"
            echo "  1. Open the .dmg file"
            echo "  2. Drag ${APP_NAME}.app to Applications"
            ;;
        windows)
            echo "To install on Windows:"
            echo "  Run the .exe installer"
            ;;
    esac
    echo ""
}

# Run main function
main
