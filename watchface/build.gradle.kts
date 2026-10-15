plugins {
    alias(libs.plugins.android.application)
}

android {
    // No Kotlin/Java sources — this is a declarative Watch Face Format app
    enableKotlin = false
    namespace = "com.bwatch.o1"
    // android-36 is the platform installed in this environment; minSdk 33 = Wear OS 4 (Galaxy Watch 6)
    compileSdk = 36

    defaultConfig {
        applicationId = "com.bwatch.o1"
        minSdk = 33
        targetSdk = 34
        versionCode = 2
        versionName = "1.1.0"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            // shrinkResources must stay false so the WFF resources are not stripped
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}
