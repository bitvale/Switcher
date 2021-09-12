import java.util.Properties
import java.io.FileInputStream

plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("common-config-plugin") {
            id = "common-config-plugin"
            implementationClass = "plugin.CommonConfigPlugin"
        }
    }
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    val propsFile = project.file("../gradle.properties")
    val properties = Properties()
    properties.load(FileInputStream(propsFile))

    val agpVersion: String by properties
    val kotlinVersion: String by properties

    implementation("com.android.tools.build:gradle:$agpVersion")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
}
