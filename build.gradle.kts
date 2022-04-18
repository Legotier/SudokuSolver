version = "1.0"

plugins {
    id("java")
    id("application")
    id("org.openjfx.javafxplugin") version "0.0.9"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains:annotations:23.0.0")
}

tasks.jar {
    manifest {
        attributes("Main-Class" to "main.MainWrapper")
    }
    val dependencies = configurations
        .runtimeClasspath
        .get()
        .map(::zipTree) // OR .map { zipTree(it) }
    from(dependencies)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

sourceSets {
    main {
        java {
            srcDir("src")
        }
        resources {
            srcDirs("src/gui/fxml", "src/main")
        }
    }
}

javafx {
    version = "16"
    modules = listOf("javafx.controls", "javafx.fxml")
}

application {
    mainClass.set("main.MainWrapper")
}

