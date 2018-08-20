package com.mcmoddev.alt.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.mcmoddev.alt.AdditionalLootTables;

import net.minecraft.crash.CrashReport;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

public class ALTFileUtils {
	private static final String ALT_VERSION = "ALT Version";

	public static void copyFromResourceIfNotPresentFixed(ResourceLocation value) {
		copyFromResourceIfNotPresent(value, "alt");
	}
	
	public static void copyFromResourceIfNotPresent(ResourceLocation value, String modDir) {
		Path outputPath;             // base path for the output
		ModContainer modContainer;   // mod container of mod we need to copy from
		
		// loot folder + modid == path
		outputPath = Paths.get(AdditionalLootTables.getLootFolder().toString(), value.getNamespace());
		createDirectoryIfNotPresent(outputPath); // directory must exist if we're going to copy stuff into it and all that
		
		modContainer = Loader.instance().getIndexedModList().get(value.getNamespace());
		if (modContainer == null) {
			AdditionalLootTables.logger.error("Unable to get Mod Container for mod {}", value.getNamespace());
			return;
		}
		
		Path corePath = Paths.get("assets", value.getNamespace(), modDir);
		
		// walk the specified mods resources, find the json's we're looking for
		// replicate the directory tree and copy out the mods
		URL bit = modContainer.getClass().getClassLoader().getResource(corePath.toString());
		if (bit == null) {
			AdditionalLootTables.logger.error("Mod {} does not contain resources at {}", modContainer.getModId(), corePath.toString());
			return;
		}

		JarURLConnection urlcon;
		try {
			urlcon = (JarURLConnection) (bit.openConnection());
			tryCopy(urlcon, bit, corePath);
		} catch (IOException e2) {
			CrashReport report = CrashReport.makeCrashReport(e2, String.format("jarURLConnection for %s failed to open", bit.toString()));
			report.getCategory().addCrashSection(ALT_VERSION, AdditionalLootTables.VERSION);
			AdditionalLootTables.logger.error(report.getCompleteReport());
		}
	}

	private static void tryCopy(JarURLConnection urlcon, URL bit, Path corePath) {
		try (JarFile jar = urlcon.getJarFile();) {
			Enumeration<JarEntry> entries = jar.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				String entryName = entry.getName();
				String cpn = corePath.toString().replaceAll("\\", "/");
				String actName = entryName.substring(0, cpn.length());
				if(Paths.get(entryName).startsWith(corePath)) {
					if(entry.isDirectory() && actName.trim().length() != 0) {
						createDirectoryIfNotPresent(corePath.resolve(actName));
					} else if(entryName.endsWith(".json") && !corePath.resolve(actName).toFile().exists()) {
						doCopy(jar.getInputStream(entry), corePath.resolve(actName), entryName);
					}
				}
			}
		} catch (IOException e1) {
			CrashReport report = CrashReport.makeCrashReport(e1, String.format("jarURLConnection for %s failed to return a JarFile", bit.toString()));
			report.getCategory().addCrashSection(ALT_VERSION, AdditionalLootTables.VERSION);
			AdditionalLootTables.logger.error(report.getCompleteReport());
		}
	}
	
	private static void doCopy(InputStream input, Path targetPath, String name) {
		try {
			Files.copy(input, targetPath);
		} catch (IOException e) {
			CrashReport report = CrashReport.makeCrashReport(e, String.format("Files.copy(%s, %s) failed", 
					name, targetPath));
			report.getCategory().addCrashSection(ALT_VERSION, AdditionalLootTables.VERSION);
			AdditionalLootTables.logger.error(report.getCompleteReport());
		}
	}
	
	public static void createDirectoryIfNotPresent(Path lootFolder) {
		if(lootFolder.toFile().exists()) return;
		lootFolder.toFile().mkdir();
	}
	
}
