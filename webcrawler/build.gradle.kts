import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}
dependencies {
    api(project(":service-core"))
    api(project(":service-http"))
    api("org.jsoup:jsoup:1.13.1")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.9.0")
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