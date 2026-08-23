plugins {
    java
    id("xyz.jpenilla.run-paper") version "3.1.0"
    id("com.gradleup.shadow") version "9.6.1"
}

group = "io.github.md5sha256"
version = "1.2.0-SNAPSHOT"

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
        name = "mccities"
        url = uri("https://maven.minecraftcitiesnetwork.com/releases")
    }
    maven {
        name = "jitpack"
        url = uri("https://jitpack.io")
    }
    maven {
        name = "minebench-repo"
        url = uri("https://repo.minebench.de/")
        content {
            includeModule("de.themoep", "inventorygui")
        }
    }
    maven {
        name = "essentialsx"
        url = uri("https://repo.essentialsx.net/releases/")
        mavenContent {
            releasesOnly()
        }

    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    compileOnly("com.arcaniax:HeadDatabase-API:1.3.2")
    compileOnly("io.github.md5sha256:player-notifications-api:1.0.1")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1") {
        exclude(group = "org.bukkit", module = "bukkit")
        exclude(group = "org.spigotmc", module = "spigot-api")
        exclude(group = "io.papermc.paper", module = "paper-api")
    }
    compileOnly("net.essentialsx:EssentialsX:2.21.2") {
        exclude(group = "org.bukkit", module = "bukkit")
        exclude(group = "org.spigotmc", module = "spigot-api")
        exclude(group = "io.papermc.paper", module = "paper-api")
    }
    // Provided by spigot library
    compileOnly("org.mariadb.jdbc:mariadb-java-client:3.5.10")
    compileOnly("com.zaxxer:HikariCP:7.1.0")
    // Shaded libs
    implementation("de.themoep:inventorygui:1.6.6")
    implementation("org.spongepowered:configurate-yaml:4.2.0")
    implementation("org.spongepowered:configurate-gson:4.2.0")
    implementation("org.incendo:cloud-paper:2.0.0") {
        exclude("com.google.guava")
    }
    implementation("org.incendo:cloud-processors-confirmation:1.0.0-rc.1") {
        exclude("com.google.guava")
    }
    implementation("org.incendo:cloud-annotations:2.1.0") {
        exclude("com.google.guava")
    }
    testImplementation("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    testImplementation("io.github.md5sha256:player-notifications-api:1.0.1")
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Compiled against the Paper 1.21.8 API, but released as Java 25 bytecode: the server must run on
// a Java 25 JVM, as player-notifications-api already requires.
val targetJavaVersion = 25

java.toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))

tasks {

    withType(JavaCompile::class).configureEach {
        options.encoding = "UTF-8"
        options.release.set(targetJavaVersion)
    }

    test {
        useJUnitPlatform()
    }

    processResources {
        filesMatching("paper-plugin.yml") {
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
        minecraftVersion("1.21.8")
        downloadPlugins {
            github("EssentialsX", "essentials", "2.21.2", "EssentialsX-2.21.2.jar")
            github("MilkBowl", "Vault", "1.7.3", "Vault.jar")
        }
    }
}
