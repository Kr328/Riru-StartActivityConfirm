# Riru-ForceDisplayCutoutMode

A module of [Riru](https://github.com/RikkaApps/Riru). Override activity display cutout mode.

## Requirements

* [Riru](https://github.com/RikkaApps/Riru) installed.
* Android Pie

## Settings

- Add application to **blacklist** ( will not change anything for this package )

  Create a empty file which file name is target package name in **/data/misc/riru/modules/display_cutout_mode/blacklist/**

## Build

  1.Install JDK ,Gradle ,Android SDK ,Android NDK

  2.Configure with your environment [local.properties](https://github.com/Kr328/Riru-ForceDisplayCutoutMode/blob/master/local.properties)

  3.Run command 

``` Gradle 
./gradlew build
```
  4.Pick magisk-module.zip from build/outputs
