plugins {
    id("io.freefair.lombok") version "8.6"
    `maven-publish`
    `java-library`
}

version = "1.21.0"
description = "MCProtocolLib is a simple library for communicating with Minecraft clients and servers."

repositories {
    maven("https://repo.opencollab.dev/maven-releases/") {
        name = "opencollab-releases"
        content { includeGroupByRegex("org.cloudburstmc.*") }
    }
    maven("https://papermc.io/repo/repository/maven-public/") {
        name = "papermc"
        content { includeGroup("com.velocitypowered") }
    }
    maven("https://jitpack.io") {
        name = "jitpack"
        content { includeGroupByRegex("com.github.rfresh2.*") }
    }
    maven("https://maven.lenni0451.net/releases") {
        name = "Lenni0451"
        content {
            includeGroup("net.raphimc")
            includeGroup("net.lenni0451")
        }
    }
    mavenCentral()
    mavenLocal()
}

val adventureVersion = "4.17.0"
val fastutilVersion = "edaf36bfd3"

dependencies {
    api("org.slf4j:slf4j-api:2.0.13")

    api("com.github.rfresh2:OpenNBT:d3e1a5e287")

    // MinecraftAuth for authentication
    api("net.raphimc:MinecraftAuth:4.0.2")

    api("net.kyori:adventure-text-serializer-gson:$adventureVersion")
    api("net.kyori:adventure-text-serializer-json-legacy-impl:$adventureVersion")
    api("net.kyori:adventure-text-serializer-legacy:$adventureVersion")
    api("net.kyori:adventure-text-serializer-ansi:$adventureVersion")

    api("org.cloudburstmc.math:api:2.0")
    api("org.cloudburstmc.math:immutable:2.0")

    api("com.github.rfresh2.fastutil:object-int-maps:$fastutilVersion")
    api("com.github.rfresh2.fastutil:int-object-maps:$fastutilVersion")
    api("com.github.rfresh2.fastutil:int-int-maps:$fastutilVersion")

    api("io.netty:netty-all:4.1.111.Final")
    api("io.netty.incubator:netty-incubator-transport-native-io_uring:0.0.25.Final")

    api("com.velocitypowered:velocity-native:4.0.0-SNAPSHOT")

    api("org.checkerframework:checker-qual:3.44.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
}

lombok {
    version = "1.18.34"
}

tasks {
    withType(JavaCompile::class.java) {
        options.encoding = "UTF-8"
        options.isDeprecation = true
        options.compilerArgs.add("-Xlint:all,-processing")
    }
    withType<Javadoc> {
        title = "MCProtocolLib Javadocs"
        val options = options as StandardJavadocDocletOptions
        options.encoding = "UTF-8"
        options.addStringOption("Xdoclint:all,-missing", "-quiet")
    }
    test {
        useJUnitPlatform()
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.rfresh2"
            artifactId = "MCProtocolLib"
            version = project.version.toString()
            from(components["java"])
        }
    }
}
