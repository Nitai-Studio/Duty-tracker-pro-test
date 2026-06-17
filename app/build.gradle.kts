plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

android {
  namespace = "com.example"
  compileSdk = 36
  ndkVersion = "27.0.12077973"

  defaultConfig {
    applicationId = "io.github.nitaistudio.twa"
    minSdk = 24
    targetSdk = 36
    versionCode = 112
    versionName = "112"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
    debug {
    }
  }
  
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  
  buildFeatures {
    compose = true
    buildConfig = true
  }
  
  packaging {
    jniLibs {
      excludes.add("**/libstartapp.so")
      excludes.add("**/libcore_game_sdk_p.so")
    }
  }
  
  testOptions { 
    unitTests { 
      isIncludeAndroidResources = true 
    } 
  }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  constraints {
    implementation("androidx.graphics:graphics-path:1.0.1") {
      because("Support 16 KB memory page sizes on Android 15/16")
    }
    implementation("androidx.datastore:datastore-core:1.1.1") {
      because("Support 16 KB memory page sizes on Android 15/16")
    }
    implementation("androidx.datastore:datastore-preferences:1.1.1") {
      because("Support 16 KB memory page sizes on Android 15/16")
    }
  }

  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  // implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  // implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.coil.compose)
  implementation("com.google.firebase:firebase-database")
  implementation("com.android.billingclient:billing-ktx:6.2.1")
  implementation("com.startapp:inapp-sdk:5.2.6")
  implementation(libs.converter.moshi)
  // implementation(libs.firebase.ai)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  
  // Force update androidx.fragment to latest version to fix outdated SDK warning
  implementation("androidx.fragment:fragment:1.8.6")
  implementation("androidx.fragment:fragment-ktx:1.8.6")
  
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  
  ksp(libs.androidx.room.compiler)
  ksp(libs.moshi.kotlin.codegen)
}

val copyApkToOutputs = tasks.register<Copy>("copyApkToOutputs") {
    from(layout.buildDirectory.dir("outputs/apk/debug"))
    include("app-debug.apk")
    into(rootProject.layout.projectDirectory.dir(".build-outputs"))
}

val copyApkToOutputsNoDot = tasks.register<Copy>("copyApkToOutputsNoDot") {
    from(layout.buildDirectory.dir("outputs/apk/debug"))
    include("app-debug.apk")
    into(rootProject.layout.projectDirectory.dir("build-outputs"))
}

val copyAabToOutputs = tasks.register<Copy>("copyAabToOutputs") {
    from(layout.buildDirectory.dir("outputs/bundle/debug"))
    include("app-debug.aab")
    into(rootProject.layout.projectDirectory.dir(".build-outputs"))
}

val copyAabToOutputsNoDot = tasks.register<Copy>("copyAabToOutputsNoDot") {
    from(layout.buildDirectory.dir("outputs/bundle/debug"))
    include("app-debug.aab")
    into(rootProject.layout.projectDirectory.dir("build-outputs"))
}

val copyReleaseApkToOutputs = tasks.register<Copy>("copyReleaseApkToOutputs") {
    from(layout.buildDirectory.dir("outputs/apk/release"))
    include("app-release.apk")
    into(rootProject.layout.projectDirectory.dir(".build-outputs"))
}

val copyReleaseApkToOutputsNoDot = tasks.register<Copy>("copyReleaseApkToOutputsNoDot") {
    from(layout.buildDirectory.dir("outputs/apk/release"))
    include("app-release.apk")
    into(rootProject.layout.projectDirectory.dir("build-outputs"))
}

val copyReleaseAabToOutputs = tasks.register<Copy>("copyReleaseAabToOutputs") {
    from(layout.buildDirectory.dir("outputs/bundle/release"))
    include("app-release.aab")
    into(rootProject.layout.projectDirectory.dir(".build-outputs"))
}

val copyReleaseAabToOutputsNoDot = tasks.register<Copy>("copyReleaseAabToOutputsNoDot") {
    from(layout.buildDirectory.dir("outputs/bundle/release"))
    include("app-release.aab")
    into(rootProject.layout.projectDirectory.dir("build-outputs"))
}

afterEvaluate {
    val assembleDebugTask = tasks.findByName("assembleDebug")
    val bundleDebugTask = tasks.findByName("bundleDebug")
    val assembleReleaseTask = tasks.findByName("assembleRelease")
    val bundleReleaseTask = tasks.findByName("bundleRelease")

    assembleDebugTask?.finalizedBy(copyApkToOutputs, copyApkToOutputsNoDot)
    bundleDebugTask?.finalizedBy(copyAabToOutputs, copyAabToOutputsNoDot)
    assembleReleaseTask?.finalizedBy(copyReleaseApkToOutputs, copyReleaseApkToOutputsNoDot)
    bundleReleaseTask?.finalizedBy(copyReleaseAabToOutputs, copyReleaseAabToOutputsNoDot)
}

tasks.register("printOutputFiles") {
    doLast {
        val rootDir = rootProject.layout.projectDirectory.asFile
        val buildOutputsDir = File(rootDir, "build-outputs")
        val dotBuildOutputsDir = File(rootDir, ".build-outputs")
        
        println("=== build-outputs directory ===")
        if (buildOutputsDir.exists()) {
            buildOutputsDir.listFiles()?.forEach { file ->
                println("${file.name}: ${file.length()} bytes")
            }
        } else {
            println("Does not exist")
        }
        
        println("=== .build-outputs directory ===")
        if (dotBuildOutputsDir.exists()) {
            dotBuildOutputsDir.listFiles()?.forEach { file ->
                println("${file.name}: ${file.length()} bytes")
            }
        } else {
            println("Does not exist")
        }

        println("=== packaged .so files in build directory ===")
        val bDir = layout.buildDirectory.asFile.get()
        if (bDir.exists()) {
            bDir.walkTopDown().forEach { file ->
                if (file.name.endsWith(".so") && !file.absolutePath.contains("incremental") && !file.absolutePath.contains("tmp")) {
                    println("${file.relativeTo(bDir)}: ${file.length()} bytes")
                }
            }
        } else {
            println("Build directory does not exist")
        }
    }
}
