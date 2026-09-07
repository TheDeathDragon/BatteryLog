import com.android.build.gradle.internal.api.ApkVariantOutputImpl
import java.text.SimpleDateFormat
import java.util.*

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
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

    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.constraintlayout)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.common.java8)

    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.dynamic.features.fragment)

    implementation(libs.material)
    implementation(libs.play.services.location)
    implementation(libs.mpandroidchart)
}
