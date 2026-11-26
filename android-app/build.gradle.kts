// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}

// Valfritt: lägg byggfilerna utanför projektmappen, t.ex. när projektet ligger i OneDrive
// (OneDrive gör om filer i build/ till molnplatshållare och då kraschar bygget).
// Sätt i local.properties:  buildRoot=C:/Users/<namn>/AppData/Local/SchemaappenBuild
val localProps = java.util.Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}
localProps.getProperty("buildRoot")?.let { root ->
    allprojects {
        layout.buildDirectory.set(file("$root/${project.name}"))
    }
}
