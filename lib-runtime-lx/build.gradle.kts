plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "io.neta.music.lxruntime"
    compileSdk = libs.versions.compileSdk.get().toInt()
    buildToolsVersion = libs.versions.buildTools.get()
    // 上游用 27.0.12077973；本机该版本损坏且 dl.google.com 不可达，换成本地完好的 26.1
    ndkVersion = "26.1.10909125"

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        ndk.abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        consumerProguardFiles("consumer-rules.pro")
        externalNativeBuild {
            cmake { targets += "lx-runtime" }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/jni/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}
