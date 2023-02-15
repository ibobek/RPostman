import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    this@Build_gradle.rpostman.apply {
        kotlin("multiplatform") version versions.kotlin
        id("org.jetbrains.compose") version versions.compose
    }
}

repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

group = "com.rpostman"
version = "1.0-SNAPSHOT"

kotlin {
    jvmToolchain(19)
    jvm {
        compilations.all {
            this.kotlinOptions {
                jvmTarget = "19"
            }
        }
    }

    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.foundation)
                implementation(compose.desktop.currentOs)
                implementation(compose.material)
                implementation(compose.materialIconsExtended)

                implementation(rpostman.turtle)
                implementation(rpostman.jproc)
                implementation(rpostman.coroutines)
                implementation(rpostman.coroutines.jvm)
                implementation(rpostman.coroutines.jdk8)
                implementation(rpostman.stdlib)
                implementation(rpostman.stdlib.jdk8)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.rpostman.EntryPointKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "RPostman"
            packageVersion = "1.0.0"

            macOS {
                jvmArgs(
                    "-Dapple.awt.application.appearance=system",
                    "-Dapple.awt.enableTemplateImages=true"
                )
            }
        }
    }
}