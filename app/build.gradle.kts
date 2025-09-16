plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.smartcoblight"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.smartcoblight"
        minSdk = 29
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // MQTT客户端 - 使用更稳定的版本
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5") // 稳定版


    // JSON处理
    implementation("com.google.code.gson:gson:2.8.9")

    // 权限处理
    implementation("androidx.core:core:1.10.1")

    // SharedPreferences封装
    implementation("androidx.preference:preference:1.2.0")

    // 自定义View支持
    implementation("androidx.cardview:cardview:1.0.0")

    // 添加缺失的AndroidX依赖
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

// 解决Kotlin版本冲突 - 简单方法
configurations.all {
    resolutionStrategy {
        // 排除冲突的Kotlin JDK模块
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk7")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk8")
    }
}
