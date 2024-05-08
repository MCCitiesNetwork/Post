plugins {
    java
    id("xyz.jpenilla.run-paper") version "2.3.0"
}

group = "io.github.md5sha256"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
    maven {
        name = "sonatype-snapshots"
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        mavenContent {
            snapshotsOnly()
        }
    }
    maven {
        name = "minebench-repo"
        url = uri("https://repo.minebench.de/")
        content {
            includeModule("de.themoep", "inventorygui")
        }
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    implementation("de.themoep:inventorygui:1.6.1-SNAPSHOT")
    implementation("org.spongepowered:configurate-yaml:4.1.2")
    implementation("org.spongepowered:configurate-gson:4.1.2")
}

val targetJavaVersion = 17

java.toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))

tasks {

    withType(JavaCompile::class).configureEach {
        options.encoding = "UTF-8"
        options.release.set(targetJavaVersion)
    }

    processResources {
        filesMatching("plugin.yml") {
            expand("version" to project.version)
        }
    }
    runServer {
        minecraftVersion("1.20.4")
    }
}
