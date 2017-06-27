package com.mcmoddev.alt.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Deque;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;

import com.mcmoddev.alt.AdditionalLootTables;

public class ALTFileUtils {
	public static void createDirectoryIfNotPresent(Path path) {
		if (path.toFile().exists()) {
			try {
				Files.createDirectory(path);
			} catch(IOException ex) {
				AdditionalLootTables.logger.fatal("Could not create %s: %s", path.toString(), ex.getMessage());
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
			file.delete();
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

}
