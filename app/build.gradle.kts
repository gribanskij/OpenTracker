plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization.plugin)
    alias(libs.plugins.compose.plugin)
}

android {
    namespace = "com.gribansky.opentracker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.gribansky.opentracker"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
        compose = true
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.google.gms)
    implementation(libs.kotlin.serialization)
    implementation(libs.adapterdelegates4)
    implementation(libs.cardview)

    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation(libs.compose.navigation)

    implementation("androidx.compose.foundation:foundation")
    implementation ("androidx.compose.runtime:runtime")
    implementation ("androidx.compose.ui:ui")
    implementation ("androidx.compose.foundation:foundation")
    implementation ("androidx.compose.material:material")
    implementation ("androidx.compose.material:material-icons-extended")
    implementation ("androidx.activity:activity-compose:1.10.1")
    implementation ("androidx.navigation:navigation-compose:2.9.0")
    debugImplementation ("androidx.compose.ui:ui-tooling")


    testImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.compose.ui:ui-test")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}