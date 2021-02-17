plugins {
    java
    maven
    kotlin("jvm")
}

//group = "services"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":service-core"))
    implementation(project(":service-http"))
    implementation(kotlin("stdlib-jdk8"))
    implementation ("org.jsoup:jsoup:1.13.1")

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