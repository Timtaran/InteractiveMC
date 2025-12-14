plugins {
    id("multiloader-loader")
    id("dev.architectury.loom")
    id("architectury-plugin")
	id("dev.kikugie.fletching-table.fabric") version "0.1.0-alpha.22"
}

fletchingTable {
	j52j.register("main") {
		extension("json", "**/*.json5")
	}
}

dependencies {
	minecraft("com.mojang:minecraft:${commonMod.mc}")
	mappings(loom.layered {
		officialMojangMappings()
		commonMod.depOrNull("parchment")?.let { parchmentVersion ->
			parchment("org.parchmentmc.data:parchment-${commonMod.mc}:$parchmentVersion@zip")
		}
	})

	modImplementation("net.fabricmc:fabric-loader:${commonMod.dep("fabric_loader")}")
	modApi("net.fabricmc.fabric-api:fabric-api:${commonMod.dep("fabric_api")}+${commonMod.mc}")

	// Required dependencies
	modImplementation("dev.isxander:yet-another-config-lib:${commonMod.dep("yacl")}-fabric")

	// Optional dependencies
	// Mod Menu (https://www.curseforge.com/minecraft/mc-mods/modmenu)
	commonMod.depOrNull("mod_menu")?.let { modMenuVersion ->
		modImplementation("com.terraformersmc:modmenu:${modMenuVersion}")
	}

	commonMod.depOrNull("devauth")?.let { devauthVersion ->
		modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:${devauthVersion}")
	}
}

loom {
	accessWidenerPath = common.project.file("../../src/main/resources/accesswideners/${commonMod.mc}-${mod.id}.accesswidener")

	runs {
		getByName("client") {
			client()
			configName = "Fabric Client"
			ideConfigGenerated(true)
		}
		getByName("server") {
			server()
			configName = "Fabric Server"
			ideConfigGenerated(true)
		}
	}
}

tasks.named<ProcessResources>("processResources") {
	val awFile = project(":common").file("src/main/resources/accesswideners/${commonMod.mc}-${mod.id}.accesswidener")

	from(awFile.parentFile) {
		include(awFile.name)
		rename(awFile.name, "${mod.id}.accesswidener")
		into("")
	}
}