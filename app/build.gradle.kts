import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.application")
    id("com.mikepenz.aboutlibraries.plugin")
    kotlin("android")
    kotlin("plugin.serialization")
    id("com.github.zellius.shortcut-helper")
    id("com.squareup.sqldelight")
}

if (gradle.startParameter.taskRequests.toString().contains("Standard")) {
    apply<com.google.gms.googleservices.GoogleServicesPlugin>()
    apply<com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsPlugin>()
}

shortcutHelper.setFilePath("./shortcuts.xml")

val SUPPORTED_ABIS = setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")

android {
    namespace = "eu.kanade.tachiyomi"
    compileSdk = AndroidConfig.compileSdk
    ndkVersion = AndroidConfig.ndk

    defaultConfig {
        applicationId = "xyz.jmir.tachiyomi.mi"
        minSdk = AndroidConfig.minSdk
        targetSdk = AndroidConfig.targetSdk
        versionCode = 81
        versionName = "0.13.5.0"

        buildConfigField("String", "COMMIT_COUNT", "\"${getCommitCount()}\"")
        buildConfigField("String", "COMMIT_SHA", "\"${getGitSha()}\"")
        buildConfigField("String", "BUILD_TIME", "\"${getBuildTime()}\"")
        buildConfigField("boolean", "INCLUDE_UPDATER", "false")
        buildConfigField("boolean", "PREVIEW", "false")

        // Please disable ACRA or use your own instance in forked versions of the project
        //buildConfigField("String", "ACRA_URI", "\"https://acra.jmir.xyz/report\"")

        ndk {
            abiFilters += SUPPORTED_ABIS
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    splits {
        abi {
            isEnable = true
            reset()
            include(*SUPPORTED_ABIS.toTypedArray())
            isUniversalApk = true
        }
    }

    buildTypes {
        named("debug") {
            versionNameSuffix = "-${getCommitCount()}"
            applicationIdSuffix = ".debug"
        }
        named("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles("proguard-android-optimize.txt", "proguard-rules.pro")
        }
        create("preview") {
            initWith(getByName("release"))
            buildConfigField("boolean", "PREVIEW", "true")

            val debugType = getByName("debug")
            signingConfig = debugType.signingConfig
            versionNameSuffix = debugType.versionNameSuffix
            applicationIdSuffix = debugType.applicationIdSuffix
        }
    }

    sourceSets {
        getByName("preview").res.srcDirs("src/debug/res")
    }

    flavorDimensions.add("default")

    productFlavors {
        create("standard") {
            buildConfigField("boolean", "INCLUDE_UPDATER", "true")
            dimension = "default"
        }
        create("dev") {
            resourceConfigurations.addAll(listOf("en", "de", "ar", "xxhdpi"))
            dimension = "default"
        }
    }

    packagingOptions {
        resources.excludes.addAll(listOf(
            "META-INF/DEPENDENCIES",
            "LICENSE.txt",
            "META-INF/LICENSE",
            "META-INF/LICENSE.txt",
            "META-INF/README.md",
            "META-INF/NOTICE",
            "META-INF/*.kotlin_module",
            "META-INF/*.version",
        ))

        jniLibs.pickFirsts.addAll(listOf(
            "**/libavcodec.so",
            "**/libavdevice.so",
            "**/libavfilter.so",
            "**/libavformat.so",
            "**/libavutil.so",
            "**/libswresample.so",
            "**/libswscale.so",
            "**/libc++_shared.so",
        ))
    }

    dependenciesInfo {
        includeInApk = false
    }

    buildFeatures {
        viewBinding = true
        compose = true

        // Disable some unused things
        aidl = false
        renderScript = false
        shaders = false
    }

    lint {
        disable.addAll(listOf("MissingTranslation", "ExtraTranslation"))
        abortOnError = false
        checkReleaseBuilds = false
    }

    composeOptions {
        kotlinCompilerExtensionVersion = compose.versions.compiler.get()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    sqldelight {
        database("Database") {
            packageName = "eu.kanade.tachiyomi"
            dialect = "sqlite:3.24"
            sourceFolders = listOf("sqldelight")
        }
        database("AnimeDatabase") {
            packageName = "eu.kanade.tachiyomi.mi"
            dialect = "sqlite:3.24"
            sourceFolders = listOf("sqldelightanime")
        }
    }
}

dependencies {
    // Compose
    implementation(compose.activity)
    implementation(compose.foundation)
    implementation(compose.material3.core)
    implementation(compose.material3.windowsizeclass)
    implementation(compose.material3.adapter)
    implementation(compose.material.icons)
    implementation(compose.animation)
    implementation(compose.animation.graphics)
    implementation(compose.ui.tooling)
    implementation(compose.ui.util)
    implementation(compose.accompanist.webview)
    implementation(compose.accompanist.swiperefresh)
    implementation(compose.accompanist.flowlayout)

    implementation(androidx.paging.runtime)
    implementation(androidx.paging.compose)

    implementation(libs.bundles.sqlite)
    implementation(androidx.sqlite)
    implementation(libs.sqldelight.android.driver)
    implementation(libs.sqldelight.coroutines)
    implementation(libs.sqldelight.android.paging)

    implementation(kotlinx.reflect)
    implementation(kotlinx.bundles.coroutines)

    // Source models and interfaces from Tachiyomi 1.x
    implementation(libs.tachiyomi.api)

    // AndroidX libraries
    implementation(androidx.annotation)
    implementation(androidx.appcompat)
    implementation(androidx.biometricktx)
    implementation(androidx.constraintlayout)
    implementation(androidx.coordinatorlayout)
    implementation(androidx.corektx)
    implementation(androidx.splashscreen)
    implementation(androidx.recyclerview)
    implementation(androidx.swiperefreshlayout)
    implementation(androidx.viewpager)

    implementation(androidx.bundles.lifecycle)

    // Job scheduling
    implementation(androidx.bundles.workmanager)

    // RX
    implementation(libs.bundles.reactivex)
    implementation(libs.flowreactivenetwork)

    // Network client
    implementation(libs.bundles.okhttp)
    implementation(libs.okio)

    // TLS 1.3 support for Android < 10
    implementation(libs.conscrypt.android)

    // Data serialization (JSON, protobuf)
    implementation(kotlinx.bundles.serialization)

    // JavaScript engine
    implementation(libs.bundles.js.engine)

    // HTML parser
    implementation(libs.jsoup)

    // Disk
    implementation(libs.disklrucache)
    implementation(libs.unifile)
    implementation(libs.junrar)

    // Preferences
    implementation(libs.preferencektx)
    implementation(libs.flowpreferences)

    // Model View Presenter
    implementation(libs.bundles.nucleus)

    // Dependency injection
    implementation(libs.injekt.core)

    // Image loading
    implementation(libs.bundles.coil)

    implementation(libs.subsamplingscaleimageview) {
        exclude(module = "image-decoder")
    }
    implementation(libs.image.decoder)

    // Sort
    implementation(libs.natural.comparator)

    // UI libraries
    implementation(libs.material)
    implementation(libs.androidprocessbutton)
    implementation(libs.flexible.adapter.core)
    implementation(libs.flexible.adapter.ui)
    implementation(libs.viewstatepageradapter)
    implementation(libs.photoview)
    implementation(libs.directionalviewpager) {
        exclude(group = "androidx.viewpager", module = "viewpager")
    }
    implementation(libs.insetter)
    implementation(libs.markwon)
    implementation(libs.aboutLibraries.core)
    implementation(libs.aboutLibraries.compose)

    // Conductor
    implementation(libs.bundles.conductor)

    // FlowBinding
    implementation(libs.bundles.flowbinding)

    // Logging
    implementation(libs.logcat)

    // Crash reports/analytics
    implementation(libs.acra.http)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    // Shizuku
    implementation(libs.bundles.shizuku)

    // Tests
    testImplementation(libs.junit)

    // For detecting memory leaks; see https://square.github.io/leakcanary/
    // debugImplementation(libs.leakcanary.android)

    implementation(libs.leakcanary.plumber)

    // FFmpeg
    implementation(libs.ffmpeg.kit)
    implementation(libs.arthenica.smartexceptions)

    // mpv-android
    implementation(libs.aniyomi.mpv)
}

tasks {
    withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    // See https://kotlinlang.org/docs/reference/experimental.html#experimental-status-of-experimental-api(-markers)
    withType<KotlinCompile> {
        kotlinOptions.freeCompilerArgs += listOf(
            "-opt-in=kotlin.Experimental",
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlin.ExperimentalStdlibApi",
            "-opt-in=kotlinx.coroutines.FlowPreview",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlinx.coroutines.InternalCoroutinesApi",
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
            "-opt-in=coil.annotation.ExperimentalCoilApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-opt-in=androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi",
            "-opt-in=androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi",
        )
    }

    // Duplicating Hebrew string assets due to some locale code issues on different devices
    val copyHebrewStrings = task("copyHebrewStrings", type = Copy::class) {
        from("./src/main/res/values-he")
        into("./src/main/res/values-iw")
        include("**/*")
    }

    preBuild {
        dependsOn(formatKotlin, copyHebrewStrings)
    }
}

buildscript {
    dependencies {
        classpath(kotlinx.gradle)
    }
}
