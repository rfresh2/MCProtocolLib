plugins {
    id("io.freefair.lombok") version "9.5.0"
    `java-library`
    `maven-publish`
}

version = "0.1.0"
description = "Adventure text component serializers for Cloudburst's NBT library"
val javaVersion = JavaLanguageVersion.of(25)

repositories {
    maven("https://maven.2b2t.vc/releases")
    maven("https://maven.2b2t.vc/remote")
    mavenLocal()
}

dependencies {
    api(libs.jspecify)
    api(libs.adventure.api)
    api(libs.cloudburstnbt)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

lombok {
    version = "1.18.48"
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
        options.splitIndex()
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
            artifactId = "text-serializer-nbt"
            version = project.version.toString()
            System.getenv("PUBLISH_VERSION")?.let {
                version = it
            }
            from(components["java"])
        }
    }
}

//tasks.withType<JavaCompile> {
//    options.compilerArgs.add("-Xlint:all,-processing")
//}
//
//tasks.withType<Javadoc> {
//    title = "MCProtocolLib Javadocs"
//    val options = options as StandardJavadocDocletOptions
//    options.encoding = "UTF-8"
//    options.addStringOption("Xdoclint:all,-missing", "-quiet")
//}

