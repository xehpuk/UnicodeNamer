plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
}

application {
    mainClass.set("de.xehpuk.unicodenamer.UnicodeNamer")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.register<Jar>("runnableJar") {
    dependsOn.addAll(listOf("compileJava", "processResources"))
    archiveClassifier.set("standalone")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest { attributes(mapOf("Main-Class" to application.mainClass)) }
    val sourcesMain = sourceSets.main.get()
    val contents = configurations.runtimeClasspath.get()
        .map { if (it.isDirectory) it else zipTree(it) } +
            sourcesMain.output
    from(contents)
}

tasks.named("build") {
    dependsOn.add(tasks.named("runnableJar"))
}