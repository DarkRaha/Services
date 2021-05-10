plugins {
    java
    kotlin("jvm")
}

group = "com.github.darkraha"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    api(project(":service-core"))
}
