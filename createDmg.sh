#!/bin/sh

NATIVE_DIR="./build/jfx/native"
NATIVE_APP="$NATIVE_DIR/RPM Jukebox.app"

DMG_OUTPUT_DIR="$NATIVE_DIR/dmg"
DMG_OUTPUT_FILE="$DMG_OUTPUT_DIR/rpm-jukebox-4.0.0.dmg"
SOURCE_DIR="$NATIVE_DIR/source"

rm -rf $SOURCE_DIR
mkdir -p $SOURCE_DIR

rm -rf $DMG_OUTPUT_DIR
mkdir -p $DMG_OUTPUT_DIR

cp -R "$NATIVE_APP" $SOURCE_DIR

create-dmg \
  --volname "RPM Jukebox" \
  --background "./src/main/deploy/package/macosx/RPM Jukebox-background.png" \
  --window-pos 400 100 \
  --window-size 517 270 \
  --icon "RPM Jukebox.app" 120 135 \
  --hide-extension "RPM Jukebox.app" \
  --app-drop-link 390 135 \
  $DMG_OUTPUT_FILE \
  $SOURCE_DIR
