# PacButton

<img src="/art/preview.gif" alt="sample" title="sample" width="320" height="600" align="right" vspace="52" />

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Platform](https://img.shields.io/badge/platform-android-green.svg)](http://developer.android.com/index.html)
[![API](https://img.shields.io/badge/API-19%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=19)

Created this cool [video/photo switch animation](https://dribbble.com/shots/5487871-Video-Photo-Switcher-Exploration) from [Oleg Frolov](https://dribbble.com/Volorf)  as android library. 

USAGE
-----

Just add PacButton view in your layout XML and PacButton library in your project via Gradle:

```gradle
dependencies {
  implementation 'com.bitvale:pacbutton:1.0.0'
}
```

XML
-----

```xml
<com.bitvale.pacbutton.PacButton
    android:id="@+id/pac_button"
    android:layout_width="@dimen/pac_size"
    android:layout_height="@dimen/pac_size"
    app:topIcon="@drawable/ic_video"
    app:bottomIcon="@drawable/ic_photo"
    app:iconHeight="@dimen/icon_size"
    app:iconWidth="@dimen/icon_size"
    app:pacGradientColor_1="@color/gradient_color_1"
    app:pacGradientColor_2="@color/gradient_color_2" />
```

You must use the following properties in your XML to change your PacButton.


##### Properties:

* `app:topIcon`                     (drawable)  -> default  none
* `app:bottomIcon`                  (drawable)  -> default  none
* `app:iconHeight`                  (dimension) -> default  none
* `app:iconWidth`                   (dimension) -> default  none
* `app:pacColor`                    (color)     -> default  none
* `app:pacGradientColor_1`          (color)     -> default  #7651F8
* `app:pacGradientColor_2`          (color)     -> default  #E74996

You can use solid color with pacColor property or gradient with pacGradientColor properties.

Kotlin
-----

```kotlin
pac_button.setSelectAction {
    if (it) some_image.setImageResource(R.drawable.ic_video_cam)
    else some_image.setImageResource(R.drawable.ic_photo_cam)
}

pac_button.setAnimationUpdateListener { progress ->
    some_image.alpha = 1 - progress
}
```

LICENCE
-----

LavaFab by [Alexander Kolpakov](https://play.google.com/store/apps/dev?id=7044571013168957413) is licensed under an [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).