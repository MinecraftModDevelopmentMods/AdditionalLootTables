package com.mcmoddev.alt.util;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;

import com.mcmoddev.alt.AdditionalLootTables;

import static com.mcmoddev.alt.AdditionalLootTables.MODID;

/**
 * Borrowed from BASE
 * (https://raw.githubusercontent.com/The-Acronym-Coders/BASE/develop/1.11.2/src/main/java/com/teamacronymcoders/base/util/files/ResourcePackAssembler.java)
 *
 * Lightly modified to make it useful for the work we need.
 */
@SideOnly(Side.CLIENT)
public class ResourceLoader {
    private static ResourcePackAssembler assembler;
    
    public static void assembleResourcePack() {
    	File resourceFolder = new File(AdditionalLootTables.ALTBaseConfigPath);
        File resource = resourceFolder.getParentFile();
        assembler = new ResourcePackAssembler(new File(resource, AdditionalLootTables.ALTFolderName),
                "Additional Loot Tables Resources", MODID);
        createImportantFolders();

        copyFilesFromFolder("", resourceFolder);
        assembler.assemble().inject();
    }

    private static void copyFilesFromFolder(String path, File folder) {
    	File[] files = folder.listFiles();
    	if (files != null) {
    		for (File file : files) {
    			if (file.isDirectory() && !"dumps".equals(file.getName())) {
    				copyFilesFromFolder(path + "/" + file.getName(), file);
    			} else {
    				if(file.toString().endsWith(".json")) {
    					assembler.addFile(path, file);
    				}
    			}
    		}
    	}
    }

    private static void createImportantFolders() {
    	// do nothing
    	
    }

}
