# Andromeda
[![DOI](https://zenodo.org/badge/1464688619.svg)](https://doi.org/10.5281/zenodo.24380973)

Andromeda is an Android local-network device and service discovery tool written in Android IDE using Java. It scans a local subnet for hosts responding to a hardcoded TCP ports list. 
The UI allows the user to select one of the responsive hosts and conduct a deeper port scan (SSH, HTTP, HTTPS, SMB, IPP, RDP). More features to be added. 

## Requirements

- Android device connected to Wi‑Fi
- Android 7.0+ recommended
- Internet permission enabled in `AndroidManifest.xml`

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

0. Install Android IDE.
1. Install apk.
2. Open application.
3. Connect the phone to your local Wi‑Fi.
4. Enter a subnet prefix in the predefined field. (default: 192.168.1.).
5. Tap Find responsive devices.
6. Select a device IP.
7. Review the TCP service results.
