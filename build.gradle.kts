plugins {
    java
    maven
    kotlin("jvm") version "1.4.10"
}

//group = "service"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}



// val kotl

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.0")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}