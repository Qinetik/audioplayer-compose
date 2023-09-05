plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("maven-publish")
}

group = "com.wakaztahir"
version = property("version") as String

kotlin {
    androidTarget {
        publishLibraryVariants("release")
    }
    jvm("desktop")
    js(IR) {
        browser()
        binaries.executable()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {

            }
        }
        val desktopMain by getting {
            dependencies {

            }
        }
        val jsMain by getting {
            dependencies {

            }
        }
    }
}

android {
    namespace = "com.wakaztahir.audioplayercompose"
    compileSdk = 33
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

publishing {
    repositories {
        maven("https://maven.pkg.github.com/Qawaz/audioplayer-compose") {
            name = "GithubPackages"
            try {
                credentials {
                    username = (System.getenv("GPR_USER")).toString()
                    password = (System.getenv("GPR_API_KEY")).toString()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
}
