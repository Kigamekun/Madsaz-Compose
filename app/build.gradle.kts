import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "id.madsaz.app"
    compileSdk = 37

    defaultConfig {
        // Memperbarui app yang sudah terbit di Play (com.madsaz); kode tetap di namespace id.madsaz.app.
        applicationId = "com.madsaz"
        minSdk = 24
        targetSdk = 37
        // Harus di atas versionCode app lama (14, versi 1.2.1). CI mengirim -PversionCode=<nomor naik>; build lokal memakai nilai ini.
        versionCode = providers.gradleProperty("versionCode").map(String::toInt).orElse(100).get()
        versionName = "2.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // URL API: -PmadsazApiBaseUrl=... (uji lokal/CI) mengalahkan secrets.properties.
        val apiBaseUrl = providers.gradleProperty("madsazApiBaseUrl").orNull ?: run {
            val secretsFile = rootProject.file("secrets.properties")
            check(secretsFile.exists()) { "Missing secrets.properties (atau beri -PmadsazApiBaseUrl)" }
            Properties().apply { secretsFile.inputStream().use { load(it) } }
                .getProperty("madsazApiBaseUrl")?.takeIf { it.isNotBlank() }
                ?: error("Missing madsazApiBaseUrl in secrets.properties")
        }
        // Rilis memakai cleartext=false; URL LAN http dari secrets.properties baru gagal saat runtime, jadi gagalkan build rilis lebih awal.
        val releaseRequested = gradle.startParameter.taskNames.any { it.contains("release", ignoreCase = true) }
        check(!releaseRequested || (apiBaseUrl.startsWith("https://") && apiBaseUrl.trimEnd('/').endsWith("/api/v1"))) {
            "Build rilis butuh madsazApiBaseUrl berformat https://<host>/api/v1"
        }
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
    }

    // Upload key Play App Signing. Tanpa keystore.properties, build rilis tetap tak bertanda tangan.
    val keystoreFile = rootProject.file("keystore.properties")
    if (keystoreFile.exists()) {
        val keystore = Properties().apply { keystoreFile.inputStream().use { load(it) } }
        signingConfigs {
            create("release") {
                storeFile = rootProject.file(requireNotNull(keystore.getProperty("storeFile")) { "storeFile kosong di keystore.properties" })
                storePassword = requireNotNull(keystore.getProperty("storePassword")) { "storePassword kosong di keystore.properties" }
                keyAlias = requireNotNull(keystore.getProperty("keyAlias")) { "keyAlias kosong di keystore.properties" }
                keyPassword = requireNotNull(keystore.getProperty("keyPassword")) { "keyPassword kosong di keystore.properties" }
            }
        }
    }

    buildTypes {
        release {
            optimization {
                enable = true
            }
            signingConfig = signingConfigs.findByName("release")
        }
    }

    compileOptions {
        // java.time (Instant, LocalDate, DateTimeFormatter) baru ada di Android 8 (API 26); minSdk 24 membutuhkan desugaring.
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Bahasa yang dikirim: Indonesia (default values/), Inggris, Arab. LocaleConfig dibuat otomatis dari folder values-*;
    // res/resources.properties menyebut bahasa default. Pengguna memilih lewat layar Bahasa di aplikasi atau Pengaturan > Bahasa aplikasi (Android 13+).
    androidResources {
        generateLocaleConfig = true
        localeFilters += listOf("id", "en", "ar")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

}

if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}

dependencies {
    implementation("com.android.billingclient:billing:9.1.0")
    implementation(platform("com.google.firebase:firebase-bom:34.19.0"))
    implementation("com.google.firebase:firebase-messaging")
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.exifinterface)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.video)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.core.splashscreen)
    testImplementation(libs.junit)
    // org.json di android.jar hanyalah stub untuk unit test JVM; ApiClient memakai implementasi asli ini saat diuji.
    testImplementation(libs.org.json)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
