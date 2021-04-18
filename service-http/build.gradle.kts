import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}
dependencies {
    api(project(":service-core"))
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    implementation ("com.google.code.gson:gson:2.8.6")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.9.0")
   // testImplementation ("com.github.gmazzo:okhttp-mock:1.4.0")
    implementation(kotlin("stdlib-jdk8"))
}
repositories {
    mavenCentral()
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}