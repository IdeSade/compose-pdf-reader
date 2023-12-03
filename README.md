# SimplePdfReader - Jetpack Compose PDF reader

![GitHub release](https://img.shields.io/github/v/release/IdeSade/compose-pdf-reader)
![JitPack](https://img.shields.io/jitpack/version/com.github.idesade/compose-pdf-reader)

SimplePdfReader is a PDF reader library written completely in Jetpack Compose, this was created using [PdfRender](https://developer.android.com/reference/android/graphics/pdf/PdfRenderer) and coroutine.

## Features

- Very simple
- Zoom and pan
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
PdfReader(File("path/file.pdf"))
```