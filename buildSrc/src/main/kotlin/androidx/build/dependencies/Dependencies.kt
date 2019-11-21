/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.build.dependencies

const val ANDROIDX_TEST_CORE = "androidx.test:core:1.2.0"
const val ANDROIDX_TEST_EXT_JUNIT = "androidx.test.ext:junit:1.1.1"
const val ANDROIDX_TEST_EXT_KTX = "androidx.test.ext:junit-ktx:1.1.1"
const val ANDROIDX_TEST_MONITOR = "androidx.test:monitor:1.2.0"
const val ANDROIDX_TEST_RULES = "androidx.test:rules:1.2.0"
const val ANDROIDX_TEST_RUNNER = "androidx.test:runner:1.2.0"
const val ANDROIDX_TEST_UIAUTOMATOR = "androidx.test.uiautomator:uiautomator:2.2.0"
const val AUTO_COMMON = "com.google.auto:auto-common:0.10"
const val AUTO_VALUE = "com.google.auto.value:auto-value:1.6.3"
const val AUTO_VALUE_ANNOTATIONS = "com.google.auto.value:auto-value-annotations:1.6.3"
const val AUTO_VALUE_PARCEL = "com.ryanharter.auto.value:auto-value-parcel:0.2.6"
const val ANTLR = "org.antlr:antlr4:4.7.1"
const val APACHE_COMMONS_CODEC = "commons-codec:commons-codec:1.10"
const val CHECKER_FRAMEWORK = "org.checkerframework:checker-qual:2.5.3"
const val CONSTRAINT_LAYOUT = "androidx.constraintlayout:constraintlayout:1.1.0@aar"
const val DEXMAKER_MOCKITO = "com.linkedin.dexmaker:dexmaker-mockito:2.25.0"
const val ESPRESSO_CONTRIB = "androidx.test.espresso:espresso-contrib:3.1.0"
const val ESPRESSO_CORE = "androidx.test.espresso:espresso-core:3.1.0"
const val ESPRESSO_IDLING_RESOURCE = "androidx.test.espresso:espresso-idling-resource:3.1.0"
const val ESPRESSO_WEB = "androidx.test.espresso:espresso-web:3.1.0"
const val FINDBUGS = "com.google.code.findbugs:jsr305:3.0.2"
const val GCM_NETWORK_MANAGER = "com.google.android.gms:play-services-gcm:17.0.0"
const val GOOGLE_COMPILE_TESTING = "com.google.testing.compile:compile-testing:0.18"
const val GSON = "com.google.code.gson:gson:2.8.0"
const val GUAVA_VERSION = "27.0.1-jre"
const val GUAVA = "com.google.guava:guava:$GUAVA_VERSION"
const val GUAVA_ANDROID_VERSION = "27.0.1-android"
const val GUAVA_ANDROID = "com.google.guava:guava:$GUAVA_ANDROID_VERSION"
const val GUAVA_LISTENABLE_FUTURE = "com.google.guava:listenablefuture:1.0"
const val GRADLE_INCAP_HELPER = "net.ltgt.gradle.incap:incap:0.2"
const val GRADLE_INCAP_HELPER_PROCESSOR = "net.ltgt.gradle.incap:incap-processor:0.2"
const val INTELLIJ_ANNOTATIONS = "com.intellij:annotations:12.0"
const val JAVAPOET = "com.squareup:javapoet:1.8.0"
const val JSR250 = "javax.annotation:javax.annotation-api:1.2"
const val JUNIT = "junit:junit:4.12"
const val KOTLINPOET = "com.squareup:kotlinpoet:1.4.0"
const val KOTLIN_GRADLE_PLUGIN = "org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.20"

const val KOTLIN_METADATA = "me.eugeniomarletti.kotlin.metadata:kotlin-metadata:1.4.0"
const val KOTLIN_METADATA_JVM = "org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.0.5"

private const val KOTLIN_COROUTINES_VERSION = "1.3.0"
const val KOTLIN_COROUTINES_ANDROID =
    "org.jetbrains.kotlinx:kotlinx-coroutines-android:$KOTLIN_COROUTINES_VERSION"
