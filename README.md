# SimplePdfReader - Jetpack Compose PDF reader
[![](https://jitpack.io/v/idesade/compose-pdf-reader.svg)](https://jitpack.io/#idesade/compose-pdf-reader)

SimplePdfReader is a PDF reader library written completely in Jetpack Compose, this was created using [PdfRender](https://developer.android.com/reference/android/graphics/pdf/PdfRenderer) and coroutine.

## Features

- Very simple
- Double tap to zoom and pan

# Demo

<img src="assets/demo.gif" alt="demo"/>

# Get Started
## Integrate
Add the JitPack maven repository
```gradle
maven { url "https://jitpack.io"  }
```
Add the dependency
```gradle
implementation("com.github.idesade:compose-pdf-reader:LATEST_VERSION")
```
## How to use
```kotlin
PdfReader(File(...))
```
