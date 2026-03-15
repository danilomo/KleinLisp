plugins {
    java
}

group = "net.sourceforge.kleinlisp"
version = "0.0.1"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
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
    implementation("org.pcollections:pcollections:4.0.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.1")
    testImplementation("org.hamcrest:hamcrest-core:1.3")
}

tasks.test {
    useJUnitPlatform()
}
