plugins {
    java
    id("xyz.jpenilla.run-paper") version "2.3.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
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
    compileOnly("com.arcaniax:HeadDatabase-API:1.3.2")
    // Provided by spigot library
    compileOnly("org.mariadb.jdbc:mariadb-java-client:3.4.0")
    compileOnly("com.zaxxer:HikariCP:5.1.0")
    // Shaded libs
    implementation("de.themoep:inventorygui:1.6.1-SNAPSHOT")
    implementation("org.spongepowered:configurate-yaml:4.1.2")
    implementation("org.spongepowered:configurate-gson:4.1.2")
    implementation("org.incendo:cloud-paper:2.0.0-beta.5") {
        exclude("com.google.guava")
    }
    implementation("org.incendo:cloud-processors-confirmation:1.0.0-beta.2") {
        exclude("com.google.guava")
    }
    implementation("org.incendo:cloud-annotations:2.0.0-beta.2") {
        exclude("com.google.guava")
    }
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
    shadowJar {
        val path = "io.github.md5sha256.democracypost.libraries"
        relocate("org.incendo.cloud", "${path}.cloud")
        relocate("io.leangen.geantyref", "${path}.geantyref")
        relocate("org.spongepowered", "${path}.spongepowered")
        relocate("org.yaml.snakeyaml", "${path}.snakeyaml")
        relocate("com.google.gson", "${path}.gson")
        relocate("de.themoep.inventorygui", "${path}.inventorygui")
    }
    runServer {
        minecraftVersion("1.20.4")
    }
}
