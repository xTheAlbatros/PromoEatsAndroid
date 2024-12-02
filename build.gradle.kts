// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    kotlin("jvm") version "1.8.0" apply false
    kotlin("kapt") version "1.8.0" apply false
}