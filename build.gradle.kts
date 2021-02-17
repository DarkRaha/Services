plugins {
    kotlin("jvm") version "1.4.10"
}


allprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven")
    apply(plugin = "kotlin")
    version = "1.0"

    repositories {
        mavenCentral()
    }

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8
    }

    dependencies {
        implementation(kotlin("stdlib-jdk8"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
        testImplementation("org.junit.jupiter:junit-jupiter:5.7.0")
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
}

subprojects {
    group = "com.github.darkraha.services"
}

group = "com.github.darkraha"



dependencies {
    api(project(":service-core"))
    api(project(":service-http"))
    api(project(":webcrawler"))
}
tasks {

    jar {
        subprojects.forEach {
            dependsOn(":${it.name}:jar")
        }
        subprojects.forEach { subproject ->
            from({
                subproject.configurations.archives.allArtifacts.files.map {
                    zipTree(it)
                }
            })
        }
    }
}


tasks.register<Jar>("fatJar") {
    archiveClassifier.set("fat")

    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}

