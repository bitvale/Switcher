rootProject.buildFileName = "build.gradle.kts"

enableFeaturePreview("VERSION_CATALOGS")

include(
    ":demo",
    ":library"
)

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }

    val agpVersion: String by settings
    val kotlinVersion: String by settings
    val gradleMavenPublishVersion: String by settings

    plugins {
        id("com.android.application") version agpVersion
        id("com.android.library") version agpVersion

        id("org.jetbrains.kotlin.jvm") version kotlinVersion
        id("org.jetbrains.kotlin.android") version kotlinVersion

        id("com.vanniktech.maven.publish") version gradleMavenPublishVersion
    }

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "com.android.application",
                "com.android.library" -> {
                    useModule("com.android.tools.build:gradle:$agpVersion")
                }
                "com.vanniktech.maven.publish" -> {
                    useModule(
                        "com.vanniktech:gradle-maven-publish-plugin:$gradleMavenPublishVersion"
                    )
                }
            }
        }
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("library") {
            val coreKtxVersion: String by settings
            alias("coreKtx")
                .to("androidx.core:core-ktx:$coreKtxVersion")
            val appcompatVersion: String by settings
            alias("appcompat")
                .to("androidx.appcompat:appcompat:$appcompatVersion")
            val constraintlayoutVersion: String by settings
            alias("constraintlayout")
                .to("androidx.constraintlayout:constraintlayout:$constraintlayoutVersion")
        }
    }
}
