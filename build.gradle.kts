// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    //cap 02
    alias(libs.plugins.kotlin.serialization) apply false

    //cap 03 pligin de hilt para la inyeccion
    id("com.google.dagger.hilt.android") version "2.60.1" apply false
    //para precesar anotaciones
    id("com.google.devtools.ksp") version "2.3.6" apply false
    //cap 05
    alias(libs.plugins.room) apply false

}