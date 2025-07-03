import com.android.build.gradle.internal.api.ApkVariantOutputImpl
import java.text.SimpleDateFormat
import java.util.*

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

val projectName = "BatteryLog"
val apkFileName = "$projectName.apk"
val currentBuildTime: String = SimpleDateFormat("yy/MM/dd HH:mm:ss").format(Date())
val currentVersionDate: Int = SimpleDateFormat("yyMMdd").format(Date()).toInt()
val currentVersion: String = SimpleDateFormat("yy.MM.dd").format(Date())
val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss")
val date: String = dateFormat.format(Date())

android {
    namespace = "la.shiro.batterylog"
    compileSdk = 36

    defaultConfig {
        applicationId = "la.shiro.batterylog"
        minSdk = 28
        targetSdk = 34
        versionCode = currentVersionDate
        versionName = currentVersion
        buildConfigField("long", "VERSION_CODE", "$currentVersionDate")
        buildConfigField("String", "BUILD_TIME", "\"$currentBuildTime\"")
        buildConfigField("String", "APP_NAME", "\"$projectName\"")
        resourceConfigurations += listOf("zh-rCN")
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    dependenciesInfo {
        includeInApk = false
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    applicationVariants.all {
        outputs.all {
            if (this is ApkVariantOutputImpl) {
                outputFileName = apkFileName
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    packagingOptions {
        resources {
            excludes += setOf("META-INF/atomicfu.kotlin_module")
        }
    }
}

allprojects {
    gradle.projectsEvaluated {
        tasks.register<Zip>("zipReleaseApkAndAssets") {
            val apkFile = file("release/$apkFileName")
            val outputDir = file("dist")
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }
            from(apkFile) {
                into(projectName)
            }

            archiveFileName.set("${projectName}_${date}.zip")
            destinationDirectory.set(outputDir)

            doLast {
                println("ZIP file created at: ${outputDir.absolutePath}/${archiveFileName.get()}")
            }
        }
        tasks.register<Zip>("zipDebugSymbols") {
            val mappingFile = file("build/outputs/mapping/release/mapping.txt")
            val outputDir = file("dist")
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }
            from(mappingFile) {
                into("DebugSymbols")
            }
            archiveFileName.set("${projectName}_${date}_Symbols.zip")
            destinationDirectory.set(outputDir)
            doLast {
                println("Symbols file created at: ${outputDir.absolutePath}/${archiveFileName.get()}")
            }
        }

        tasks.getByName("assembleRelease").finalizedBy("zipReleaseApkAndAssets")
        tasks.getByName("zipReleaseApkAndAssets").finalizedBy("zipDebugSymbols")
    }
}

dependencies {

    ksp("androidx.room:room-compiler:2.7.2")
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.fragment:fragment-ktx:1.8.8")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")

    implementation("androidx.room:room-runtime:2.7.2")
    implementation("androidx.room:room-ktx:2.7.2")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.9.1")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.9.1")

    implementation("androidx.camera:camera-camera2:1.4.2")
    implementation("androidx.camera:camera-lifecycle:1.4.2")
    implementation("androidx.camera:camera-view:1.4.2")

    implementation("androidx.navigation:navigation-fragment-ktx:2.9.1")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.1")
    implementation("androidx.navigation:navigation-dynamic-features-fragment:2.9.1")

    implementation("com.google.android.material:material:1.12.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.2.0")
}
