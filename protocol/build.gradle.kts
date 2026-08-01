plugins {
    id("io.freefair.lombok") version "9.5.0"
    `maven-publish`
    `java-library`
}

version = "26.2.0"
description = "MCProtocolLib is a simple library for communicating with Minecraft clients and servers."
val javaVersion = JavaLanguageVersion.of(25)

repositories {
    maven("https://maven.2b2t.vc/releases")
    maven("https://maven.2b2t.vc/remote")
    mavenLocal()
}

dependencies {
    api("org.slf4j:slf4j-api:2.0.18")

    api("com.github.rfresh2:OpenNBT:3.0.13")

    api("net.raphimc:MinecraftAuth:5.0.2")

    api(platform("net.kyori:adventure-bom:5.2.0"))
    api("net.kyori:adventure-text-serializer-gson")
    api("net.kyori:adventure-text-serializer-json-legacy-impl")
    api("net.kyori:adventure-text-serializer-legacy")
    api("net.kyori:adventure-text-serializer-ansi")
    api("net.kyori:adventure-text-minimessage")
    api("net.kyori:adventure-text-serializer-commons")

    api("org.cloudburstmc.math:api:2.0")
    api("org.cloudburstmc.math:immutable:2.0")

    api(platform("com.github.rfresh2.fastutil:fastutil-bom:8.5.19"))
    api("com.github.rfresh2.fastutil.maps:object-int-maps:")
    api("com.github.rfresh2.fastutil.maps:int-object-maps")
    api("com.github.rfresh2.fastutil.maps:int-int-maps")
    api("com.github.rfresh2.fastutil.maps:reference-int-maps")

    api("io.netty:netty-all:4.2.16.Final")

    api("com.velocitypowered:velocity-native:4.1.0-SNAPSHOT")

    api("org.checkerframework:checker-qual:4.2.1")

    testImplementation(platform("org.junit:junit-bom:6.1.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

lombok {
    version = "1.18.42"
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
