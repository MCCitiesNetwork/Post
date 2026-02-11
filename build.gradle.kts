plugins {
    java
    id("xyz.jpenilla.run-paper") version "2.3.0"
    id("com.gradleup.shadow") version "9.3.1"
}

group = "io.github.md5sha256"
version = "1.0.1-SNAPSHOT"

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
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
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
    compileOnly("org.mariadb.jdbc:mariadb-java-client:3.4.0")
    compileOnly("com.zaxxer:HikariCP:5.1.0")
    // Shaded libs
    implementation("de.themoep:inventorygui:1.6.1-SNAPSHOT")
    implementation("org.spongepowered:configurate-yaml:4.1.2")
    implementation("org.spongepowered:configurate-gson:4.1.2")
    implementation("org.incendo:cloud-paper:2.0.0-beta.10") {
        exclude("com.google.guava")
    }
    implementation("org.incendo:cloud-processors-confirmation:1.0.0-rc.1") {
        exclude("com.google.guava")
    }
    implementation("org.incendo:cloud-annotations:2.0.0") {
        exclude("com.google.guava")
    }
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
}

val targetJavaVersion = 21

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
        minecraftVersion("1.21.8")
        downloadPlugins {
            github("EssentialsX", "essentials", "2.21.2", "EssentialsX-2.21.2.jar")
            github("MilkBowl", "Vault", "1.7.3", "Vault.jar")
        }
    }
}
