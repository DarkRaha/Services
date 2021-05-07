import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
plugins {
    java
    kotlin("jvm")
}


repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    api(project(":service-core"))
}
