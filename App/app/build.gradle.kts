plugins {
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt") // Adiciona o Kapt
    alias(libs.plugins.android.application)
    id("kotlin-parcelize")

}

kapt {
    correctErrorTypes = true
    useBuildCache = true
}

android {
    namespace = "com.teste.projeto_3"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.teste.projeto_3"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        viewBinding = true
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
        jvmTarget = "11" // Configurar para 11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity:1.9.3")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.7")
    testImplementation(libs.junit)

    // DEPENDÊNCIAS PARA ACESSAR A API
    // DEPENDÊNCIAS PARA ACESSAR A API
    implementation("com.google.code.gson:gson:2.10.1") // Atualizado
    implementation("com.squareup.okhttp3:okhttp:4.11.0") // Atualizado

    // ROOM PARA BANCO DE DADOS LOCAL
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.room:room-runtime:2.5.2") // Última versão
    kapt("androidx.room:room-compiler:2.5.2")        // Para geração de código
    implementation("androidx.room:room-ktx:2.5.2")     // Extensões Kotlin para Room

    implementation("com.squareup.retrofit2:retrofit:2.4.0")
    implementation("com.squareup.retrofit2:converter-gson:2.3.0")
    implementation("com.squareup.picasso:picasso:2.8")

    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}