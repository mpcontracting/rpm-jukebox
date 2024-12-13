# RPM Jukebox

[![Build and test](https://github.com/mpcontracting/rpm-jukebox/actions/workflows/on_push_to_main.yaml/badge.svg?branch=main)](https://github.com/mpcontracting/rpm-jukebox/actions/workflows/on_push_to_main.yaml)

A jukebox for the RPM Challenge written in JavaFX and Spring Boot

## Running in Maven

Run with the following Maven command. The JavaFX SDK is not required to run the application with Maven.

```shell
./mvnw javafx:run
```

## Running in IntelliJ

Download the JavaFX SDK v23.0.1 [from here](https://gluonhq.com/products/javafx/)

Add a run configuration with the following as VM options

```
-Ddirectory.config=.rpmjukeboxdev 
-Dconsole.log.only=true 
--module-path <path-to-sdk>/javafx-sdk-22.0.1/lib 
--add-modules=javafx.controls,javafx.fxml,javafx.media 
--enable-native-access=ALL-UNNAMED
```

## Building a release

Download the JavaFX jmods v23.0.1 [from here](https://gluonhq.com/products/javafx/)

Update the `javafx.jmods.location` property in the POM to point to the installed jmods.

Run the following Maven command.

```shell
./mvnw clean package jlink:jlink jpackage:jpackage
```

## Release Notes

### v6.0.1

* Upgraded to JavaFX 23.0.1
* Disabled caching as JavaFX 23 won't play files that don't have a file suffix :-(

### v6.0.0

* Converted to use JDK21 and JavaFX 22.0.1
* Changed build to use Maven rather than Gradle

### v5.0.0

* Converted to use JDK14
* JavaFX packages adapted from https://github.com/roskenet due to JDK11+ incompatibility
* To build you will need a JDK with JavaFX included, e.g. Liberica

### v4.1.1

* Added menus and keyboard bindings
* Squashed some bugs

### v4.0.0

* Updated keys on AWS to handle data file deletions
* Search now works for tracks with punctuation and accents
* MacOS build now needs create-dmg as the JDK javapackager doesn't work above High Sierra

```brew install create-dmg```

### v3.0.0

* Music files now stored on AWS
* Jukebox is now in a stand alone repository

### v2.0.5

* Automatically re-index data with a new version

### v2.0.4

* Minor fixes for the new 2018 data format

### v2.0.3

* Fixed issue where the jukebox couldn't download the data file if behind a proxy

### v2.0.2

* Fixed MP3 format detection

### v2.0.1

* Added Linux support
* Added a splash screen
* Updated bundled Java version
* General bug fixes

### v2.0.0

* Added proxy support
* Refactored the codebase to make it more robust

### v1.0.3

* Added a year filter to the search and random playlist function

### v1.0.2

* Search index now stores accented characters correctly
* General bug fixes

### v1.0.1

* Added an alert if a new version is released
* macOS notification center integration
* Track slider is now more accurate
* General bug fixes

### v1.0.0

* Playlists can now be re-ordered
* Fixed some memory issues
* General bug fixes
