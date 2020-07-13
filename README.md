# RPM Jukebox
A player for the RPM Challenge jukebox written in JavaFX

## Release Notes

### v5.0.0

* Converted to use JDK14
* JavaFX packages adapted from https://github.com/roskenet due to JDK11+ incompatibility

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
