// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {

    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    alias(libs.plugins.android.application) apply false
    kotlin("jvm")
}
dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

kotlin {
    jvmToolchain(8)
}