plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
}

android {
    namespace = "com.idesade.compose"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
}

kotlin {
    jvmToolchain(18)
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui.util)
    implementation(libs.compose.material3)
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.github.idesade"
            artifactId = "compose-pdf-reader"
            version = "1.0.0-alpha04"

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}