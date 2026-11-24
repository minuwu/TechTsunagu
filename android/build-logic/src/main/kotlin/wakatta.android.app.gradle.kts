import com.android.build.api.dsl.ApplicationExtension
import java.io.File
import java.util.Properties

plugins {
    id("com.android.application")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

val versionProperties = Properties().apply {
    val propFile = rootProject.file("version.properties")
    if (propFile.exists()) {
        propFile.inputStream().use { load(it) }
    }
}
val vCode = versionProperties.getProperty("versionCode", "1").toInt()
val vName = versionProperties.getProperty("versionName", "1.0.0")

val keystoreProperties = Properties().apply {
    val propFile = rootProject.file("keystore.properties")
    if (propFile.exists()) {
        propFile.inputStream().use { load(it) }
    }
}
val keystoreFile = keystoreProperties.getProperty("storeFile")
    ?: System.getenv("WAKATTA_KEYSTORE_FILE")
val keystorePassword = keystoreProperties.getProperty("storePassword")
    ?: System.getenv("WAKATTA_KEYSTORE_PASSWORD")
val keyAlias = keystoreProperties.getProperty("keyAlias")
    ?: System.getenv("WAKATTA_KEY_ALIAS")
val keyPassword = keystoreProperties.getProperty("keyPassword")
    ?: System.getenv("WAKATTA_KEY_PASSWORD")

configure<ApplicationExtension> {
    compileSdk = 37

    defaultConfig {
        applicationId = "com.techtsunagu.wakatta"
        minSdk = 26
        targetSdk = 36
        versionCode = vCode
        versionName = vName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (!keystoreFile.isNullOrBlank() && File(keystoreFile).exists() &&
            !keystorePassword.isNullOrBlank() && !keyAlias.isNullOrBlank() && !keyPassword.isNullOrBlank()
        ) {
            create("release") {
                storeFile = File(keystoreFile)
                storePassword = keystorePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            }
        }
    }

    buildTypes {
        debug {
            buildConfigField("boolean", "USE_MOCK", "true")
            buildConfigField("String", "API_BASE_URL", "\"\"")
            buildConfigField("String", "UPDATE_MANIFEST_URL", "\"\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            val relSigning = signingConfigs.findByName("release")
            if (relSigning != null) {
                signingConfig = relSigning
            }

            ndk {
                abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a"))
            }

            buildConfigField("boolean", "USE_MOCK", "true")
            buildConfigField("String", "API_BASE_URL", "\"\"")
            buildConfigField("String", "UPDATE_MANIFEST_URL", "\"\"")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
