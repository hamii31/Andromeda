# Andromeda
[![DOI](https://zenodo.org/badge/1464688619.svg)](https://doi.org/10.5281/zenodo.24380973)

Andromeda is an Android local-network device and service discovery tool written in Java. It scans all local subnets (only /24 LAns) for hosts and allows the user to select one of the responsive hosts to
conduct a gentle TCP scan on all common TCP Ports.

## Requirements
- Android 7.0+

## Build

Open the project in AndroidIDE or Android Studio and build a debug APK:

```sh
./gradlew assembleDebug
```

## APK output:

```
app/build/outputs/apk/debug/app-debug.apk
```

## How To Use

0. Install Android IDE to build APK.
1. Install apk.
2. Connect the phone to your local Wi‑Fi.
3. Tap Find.
4. Tap on one of the found devices to conduct TCP scan.
5. Review the TCP service results.
