plugins {
    id("io.freefair.lombok") version "8.13"
    `maven-publish`
    `java-library`
}

version = "1.21.0"
description = "MCProtocolLib is a simple library for communicating with Minecraft clients and servers."
val javaVersion = JavaLanguageVersion.of(21)

repositories {
    maven("https://repo.opencollab.dev/maven-releases/") {
        name = "opencollab-releases"
        content { includeGroupByRegex("org.cloudburstmc.*") }
    }
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc"
        content { includeGroup("com.velocitypowered") }
    }
    maven("https://maven.2b2t.vc/releases") {
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


dependencies {
    api("org.slf4j:slf4j-api:2.0.17")

    api("com.github.rfresh2:OpenNBT:3.0.8")

    api("net.raphimc:MinecraftAuth:4.1.1")

    val adventureVersion = "4.19.0"
    api("net.kyori:adventure-text-serializer-gson:$adventureVersion")
    api("net.kyori:adventure-text-serializer-json-legacy-impl:$adventureVersion")
    api("net.kyori:adventure-text-serializer-legacy:$adventureVersion")
    api("net.kyori:adventure-text-serializer-ansi:$adventureVersion")
    api("net.kyori:adventure-text-minimessage:$adventureVersion")

    api("org.cloudburstmc.math:api:2.0")
    api("org.cloudburstmc.math:immutable:2.0")

    val fastutilVersion = "8.5.15"
    api("com.github.rfresh2.fastutil.maps:object-int-maps:$fastutilVersion")
    api("com.github.rfresh2.fastutil.maps:int-object-maps:$fastutilVersion")
    api("com.github.rfresh2.fastutil.maps:int-int-maps:$fastutilVersion")
    api("com.github.rfresh2.fastutil.maps:reference-int-maps:$fastutilVersion")

    api("io.netty:netty-all:4.1.119.Final")
    compileOnly("io.netty.incubator:netty-incubator-transport-native-io_uring:0.0.26.Final")

    api("com.velocitypowered:velocity-native:3.4.0-SNAPSHOT")

    api("org.checkerframework:checker-qual:3.49.1")

    testImplementation(platform("org.junit:junit-bom:5.12.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

lombok {
    version = "1.18.36"
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
    toolchain { languageVersion = javaVersion }
}

publishing {
    repositories {
        maven {
            name = "vc"
            url = uri("https://maven.2b2t.vc/releases")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.rfresh2"
            artifactId = "MCProtocolLib"
            version = project.version.toString()
            System.getenv("PUBLISH_VERSION")?.let {
                version = it
            }
            from(components["java"])
        }
    }
}
