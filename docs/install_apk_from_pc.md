How to install PhoneProfilesPlusExtender from PC
================================================

##### If it is not possible to install PhoneProfilesPlusExtender from GitHub or other sources directly on the device, then it can be installed from PC. This is done through your PC with installed adb.

If you do not have adb installed on your PC:
1. Download SDK Platform Tools from:

<https://developer.android.com/studio/releases/platform-tools.html>

2. Extract zip contents.

##### How to istall PhoneProfilesPlusExtender:

1. Download PhoneProfilesPlusExtender.apk from GitHub:

<https://github.com/henrichg/PhoneProfilesPlusExtender/releases/latest/download/PhoneProfilesPlusExtender.apk>

2. On your device, go into Settings > About > Software Information and click on Build Number 7 times. This will unlock and display Developer Options in Settings.
3. Go into Settings > Developer Options and enable USB Debugging.
4. On your PC, open a Command Prompt (Windows), or Terminal (Linux, OSX). Navigate to the folder where you extracted your adb files, and execute the following command:

- for Windows:
  `adb install [patch to apk in PC]\PhoneProfilesPlusExtender.apk`
- for Linux, OSX, macOS:
  `adb install [patch to apk in PC]/PhoneProfilesPlusExtender.apk`

5. After successful execution, PhoneProfilePlusExtender will be installed in device.

