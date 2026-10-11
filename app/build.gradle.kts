// Applica il plugin Android e il compilatore Compose.
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    // Namespace delle classi Android generate, per esempio R.
    namespace = "com.example.perudo"
    // API usate per compilare; minSdk e targetSdk hanno ruoli distinti.
    compileSdk {
        version = release(37) {
            minorApiLevel = 0
        }
    }

    // Identit? installata, Android minimo (API 33), target (37) e versione distribuita.
    defaultConfig {
        applicationId = "com.example.perudo"
        minSdk = 33
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // Ottimizzazione del codice disattivata per questa configurazione release.
            optimization {
                enable = false
            }
        }
    }
    // Compatibilit? del codice Java a 11; il daemon Gradle usa invece un JDK 21.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    // Abilita la UI dichiarativa Jetpack Compose.
    buildFeatures {
        compose = true
    }
}

// La BOM allinea le versioni Compose; gli altri alias arrivano dal catalogo TOML.
dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    // Integra ViewModel con Compose e fornisce viewModel() alla schermata.
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    // Test JVM locali e test Android su dispositivo/emulatore.
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    // Strumenti di anteprima, ispezione e test disponibili solo nella build debug.
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}