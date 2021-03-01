dependencies {
    api(project(":service-core"))
    api(project(":service-http"))
    api("org.jsoup:jsoup:1.13.1")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.9.0")
}
