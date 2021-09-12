plugins {
    id(GradlePluginId.ANDROID_LIBRARY)
    id(GradlePluginId.KOTLIN_ANDROID)
    id(GradlePluginId.COMMON_CONFIG_PLUGIN)
}

dependencies {
    implementation(library.coreKtx)
}
