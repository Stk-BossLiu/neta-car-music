pluginManagement {
    // 本机到 dl.google.com 不稳定时 com.android.library 的插件 marker 拉不到；
    // marker 本身只是指向 com.android.tools.build:gradle 的跳板，这里直接映射到同一构件
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.android.library" && requested.version != null) {
                useModule("com.android.tools.build:gradle:${requested.version}")
            }
        }
    }
    repositories {
        // mavenLocal 放最前：断网期间从镜像手动补进 ~/.m2 的构件从这里走，其余命中本地依赖缓存
        mavenLocal()
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        maven("https://maven.aliyun.com/repository/central")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // mavenLocal 放最前：断网期间从镜像手动补进 ~/.m2 的构件从这里走，其余命中本地依赖缓存
        mavenLocal()
        google()
        mavenCentral()
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/central")
    }
}

rootProject.name = "neta-car-music"
include(":app")
include(":baselineprofile")
include(":lib-decoder-flac")
include(":lib-runtime-lx")
