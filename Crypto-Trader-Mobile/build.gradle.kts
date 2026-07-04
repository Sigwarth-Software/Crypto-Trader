plugins {
    id("com.android.application") version "8.6.1"
    kotlin("android") version "2.1.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
    id("com.google.dagger.hilt.android") version "2.52"
    id("com.google.devtools.ksp") version "2.1.0-1.0.29"
}

kotlin { jvmToolchain(21) }

android {
    namespace = "org.cryptotrader.mobile"
    compileSdk = 36

    defaultConfig {
        applicationId = "org.cryptotrader.mobile"
        minSdk = 26
        //noinspection EditedTargetSdkVersion
        targetSdk = 36
        versionCode = 1
        versionName = "0.0.3"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.15" }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE*"
            excludes += "/META-INF/NOTICE*"
            excludes += "/META-INF/*.txt"
            excludes += "/META-INF/spring.*"
            excludes += "/META-INF/web-fragment.xml"
            excludes += "/META-INF/services/javax.servlet.ServletContainerInitializer"
            excludes += "/META-INF/INDEX.LIST"
            excludes += "**/spring-configuration-metadata.json"
            excludes += "**/additional-spring-configuration-metadata.json"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "21"
        freeCompilerArgs += "-Xskip-metadata-version-check"
    }
    buildToolsVersion = "36.0.0"
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val ktorVersion = "2.3.12"
val roomVersion = "2.6.1"
val accompanistVersion = "0.36.0"
val vicoVersion = "2.0.0"

//noinspection GradleDependency
dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.3")
    // Shared library modules
    implementation("org.cryptotrader:api-communication:0.0.6")
    testImplementation("org.cryptotrader:testing-infrastructure:0.0.1")
    // Compose BOM and core UI
    implementation(platform("androidx.compose:compose-bom:2024.09.01"))
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")

    // Images, animations, charts
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("com.airbnb.android:lottie-compose:6.5.2")
    implementation("com.patrykandpatrick.vico:compose:$vicoVersion")

    // Accompanist
    implementation("com.google.accompanist:accompanist-systemuicontroller:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-permissions:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-placeholder-material:$accompanistVersion")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // Paging
    implementation("androidx.paging:paging-runtime-ktx:3.3.2")
    implementation("androidx.paging:paging-compose:3.3.2")

    // Ktor Client
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-gson:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("io.ktor:ktor-client-websockets:$ktorVersion")

    // Room
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.52")
    ksp("com.google.dagger:hilt-compiler:2.52")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Baseline Profile Installer
    implementation("androidx.profileinstaller:profileinstaller:1.4.1")

    // Testing – JUnit 5 + Testing-Infrastructure
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.0")
    testImplementation("org.mockito:mockito-core:5.12.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.12.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("app.cash.turbine:turbine:1.1.0")
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("org.slf4j:slf4j-api:2.0.13")
    testImplementation("org.slf4j:slf4j-simple:2.0.13")
    testImplementation("org.jetbrains:annotations:24.1.0")

    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.09.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