const val KOTLIN_COROUTINES_CORE =
    "org.jetbrains.kotlinx:kotlinx-coroutines-core:$KOTLIN_COROUTINES_VERSION"
const val KOTLIN_COROUTINES_GUAVA =
    "org.jetbrains.kotlinx:kotlinx-coroutines-guava:$KOTLIN_COROUTINES_VERSION"
const val KOTLIN_COROUTINES_TEST =
    "org.jetbrains.kotlinx:kotlinx-coroutines-test:$KOTLIN_COROUTINES_VERSION"
const val KOTLIN_COROUTINES_RX2 =
    "org.jetbrains.kotlinx:kotlinx-coroutines-rx2:$KOTLIN_COROUTINES_VERSION"

const val LEAKCANARY_INSTRUMENTATION =
    "com.squareup.leakcanary:leakcanary-android-instrumentation:1.6.2"
const val MATERIAL = "com.google.android.material:material:1.0.0"
const val MOCKITO_CORE = "org.mockito:mockito-core:2.25.0"
const val MOCKITO_KOTLIN = "com.nhaarman.mockitokotlin2:mockito-kotlin:2.1.0"
const val MULTIDEX = "androidx.multidex:multidex:2.0.0"
const val NULLAWAY = "com.uber.nullaway:nullaway:0.3.7"
const val OKHTTP_MOCKWEBSERVER = "com.squareup.okhttp3:mockwebserver:3.11.0"
const val REACTIVE_STREAMS = "org.reactivestreams:reactive-streams:1.0.0"
const val RX_JAVA = "io.reactivex.rxjava2:rxjava:2.2.9"
const val TRUTH = "com.google.truth:truth:1.0"
const val XERIAL = "org.xerial:sqlite-jdbc:3.25.2"
const val XPP3 = "xpp3:xpp3:1.1.4c"
const val XMLPULL = "xmlpull:xmlpull:1.1.3.1"

const val ROBOLECTRIC = "org.robolectric:robolectric:4.1"

const val PROTOBUF = "com.google.protobuf:protobuf-java:3.4.0"

const val FLOGGER = "com.google.flogger:flogger:0.4"
const val FLOGGER_SYSTEM_BACKEND = "com.google.flogger:flogger-system-backend:0.4"

// The following versions change depending on whether we are in the main or ui project - the
// specific versions are configured in build_dependencies.gradle as they are needed during
// buildSrc configuration. They are then set here in AndroidXPlugin when configuring the root
// project.
internal lateinit var kotlinVersion: String

val KOTLIN_VERSION get() = kotlinVersion
val KOTLIN_STDLIB get() = "org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion"
val KOTLIN_STDLIB_COMMON get() = "org.jetbrains.kotlin:kotlin-stdlib-common:$kotlinVersion"
val KOTLIN_STDLIB_JDK8 get() = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion"
val KOTLIN_STDLIB_JS get() = "org.jetbrains.kotlin:kotlin-stdlib-js:$kotlinVersion"
val KOTLIN_TEST get() = "org.jetbrains.kotlin:kotlin-test:$kotlinVersion"
val KOTLIN_TEST_COMMON get() = "org.jetbrains.kotlin:kotlin-test-common:$kotlinVersion"
val KOTLIN_TEST_ANNOTATIONS_COMMON get() =
    "org.jetbrains.kotlin:kotlin-test-annotations-common:$kotlinVersion"
val KOTLIN_TEST_JUNIT get() = "org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion"
val KOTLIN_TEST_JS get() = "org.jetbrains.kotlin:kotlin-test-js:$kotlinVersion"
val KOTLIN_REFLECT get() = "org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion"

internal lateinit var agpVersion: String

const val AGP_STABLE = "com.android.tools.build:gradle:3.5.2"
val AGP_LATEST get() = "com.android.tools.build:gradle:$agpVersion"

internal lateinit var lintVersion: String

const val LINT_API_MIN = "com.android.tools.lint:lint-api:26.3.0"
val LINT_API_LATEST get() = "com.android.tools.lint:lint-api:$lintVersion"
val LINT_CORE get() = "com.android.tools.lint:lint:$lintVersion"
val LINT_TESTS get() = "com.android.tools.lint:lint-tests:$lintVersion"
