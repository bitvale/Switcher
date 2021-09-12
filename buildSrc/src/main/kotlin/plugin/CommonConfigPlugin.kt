package plugin

import AndroidConfig
import GradlePluginId
import com.android.build.gradle.BaseExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project

class CommonConfigPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val androidExtension = project.extensions.getByName("android")

        (androidExtension as? BaseExtension)?.apply {
            val pluginManager = project.pluginManager
            val isAppModule = pluginManager.hasPlugin(GradlePluginId.ANDROID_APPLICATION)
            compileSdkVersion(AndroidConfig.COMPILE_SDK_VERSION)

            defaultConfig {
                if (isAppModule) {
                    applicationId = AndroidConfig.ID
                    vectorDrawables.useSupportLibrary = true
                }
                targetSdk = AndroidConfig.TARGET_SDK_VERSION
                minSdk = AndroidConfig.MIN_SDK_VERSION

                versionCode = AndroidConfig.VERSION_CODE
                versionName = AndroidConfig.VERSION_NAME

                testInstrumentationRunner = AndroidConfig.TEST_INSTRUMENTATION_RUNNER
                consumerProguardFile(AndroidConfig.CONSUMER_PROGUARD_FILE)
            }

            buildTypes {
                getByName("release") {
                    // For mock only, do not use in production!
                    signingConfig = signingConfigs.getByName("debug")
                }
            }

            compileOptions {
                val javaVersion = JavaVersion.VERSION_1_8
                targetCompatibility = javaVersion
                sourceCompatibility = javaVersion
            }

            buildFeatures.viewBinding = true
        }
    }
}
