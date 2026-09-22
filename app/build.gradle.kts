plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// رقم البناء من GitHub Actions عشان كل نسخة جديدة تتثبت فوق القديمة
val buildNumber = (System.getenv("GITHUB_RUN_NUMBER") ?: "1").toInt()

android {
    namespace = "com.ashraf.natiga"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ashraf.natiga"
        minSdk = 24          // Android 7+ (التقويم الهجري والقبطي مدمجين من هنا)
        targetSdk = 34
        versionCode = buildNumber
        versionName = "1.$buildNumber"
    }

    // مفتاح توقيع ثابت: كل تحديث يتثبت فوق اللي قبله من غير ما تمسح التطبيق
    signingConfigs {
        create("natiga") {
            storeFile = file("keystore/natiga.p12")
            storeType = "pkcs12"
            storePassword = "natiga123"
            keyAlias = "natiga"
            keyPassword = "natiga123"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("natiga")
        }
        debug { signingConfig = signingConfigs.getByName("natiga") }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
