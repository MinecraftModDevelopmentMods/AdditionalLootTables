package com.mcmoddev.alt.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.mcmoddev.alt.AdditionalLootTables;

import net.minecraft.crash.CrashReport;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

public class ALTFileUtils {
	public static void createDirectoryIfNotPresent(Path path) {
		if (!path.toFile().exists()) {
				try {
					Files.createDirectory(path);
				} catch (IOException e) {
					CrashReport report = CrashReport.makeCrashReport(e, String.format("Could not create directory %s", path.toString()));
					report.getCategory().addCrashSection("ALT Version", AdditionalLootTables.VERSION);
					AdditionalLootTables.logger.error(report.getCompleteReport());
				}
		}
	}

	public static void copyFromJar(Class<?> jarClass, String filename, File to) {
		URL url = jarClass.getResource("/assets/" + filename);

		try {
			FileUtils.copyURLToFile(url, to);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("resource")
	public static void zipFolderContents(File directory, File zipfile) throws IOException {
		URI base = directory.toURI();
		Deque<File> queue = new LinkedList<>();
		queue.push(directory);
		OutputStream out = new FileOutputStream(zipfile);
		Closeable res = out;
		try {
			ZipOutputStream zout = new ZipOutputStream(out);
			res = zout;
			while (!queue.isEmpty()) {
				directory = queue.pop();
				File[] files = directory.listFiles();
				if (files != null) {
					for (File child : files) {
						String name = base.relativize(child.toURI()).getPath();
						if (child.isDirectory()) {
							queue.push(child);
							name = name.endsWith("/") ? name : name + "/";
							zout.putNextEntry(new ZipEntry(name));
						} else {
							zout.putNextEntry(new ZipEntry(name));
							copy(child, zout);
							zout.closeEntry();
						}
					}
				}
			}
		} finally {
			res.close();
		}
	}    

	/**
	 * @see #zipFolderContents(File, File)
	 */
	private static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		while (true) {
			int readCount = in.read(buffer);
			if (readCount < 0) {
				break;
			}
			out.write(buffer, 0, readCount);
		}
	}

	/**
	 * @see #zipFolderContents(File, File)
	 */
	private static void copy(File file, OutputStream out) throws IOException {
		try (InputStream in = new FileInputStream(file)) {
			copy(in, out);
		}
	}

	public static void safeDelete(File file) {
		try {
			Files.delete(file.toPath());
		} catch (Exception e) {
			AdditionalLootTables.logger.error("Deleting file " + file.getAbsolutePath() + " failed.");
		}
	}

	public static void safeDeleteDirectory(File file) {
		try {
			FileUtils.deleteDirectory(file);
		} catch (Exception e) {
			AdditionalLootTables.logger.error("Deleting directory " + file.getAbsolutePath() + " failed.");
		}
	}

	public static void copyFromResourceIfNotPresent(ResourceLocation value) {
		Path base = Paths.get(AdditionalLootTables.getLootFolder().toString(), value.getNamespace()).normalize();
		createDirectoryIfNotPresent( base );
		ModContainer modContainer = Loader.instance().getIndexedModList().get(value.getNamespace());
		if( modContainer == null ) {
			AdditionalLootTables.logger.error("Unable to get mod container for mod {} - possible malformed ResourceLocation? ({})",
					value.getNamespace(), value.toString());
			return;
		}
		File container = modContainer.getSource();
		Path root = container.toPath().resolve(Paths.get("assets", value.getNamespace(), value.getPath()));
		
		if( !root.toFile().isDirectory() ) {
			AdditionalLootTables.logger.error("Mod {} asked us to load from {} but it is not a directory!", value.getNamespace(), root.toString());
			return;
		}

		Iterator<Path> itr = null;
		Stream<Path> stream = null;
		try {
			stream = Files.walk(root);
			itr = stream.iterator();				
		} catch( IOException e) {
			AdditionalLootTables.logger.error("Getting iterator for resource of mod {}", value.getNamespace(), e);
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
		
		while( itr != null && itr.hasNext() ) {
			Path current = itr.next();
				
			if( current.toFile().isDirectory() ) {
				Path targetDir = base.resolve(current.toFile().getName());
				createDirectoryIfNotPresent( targetDir );
				
				copyFiles( current, targetDir );
			}
		}
	}

	public static void copyFiles(Path sourceDir, Path targetDir) {
		Iterator<Path> itr = null;
		Stream<Path> stream = null;

		try {
			stream = Files.walk(sourceDir);
			itr = stream.iterator();
		} catch( IOException e ) {
			AdditionalLootTables.logger.error("Unable to get iterator for {}", sourceDir, e);
		} finally {
			if (stream != null) {
				stream.close();
			}
		}

		while( itr != null && itr.hasNext() ) {
			Path current = itr.next();

			if( "json".equals(FilenameUtils.getExtension(current.toFile().getName())) ) {
				File targetFile = Paths.get(targetDir.toString(), current.toFile().getName()).toFile();
				// only copy out if the target file doesn't already exist
				// this way we don't overwrite users customized files
				if( !targetFile.exists() ) {
					OutputStream out = null;
					try {
						out = new FileOutputStream(Paths.get(targetDir.toAbsolutePath().toString(), current.toFile().getName()).toAbsolutePath().toString());
						copy(current.toFile(), out);
					} catch (FileNotFoundException e) {
						AdditionalLootTables.logger.error("Unable to create output stream for {}", Paths.get(targetDir.toAbsolutePath().toString(), current.toFile().getName()).toAbsolutePath().toString(), e);					
					} catch (IOException e) {
						AdditionalLootTables.logger.error("Error copying config over", e);					
					} finally {
						try {
							if( out != null ) out.close();
						} catch(IOException e) {
							AdditionalLootTables.logger.error("Unable to close output stream", e);					
						}
					}
				}
			}
		}
	}
}
