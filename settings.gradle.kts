enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositories {
        maven("https://repo.opencollab.dev/maven-releases/") {
            name = "opencollab-releases"
        }
        maven("https://repo.opencollab.dev/maven-snapshots/") {
            name = "opencollab-snapshots"
        }
        // fixme: remove when adventure 4.15.0 releases
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") {
            name = "sonatype-snapshots"
        }
        maven("https://papermc.io/repo/repository/maven-public/") {
            name = "papermc"
        }
        maven("https://jitpack.io") {
            name = "jitpack"
        }
        maven("https://litarvan.github.io/maven") {
            name = "litarvan"
        }
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "mcprotocollib"

include("protocol", "example")
