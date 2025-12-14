val IS_CI = System.getenv("CI") == "true"

plugins {
    id("dev.kikugie.stonecutter")
    id("dev.architectury.loom") version "1.13-SNAPSHOT" apply false
    id("architectury-plugin") version "3.4-SNAPSHOT" apply false
}

if (IS_CI) stonecutter active null
else stonecutter active file("versions/current") /* [SC] DO NOT EDIT */

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.fabricmc.net/")

        // YACL
        maven("https://maven.isxander.dev/releases/")

        // Mod Menu
        maven("https://maven.terraformersmc.com/")
        maven("https://maven.nucleoid.xyz/") // Text Placeholder API

        exclusiveContent {
            forRepository {
                maven("https://repo.spongepowered.org/repository/maven-public") { name = "Sponge" }
            }
            filter { includeGroupAndSubgroups("org.spongepowered") }
        }
        exclusiveContent {
            forRepositories(
                maven("https://maven.parchmentmc.org") { name = "ParchmentMC" },
                maven("https://maven.neoforged.net/releases") { name = "NeoForge" },
            )
            filter { includeGroup("org.parchmentmc.data") }
        }
        maven("https://www.cursemaven.com")
        maven("https://api.modrinth.com/maven") {
            name = "Modrinth"
            content {
                includeGroup("maven.modrinth")
            }
        }
        maven("https://maven.terraformersmc.com/releases/") { name = "TerraformersMC" }
        maven("https://maven.isxander.dev/releases")
        maven("https://maven.isxander.dev/snapshots")
        maven("https://maven.quiltmc.org/repository/release")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://maven.architectury.dev/")
        maven("https://maven.theillusivec4.top/")
        maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")

        maven("https://maven.jamieswhiteshirt.com/libs-release") {
            content {
                includeGroup("com.jamieswhitefshirt")
            }
        }

        maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")

    }
}
