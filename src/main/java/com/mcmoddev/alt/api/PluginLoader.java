package com.mcmoddev.alt.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.mcmoddev.alt.AdditionalLootTables;
import com.mcmoddev.alt.util.ALTFileUtils;

import net.minecraft.crash.CrashReport;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.discovery.ASMDataTable.ASMData;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class PluginLoader {

	private static final String ALT_VERSION = "ALT Version";

	private class PluginData {
		private final String modId;
		private final String resourcePath;
		
		PluginData( String modId, String resourcePath ) {
			this.modId = modId;
			this.resourcePath = resourcePath;
		}
		
		String getModId() {
			return this.modId;
		}

		String getResourcePath() {
			return this.resourcePath;
		}

		String getCompletePath() {
			return String.format("assets/%s/%s", this.modId, this.resourcePath);
		}

		ResourceLocation getResourceLocation() {
			return new ResourceLocation(this.modId, this.resourcePath);
		}
	}

	private List<PluginData> dataStore = new ArrayList<>();

	private String getAnnotationItem(String item, final ASMData asmData) {
		if (asmData.getAnnotationInfo().get(item) != null) {
			return asmData.getAnnotationInfo().get(item).toString();
		} else {
			if( "resourcePath".equals(item) ) {
				// for some reason the default value is never presevered
				return "alt";
			} else {
				return "";
			}
		}
	}

	public void load(FMLPreInitializationEvent event) {
		for (final ASMData asmDataItem : event.getAsmData().getAll(ALTPlugin.class.getCanonicalName())) {
			final String modId = getAnnotationItem("modid", asmDataItem);
			final String resourceBase = getAnnotationItem("resourcePath", asmDataItem);
			PluginData pd = new PluginData( modId, resourceBase);
			dataStore.add(pd);
		}
	}

	public void loadResources() {
		dataStore.stream().forEach( pd -> {
			ALTFileUtils.createDirectoryIfNotPresent(Paths.get(AdditionalLootTables.getLootFolder().toString(), pd.modId));
			URL resource = getClass().getClassLoader().getResource(pd.getCompletePath());
			
			URI resourceURI = getURI(resource, pd.getResourceLocation());
			
			Path myPath = null;
			FileSystem fileSystem = null;
			String tName = null;
			
			try {
				if (resourceURI.getScheme().equals("jar")) {
					fileSystem = FileSystems.newFileSystem(resourceURI, Collections.<String, Object>emptyMap());
					myPath = fileSystem.getPath(pd.getCompletePath());
				} else {
					myPath = Paths.get(resourceURI);
				}
			} catch( IOException e ) {
				CrashReport report = CrashReport.makeCrashReport(e, String.format("Failed to get FileSystem for %s", resourceURI.toString()));
				report.getCategory().addCrashSection(ALT_VERSION, AdditionalLootTables.VERSION);
				fileSystem = null;
				myPath = null;
				AdditionalLootTables.logger.error(report.getCompleteReport());
			}
			
			if( myPath == null || fileSystem == null ) {
				return;
			}

			try {
				Stream<Path> walk = Files.walk(myPath, 1);			
				if( walk == null ) {
					return;
				}

				boolean topDir = true;
				
				for( Iterator<Path> iter = walk.iterator(); iter.hasNext(); ) {
					Path p = iter.next();
					
					if( Files.isDirectory(p) && !topDir) {
						tName = Paths.get(AdditionalLootTables.getLootFolder().toString(), 
								pd.getModId(), p.getFileName().toString()).toString();
						ALTFileUtils.createDirectoryIfNotPresent( Paths.get(tName) );

						copyConfigFiles( p, tName );
					} else {
						topDir = false;
					}

				}
				walk.close();
			} catch (IOException e) {
				CrashReport report = CrashReport.makeCrashReport(e, String.format("Failed to get filesystem iterator for %s", myPath.toString()));
				report.getCategory().addCrashSection(ALT_VERSION, AdditionalLootTables.VERSION);
				AdditionalLootTables.logger.error(report.getCompleteReport());
			} finally {
				IOUtils.closeQuietly(fileSystem);
			}
 		});
	}

	private void copyConfigFiles(Path p, String tName) {
		Stream<Path> walk = null;
		try {
			walk = Files.walk(p, 1);			
			if( walk == null ) {
				return;
			}

			for( Iterator<Path> iter = walk.iterator(); iter.hasNext(); ) {
				Path next = iter.next();

				if( "json".equals(FilenameUtils.getExtension(next.getFileName().toString()))) {
					File target = Paths.get(tName, next.getFileName().toString()).toFile();
					String name = target.toString();

					if( !target.exists() ) {
						try {
							InputStream reader = Files.newInputStream(next);
							FileUtils.copyInputStreamToFile(reader, target);
							IOUtils.closeQuietly(reader);
						} catch(IOException ex) {
							CrashReport report = CrashReport.makeCrashReport(ex, String.format("Failed to copy file %s to %s", next.toUri().toURL().toString(), Paths.get(name).toString()));
							report.getCategory().addCrashSection(ALT_VERSION, AdditionalLootTables.VERSION);
							AdditionalLootTables.logger.error(report.getCompleteReport());
						}
					}
				}
			}
			walk.close();
		} catch( IOException e ) {
			CrashReport report = CrashReport.makeCrashReport(e, String.format("Failed to get filesystem iterator for %s", p.toString()));
			report.getCategory().addCrashSection(ALT_VERSION, AdditionalLootTables.VERSION);
			AdditionalLootTables.logger.error(report.getCompleteReport());
		} finally {
			if (walk != null) {
				walk.close();
			}
		}
	}
	
	private URI getURI(URL resource, ResourceLocation loc) {
		URI uri;
		try {
			uri = resource.toURI();
		} catch (URISyntaxException e) {
			CrashReport report = CrashReport.makeCrashReport(e, String.format("Failed to get URI for %s", loc.toString()));
			report.getCategory().addCrashSection(ALT_VERSION, AdditionalLootTables.VERSION);
			AdditionalLootTables.logger.error(report.getCompleteReport());
			return null;
		}
		return uri;
	}
}
