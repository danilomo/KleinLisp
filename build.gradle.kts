plugins {
    `java-library`
    application
    `maven-publish`
    id("com.diffplug.spotless") version "6.25.0"
}

application {
    mainClass.set("net.sourceforge.kleinlisp.Main")
}

group = "com.github.danilomo"
version = "0.0.3"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

val parserDir = "src/main/java/net/sourceforge/kleinlisp/parser"
val binDir = "bin"

val generateLexer by tasks.registering(JavaExec::class) {
    description = "Generate lexer from JFlex specification"
    group = "generation"

    inputs.file("$parserDir/lexical.flex")
    outputs.file("$parserDir/LexicalAnalyzer.java")

    classpath = files("$binDir/JFlex.jar")
    mainClass.set("JFlex.Main")
    args = listOf("-d", parserDir, "$parserDir/lexical.flex")
}

val generateParser by tasks.registering(JavaExec::class) {
    description = "Generate parser from CUP specification"
    group = "generation"

    inputs.file("$parserDir/parser.cup")
    outputs.files("$parserDir/parser.java", "$parserDir/sym.java")

    classpath = files("$binDir/java-cup-11a.jar")
    mainClass.set("java_cup.Main")
    args = listOf("-destdir", parserDir, "-expect", "1", "$parserDir/parser.cup")
}

sourceSets {
    main {
        java {
            srcDirs("src/main/java")
        }
        resources {
            srcDirs("src/main/resources")
        }
    }
    test {
        java {
            srcDirs("src/test/java")
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    api("org.pcollections:pcollections:4.0.2")
    api("org.json:json:20231013")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.1")
    testImplementation("org.junit.platform:junit-platform-suite-api:1.9.1")
    testRuntimeOnly("org.junit.platform:junit-platform-suite-engine:1.9.1")
    testImplementation("org.hamcrest:hamcrest-core:1.3")
}

tasks.test {
    useJUnitPlatform()
}

// Normal JAR for library usage (no bundled dependencies)
tasks.jar {
    archiveClassifier.set("")
    manifest {
        attributes(
            "Main-Class" to "net.sourceforge.kleinlisp.Main"
        )
    }
}

// Fat JAR for standalone application usage (with bundled dependencies)
tasks.register<Jar>("fatJar") {
    archiveClassifier.set("all")
    manifest {
        attributes(
            "Main-Class" to "net.sourceforge.kleinlisp.Main"
        )
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

spotless {
    java {
        targetExclude(
            "src/main/java/net/sourceforge/kleinlisp/parser/LexicalAnalyzer.java",
            "src/main/java/net/sourceforge/kleinlisp/parser/parser.java",
            "src/main/java/net/sourceforge/kleinlisp/parser/sym.java",
            "src/main/java/net/sourceforge/kleinlisp/java_cup/**"
        )
        googleJavaFormat().reflowLongStrings()
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
