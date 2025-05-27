fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## Android

### android test

```sh
[bundle exec] fastlane android test
```

Runs all the tests

### android generate_version_code

```sh
[bundle exec] fastlane android generate_version_code
```

Generate version code

### android setup_keystore

```sh
[bundle exec] fastlane android setup_keystore
```

Setup keystore properties from CI environment

### android build_apk

```sh
[bundle exec] fastlane android build_apk
```

Build apk file

### android build_release

```sh
[bundle exec] fastlane android build_release
```

Build release version (APK and AAB)

### android build_debug

```sh
[bundle exec] fastlane android build_debug
```

Build debug APK with versioning

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
