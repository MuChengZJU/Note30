// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.1" apply false
    id("org.jetbrains.kotlin.android") version "1.8.21" apply false
    id("org.jetbrains.kotlin.kapt") version "1.8.21" apply false
}

// Modern clean task to avoid deprecated buildDir getter
tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}