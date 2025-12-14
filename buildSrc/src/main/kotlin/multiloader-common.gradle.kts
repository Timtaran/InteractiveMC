plugins {
	id("java-library")
	id("idea")
}

version = "${loader}-${commonMod.version}+mc${stonecutterBuild.current.version}"

base {
	archivesName = commonMod.id
}

java {
	toolchain.languageVersion = JavaLanguageVersion.of(commonProject.prop("java.version")!!)
	// withSourcesJar()
	// withJavadocJar()
}

tasks {
	processResources {

		val expandProps = mapOf(
			"modId" to commonMod.id,
			"modName" to commonMod.name,
			"modVersion" to commonMod.version,
			"modGroup" to commonMod.group,
			"modAuthor" to commonMod.author,
			"modDescription" to commonMod.description,
			"modLicense" to commonMod.license,
            "modGitHub" to commonMod.github,
            "modCurseForge" to commonMod.curseforgeLink,
            "modModrinth" to commonMod.modrinthLink,
            "modWiki" to commonMod.wiki,
			"minecraftVersion" to commonMod.depOrNull("minecraft"),
			"minMinecraftVersion" to commonMod.depOrNull("min_minecraft"),
			"fabricLoaderVersion" to commonMod.depOrNull("fabric-loader"),
			"fabricApiVersion" to commonMod.depOrNull("fabric-api"),
			"neoForgeVersion" to commonMod.depOrNull("neoforge"),
			"yaclVersion" to commonMod.depOrNull("yacl"),
		).filterValues { it?.isNotEmpty() == true }.mapValues { (_, v) -> v!! }

		val jsonExpandProps = expandProps.mapValues { (_, v) -> v.replace("\n", "\\\\n") }

			filesMatching(listOf("META-INF/mods.toml", "META-INF/neoforge.mods.toml")) {
				expand(expandProps)
			}

		filesMatching(listOf("pack.mcmeta", "fabric.mod.json", "*.mixins.json")) {
			expand(jsonExpandProps)
		}

		inputs.properties(expandProps)
	}
}

tasks.named("processResources") {
	dependsOn(":common:${commonMod.mc}:stonecutterGenerate")
}
