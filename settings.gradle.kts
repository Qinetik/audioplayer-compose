pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.pkg.github.com/Qawaz/markdown-compose") {
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
    plugins {
        val kotlinVersion = extra["kotlin_version"] as String
        kotlin("jvm").version(kotlinVersion).apply(false)
        kotlin("android").version(kotlinVersion).apply(false)
        kotlin("multiplatform").version(kotlinVersion).apply(false)
        id("com.android.application").version(extra["agp_version"] as String).apply(false)
        id("com.android.library").version(extra["agp_version"] as String).apply(false)
        id("org.jetbrains.compose").version(extra["compose.jb.version"] as String).apply(false)
        kotlin("plugin.serialization").version(kotlinVersion).apply(false)
    }
}
rootProject.name = "AudioPlayerCompose"

include(":demo:android")
include(":demo:desktop")
include(":demo:common")
include(":demo:web")

include(":audioplayer")