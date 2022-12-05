// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
//
// You may obtain a copy of the License at
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
import com.google.firebase.appdistribution.gradle.AppDistributionExtension

plugins {
  id("com.android.application")
  id("kotlin-android")
  id("com.google.gms.google-services")
  id("com.google.firebase.appdistribution")
}

android {
  compileSdk = 30

  defaultConfig {
    applicationId = "com.googletest.firebase.appdistribution.testapp"
    minSdk = 19
    targetSdk = 30
    versionName = "1.0"
    versionCode = 1

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    // This is so we can build the "release" variant for the "dev-app" (and the SDK) without
    // needing to have the app signed.
    getByName("release") { initWith(getByName("debug")) }
  }
  flavorDimensions("environment")
  productFlavors {
    create("prod") {
      dimension = "environment"
      versionNameSuffix = "-prod"
    }
    create("beta") {
      dimension = "environment"
      versionNameSuffix = "-beta"

      configure<AppDistributionExtension> {
        releaseNotes = "Beta variant of Android SDK test app"
        // testers "your email here"
      }
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  kotlinOptions { jvmTarget = "1.8" }
}

dependencies {
  // TODO(rachelprince): Add flag to build with public version of SDK
  println("Building with HEAD version \":firebase-appdistribution\" of SDK")

  // Debug variant uses full SDK
  // TODO(lkellogg): why doesn"t this get picked up transitively?
  debugImplementation(project(":firebase-appdistribution-api"))
  debugImplementation(project(":firebase-appdistribution"))

  // Release variant just uses the API, as if publishing to Play
  releaseImplementation(project(":firebase-appdistribution-api"))

  implementation("com.google.android.gms:play-services-tasks:18.0.1")
  implementation(libs.kotlin.stdlib)
  implementation("androidx.core:core-ktx:1.5.0")
  implementation("androidx.core:core:1.6.0")
  implementation("androidx.appcompat:appcompat:1.3.0")
  implementation("com.google.android.material:material:1.2.1")
  implementation("androidx.constraintlayout:constraintlayout:2.0.4")
  testImplementation("junit:junit:4.+")
}

// This allows the app to connect to Firebase on the CI.
extra["packageName"] = "com.googletest.firebase.appdistribution.testapp"

apply(from = "../../gradle/googleServices.gradle")
