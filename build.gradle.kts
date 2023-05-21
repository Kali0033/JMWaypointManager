import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
}

group = "org.grimurrp"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {// paper
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven { // packetevents 2.0
        url = uri("https://repo.codemc.io/repository/maven-snapshots/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")

    testImplementation(kotlin("test"))
    testImplementation("com.github.seeseemelk:MockBukkit-v1.19:2.29.0")

    // packetevents
    compileOnly("com.github.retrooper.packetevents:spigot:2.0.0-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

bukkit {
    main = "org.grimurrp.jmwaypointmanager.JMWaypointManager"
    apiVersion = "1.19"
    author = "Kali"
    depend = listOf("packetevents")
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD
}

tasks {
    test {
        useJUnitPlatform()
    }

    build {
        dependsOn(shadowJar)
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
}