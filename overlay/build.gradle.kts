plugins { java }
repositories {
    mavenLocal()
    mavenCentral()
    maven("https://libraries.minecraft.net/") {
        content { includeGroup("com.mojang") }
    }
}
val baseline = file(providers.gradleProperty("faweBaseline").get())
val canvasRuntime = file(providers.gradleProperty("canvasRuntime").get())
dependencies {
    compileOnly(files(baseline, canvasRuntime))
    compileOnly("com.mojang:datafixerupper:10.0.21")
    compileOnly("io.canvasmc.canvas:canvas-api:26.2.local-SNAPSHOT") { isTransitive = false }
    compileOnly("net.kyori:adventure-api:5.2.0")
    compileOnly("com.google.guava:guava:33.5.0-jre")
    compileOnly("org.apache.logging.log4j:log4j-api:2.26.1")
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
    implementation("org.ow2.asm:asm:9.10.1")
    implementation("org.ow2.asm:asm-tree:9.10.1")
}
java { toolchain.languageVersion.set(JavaLanguageVersion.of(25)) }
tasks.withType<JavaCompile>().configureEach { options.encoding = "UTF-8" }
tasks.register<JavaExec>("patchedJar") {
    dependsOn(tasks.classes)
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("it.coralmc.fawepatch.AssembleTrimFix")
    args(baseline.absolutePath, layout.buildDirectory.dir("classes/java/main").get().asFile.absolutePath,
        layout.buildDirectory.file("libs/FastAsyncWorldEdit-Paper-2.15.4-SNAPSHOT.jar").get().asFile.absolutePath)
}
