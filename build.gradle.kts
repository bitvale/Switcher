import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id(GradlePluginId.ANDROID_APPLICATION) apply false
    id(GradlePluginId.KOTLIN_ANDROID) apply false
    id(GradlePluginId.COMMON_CONFIG_PLUGIN) apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
