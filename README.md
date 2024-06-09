<!--suppress CheckImageSize -->
<img src="art/ic_launcher-web.png"  alt="PPPE application icon" width="100" height="100">  

[![Stand With Ukraine](https://raw.githubusercontent.com/vshymanskyy/StandWithUkraine/main/badges/StandWithUkraine.svg)](https://stand-with-ukraine.pp.ua)

PhoneProfilesPlusExtender (PPPE)
====================================

[![version](https://img.shields.io/badge/version-8.1.6-blue)](https://github.com/henrichg/PhoneProfilesPlusExtender/releases/tag/8.1.6)
[![Platform](https://img.shields.io/badge/platform-android-green.svg)](http://developer.android.com/index.html)
[![License](https://img.shields.io/hexpm/l/plug.svg)](https://github.com/henrichg/PhoneProfilesPlus/blob/master/LICENSE)
[![Crowdin](https://badges.crowdin.net/phoneprofilesplus/localized.svg)](https://crowdin.com/project/phoneprofilesplus)
[![Donate](https://img.shields.io/badge/Donate-PayPal-green.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=AF5QK49DMAL2U&currency_code=EUR)

### Sources of PhoneProfilesPlusExtender:

Use keyword "PhoneProfilesPlusExtender" for search this application in these stores:

__Droid-ify (F-Droid alternative, preferred over GitHub):__
- [PPPE release](https://apt.izzysoft.de/fdroid/index/apk/sk.henrichg.phoneprofilesplusextender)
- [Store applicaion (download)](https://apt.izzysoft.de/fdroid/index/apk/com.looker.droidify)
  &nbsp;&nbsp;&nbsp;_IzzyOnDroid repository is included_

__Neo Store (F-Droid alternative, preferred over GitHub):__
- [PPPE release](https://apt.izzysoft.de/fdroid/index/apk/sk.henrichg.phoneprofilesplusextender)
- [Store applicaion (download)](https://apt.izzysoft.de/fdroid/index/apk/com.machiav3lli.fdroid)
  &nbsp;&nbsp;&nbsp;_IzzyOnDroid repository is included_

__F-Droid:__
- [PPPE release](https://apt.izzysoft.de/fdroid/index/apk/sk.henrichg.phoneprofilesplusextender)
  &nbsp;&nbsp;&nbsp;_[How to add IzzyOnDroid repository to F-Droid application](https://apt.izzysoft.de/fdroid/index/info)_
- [Store application (download)](https://www.f-droid.org/)

__GitHub (better is using Droid-ify instead):__

- NOTE: Installation from downloaded apk is not recommended for Android 13+ for security reason. Use instead Droid-ify, if in your device is not supported application store, and install PhoneProfilesPlusExtender from it.

- [PPPE release (direct download)](https://github.com/henrichg/PhoneProfilesPlusExtender/releases/latest/download/PhoneProfilesPlusExtender.apk)
  &nbsp;&nbsp;&nbsp;_[Number of downloads by version](https://hanadigital.github.io/grev/?user=henrichg&repo=phoneprofilesplusextender)_

__If is not possible to install PhoneProfilesPlusExtender from the downloaded apk file directly on your device, you can install it from your computer.__
- [Show how](docs/install_apk_from_pc.md)

---

__What is PhoneProfilesPlusExtender:__

Android application - Extender for [PhoneProfilesPlus](https://github.com/henrichg/PhoneProfilesPlus).
- Accessibility service for profile parameter "Force stop applications"
- Accessibility service for profile parameter "Lock device"
- Accessibility service for event parameters "Application sensor"
- Accessibility service for event parameters "Orientation sensor"
- Accessibility service for event parameters "SMS/MMS sensor"
- Accessibility service for event parameters "Call sensor"
  \
  &nbsp;
- [Privacy Policy](https://henrichg.github.io/PhoneProfilesPlus/privacy_policy.html)

_**** Please report me bugs, comments and suggestions to my e-mail: <henrich.gron@gmail.com>. Speed up the especially bug fixes. Thank you very much. ****_

_*** Please help me with translation, thank you: <https://crowdin.com/project/phoneprofilesplus> ***_

##### Permissions
- __[Show it](docs/permissions.md)__

##### Screenshots
- [[1]](art/phoneScreenshots/01.png),
  [[2]](art/phoneScreenshots/02.png),
  [[3]](art/phoneScreenshots/03.png),
  [[4]](art/phoneScreenshots/04.png)

##### Supported Android versions

- From Android 8.0
- minSdkVersion = 26
- targetSdkVersion = 27
- compiledSdkVersion = 34

##### Required external libs - open-source

- AndroidX library: appcompat, localbroadcastmanager, splashscreen - https://developer.android.com/jetpack/androidx/versions
- ACRA - https://github.com/ACRA/acra
- guava - https://github.com/google/guava
- AutoService = https://github.com/google/auto/tree/main/service
- Multi-language_App (only modified class LocaleHelper.java) https://github.com/anurajr1/Multi-language_App
- AndroidHiddenApiBypass - https://github.com/LSPosed/AndroidHiddenApiBypass
- ToastCompat (as module, code modified) - https://github.com/PureWriter/ToastCompat
- Android-Accessibility-Utilities(only modified class A11NodeInfo - for debug only - code for log view hierarchy) - https://github.com/chriscm2006/Android-Accessibility-Utilities
- Android-Accessibility-Utilities(only modified class A11NodeInfoMatcher - for debug only - code for log view hierarchy) - https://github.com/chriscm2006/Android-Accessibility-Utilities
