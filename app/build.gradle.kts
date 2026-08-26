plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.emosafe"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.emosafe"
        minSdk = 24
        targetSdk = 34
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

    packaging {
        resources {
            excludes += "org/glassfish/jaxb/runtime/unmarshaller/Messages_ko.properties"
            excludes += "META-INF/DEPENDENCIES"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation(libs.common)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.intuit.sdp:sdp-android:1.1.1")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.airbnb.android:lottie:6.5.2")
    implementation("org.tensorflow:tensorflow-lite:2.16.1")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
//    implementation("org.jpmml:pmml-evaluator:1.5.10") {
//        exclude(group = "com.google.guava", module = "listenablefuture")
//    }
//    implementation("org.jpmml:pmml-model:1.5.10")
//    implementation("jakarta.xml.bind:jakarta.xml.bind-api:3.0.1")
//    implementation("org.glassfish.jaxb:jaxb-runtime:3.0.1")
    // Include guava, excluding listenablefuture
//    implementation("com.google.guava:guava:33.3.1-jre") {
//        exclude(group = "com.google.guava", module = "listenablefuture")
//    }

    // Remove this line if not needed, or just comment it out:
    // implementation("com.google.guava:listenablefuture:1.0")
//    implementation("org.jpmml:pmml-evaluator:1.6.6") {
//        exclude(group = "com.google.guava", module = "listenablefuture")
//    }
//    implementation("com.google.guava:guava:33.3.1-jre") {
//        exclude(group = "com.google.guava", module = "listenablefuture")
//    }
}