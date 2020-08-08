#!/bin/sh

VERSION="5.0.0"
BUILD_NUMBER="1"

CURRENT_YEAR="$(date +%Y)"

PROJECT_ROOT=`pwd`
APP_NAME="RPM Jukebox.app"
NATIVE_DIR="$PROJECT_ROOT/build/jpackage"
NATIVE_APP="$NATIVE_DIR/$APP_NAME"

SRC_DIR="$NATIVE_DIR/src"
SRC_APP="$SRC_DIR/$APP_NAME"
DEST_DIR="$NATIVE_DIR/dest"
DMG_OUTPUT_FILE="$DEST_DIR/rpm-jukebox-$VERSION.dmg"
TMP_DIR="$NATIVE_DIR/tmp"
JAR_PATH="$NATIVE_APP/Contents/app"
JAR_FILE="rpm-jukebox-$VERSION.jar"

APP_SIGNING_ID="Developer ID Application: $APPLE_DEV_ID"

# Run the build
./gradlew clean build jpackageImage -x test

# Replace the strings in the Info.plist file
pushd "$NATIVE_APP/Contents"
  perl -i -p0e "s|\<key>CFBundleVersion\<\/key\>\s*\<string\>$VERSION\<\/string\>|'<key>CFBundleVersion</key><string>$BUILD_NUMBER</string>'|se" Info.plist
  sed -i'.bak' -e "s/>Unknown</>public.app-category.music</g" Info.plist
  sed -i'.bak' -e "s/>Copyright (C) $CURRENT_YEAR</>(C) $CURRENT_YEAR Matt Parker</g" Info.plist

  rm -f Info.plist.bak
popd

pushd "$NATIVE_APP/Contents/runtime/Contents"
  perl -i -p0e "s|\<key>CFBundleVersion\<\/key\>\s*\<string\>$VERSION\<\/string\>|'<key>CFBundleVersion</key><string>$BUILD_NUMBER</string>'|se" Info.plist
  sed -i'.bak' -e "s/>com.oracle.java.uk.co.mpcontracting.rpmjukebox</>uk.co.mpcontracting.rpmjukebox</g" Info.plist

  rm -f Info.plist.bak
popd

# Remove attributes on the app
xattr -rc "$NATIVE_APP"

# Unpack jar, sign internal dylibs, and repack
rm -rf $TMP_DIR
mkdir -p $TMP_DIR
mv "$JAR_PATH/$JAR_FILE" "$TMP_DIR"

pushd "$TMP_DIR"
  jar xf "$JAR_FILE"
  rm "$JAR_FILE"
  find . -name *.dylib | xargs codesign -s "$APP_SIGNING_ID" -f -v
  jar cmf META-INF/MANIFEST.MF "$JAR_PATH/$JAR_FILE" *
popd

rm -rf $TMP_DIR

# Sign the app and the runtime
codesign -s "$APP_SIGNING_ID" --options runtime --entitlements macos.entitlements -f -v "$NATIVE_APP/Contents/runtime/Contents/MacOS/libjli.dylib"
codesign -s "$APP_SIGNING_ID" --options runtime --entitlements macos.entitlements -f -v "$NATIVE_APP/Contents/MacOS/libapplauncher.dylib"
codesign -s "$APP_SIGNING_ID" --options runtime --entitlements macos.entitlements -f -v "$NATIVE_APP/Contents/MacOS/RPM Jukebox"
codesign -s "$APP_SIGNING_ID" --options runtime --entitlements macos.entitlements -f -v "$NATIVE_APP"

# Create the source dir and move the patched and signed native app into it
rm -rf $SRC_DIR
mkdir -p $SRC_DIR
mv "$NATIVE_APP" $SRC_DIR

# Create the destination directory
rm -rf $DEST_DIR
mkdir -p $DEST_DIR

# Create the DMG
create-dmg \
  --volname "RPM Jukebox" \
  --background "./src/main/deploy/package/macosx/RPM Jukebox-background.png" \
  --window-pos 400 100 \
  --window-size 517 270 \
  --icon "RPM Jukebox.app" 120 135 \
  --hide-extension "RPM Jukebox.app" \
  --app-drop-link 390 135 \
  $DMG_OUTPUT_FILE \
  $SRC_DIR

# Sign the DMG
codesign -s "$APP_SIGNING_ID" --options runtime --entitlements macos.entitlements -vvvv --deep "$DMG_OUTPUT_FILE"

# Upload the DMG for verification
REQUEST_UUID=`xcrun altool -t osx -f "$DMG_OUTPUT_FILE" --primary-bundle-id "uk.co.mpcontracting.rpmjukebox-$VERSION" --notarize-app -u "$APPLE_ID" -p @keychain:"Apple Developer: $APPLE_ID" | grep RequestUUID | awk '{print $3}'`

# Wait for the DMG to be verified
while xcrun altool --notarization-info $REQUEST_UUID -u "$APPLE_ID" -p @keychain:"Apple Developer: $APPLE_ID" | grep "Status: in progress" > /dev/null; do
  echo "$REQUEST_UUID : Verification in progress..."
  sleep 30
done

# Attach the stamp to the DMG
xcrun stapler staple -v "$DMG_OUTPUT_FILE"

# Check the app and the DMG
spctl -vvv --assess --type exec "$SRC_APP"
codesign -vvv --deep --strict "$DMG_OUTPUT_FILE"
codesign -dvv "$DMG_OUTPUT_FILE"
