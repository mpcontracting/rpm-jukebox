#!/bin/sh

VERSION="5.0.0"
BUILD_NUMBER="13"

CURRENT_YEAR="$(date +%Y)"

PROJECT_ROOT=`pwd`
APP_NAME="RPM Jukebox.app"
NATIVE_DIR="$PROJECT_ROOT/build/jpackage"
NATIVE_APP="$NATIVE_DIR/$APP_NAME"

SRC_DIR="$NATIVE_DIR/src"
SRC_APP="$SRC_DIR/$APP_NAME"
DEST_DIR="$NATIVE_DIR/dest"
PKG_OUTPUT_FILE="$DEST_DIR/rpm-jukebox-$VERSION.pkg"
TMP_DIR="$NATIVE_DIR/tmp"
JAR_PATH="$NATIVE_APP/Contents/app"
JAR_FILE="rpm-jukebox-$VERSION.jar"

APP_SIGNING_ID="Apple Distribution: $APPLE_DEV_ID"
INSTALLER_SIGNING_ID="3rd Party Mac Developer Installer: $APPLE_DEV_ID"

# Run the build
./gradlew clean build jpackageImage -x test

# Copy the Java executables with the patched Info.plist entries
pushd "$NATIVE_APP/Contents/runtime/Contents/Home/bin"
  rm -f *
  cp "$HOME/Development/Zulu/patched-java-01/"* ./
popd

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
  find . -name *.dylib | xargs codesign -s "$APP_SIGNING_ID" --options runtime --entitlements "$PROJECT_ROOT/app-store.entitlements" -f -v
  jar cmf META-INF/MANIFEST.MF "$JAR_PATH/$JAR_FILE" *
popd

rm -rf $TMP_DIR

# Sign the app and the runtime
pushd "$NATIVE_APP/Contents/runtime/Contents/Home"
  find . -name *.dylib | xargs codesign -s "$APP_SIGNING_ID" --options runtime --entitlements "$PROJECT_ROOT/app-store.entitlements" -f -v
popd

codesign -s "$APP_SIGNING_ID" --options runtime --entitlements app-store.entitlements -f -v "$NATIVE_APP/Contents/runtime/Contents/Home/bin/java"
codesign -s "$APP_SIGNING_ID" --options runtime --entitlements app-store.entitlements -f -v "$NATIVE_APP/Contents/runtime/Contents/Home/bin/jrunscript"
codesign -s "$APP_SIGNING_ID" --options runtime --entitlements app-store.entitlements -f -v "$NATIVE_APP/Contents/runtime/Contents/Home/bin/keytool"
codesign -s "$APP_SIGNING_ID" --options runtime --entitlements app-store.entitlements -f -v "$NATIVE_APP/Contents/runtime/Contents/Home/lib/jspawnhelper"
codesign -s "$APP_SIGNING_ID" --options runtime --entitlements app-store.entitlements -f -v "$NATIVE_APP/Contents/runtime/Contents/MacOS/libjli.dylib"
codesign -s "$APP_SIGNING_ID" --options runtime --entitlements app-store.entitlements -f -v "$NATIVE_APP/Contents/MacOS/libapplauncher.dylib"
codesign -s "$APP_SIGNING_ID" --options runtime --entitlements app-store.entitlements -f -v "$NATIVE_APP/Contents/MacOS/RPM Jukebox"
codesign -s "$APP_SIGNING_ID" --options runtime --entitlements app-store.entitlements -f -v "$NATIVE_APP"

# Create the source dir and move the patched and signed native app into it
rm -rf $SRC_DIR
mkdir -p $SRC_DIR
mv "$NATIVE_APP" $SRC_DIR

# Create the destination directory
rm -rf $DEST_DIR
mkdir -p $DEST_DIR

# Create and sign the package
productbuild --component "$SRC_APP" /Applications/ "$PKG_OUTPUT_FILE" --sign "$INSTALLER_SIGNING_ID"
