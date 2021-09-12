plugins {
    id(GradlePluginId.ANDROID_APPLICATION)
    id(GradlePluginId.KOTLIN_ANDROID)
    id(GradlePluginId.COMMON_CONFIG_PLUGIN)
}

dependencies {
    implementation(project(":library"))
    implementation(library.coreKtx)
    implementation(library.appcompat)
    implementation(library.constraintlayout)
}
