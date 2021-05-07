tasks {

    jar {
        val modules = setOf("service-core", "service-http", "webcrawler", "service-diskcache")

//        rootProject.subprojects.filter { it.name!="all" }.forEach {
//            dependsOn(":${it.name}:jar")
//            println("module: ${it.name}")
//        }

        rootProject.subprojects.filter { it.name in modules }.forEach {
            dependsOn(":${it.name}:jar")
            println("module: ${it.name}")
        }

        rootProject.subprojects.filter { it.name != "all" }.forEach { project ->
            from({
                project.configurations.archives.allArtifacts.files.map {
                    zipTree(it)
                }
            })
        }
    }
}