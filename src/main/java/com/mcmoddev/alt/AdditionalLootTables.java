package com.mcmoddev.alt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mcmoddev.alt.data.Config;
import com.mcmoddev.alt.util.ALTFileUtils;
import com.mcmoddev.alt.util.ResourceLoader;
import com.mcmoddev.alt.proxy.CommonProxy;
import com.mcmoddev.alt.commands.ALTDumpCommand;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = AdditionalLootTables.MODID, version = AdditionalLootTables.VERSION, name= AdditionalLootTables.NAME)
public class AdditionalLootTables {
	public static final String NAME = "Additional Loot Tables";
	public static final String MODID = "alt";
	public static final String VERSION = "1.1.0";

	@Instance
	public static AdditionalLootTables INSTANCE = null;
	public static Config config = null;
	public static Logger logger = LogManager.getFormatterLogger(MODID);
	public static final String ALTFolderName = "additional-loot-tables";
	public static String ALTBaseConfigPath = null;
	private static Path loot_folder;
    private static final String PROXY_BASE = "com.mcmoddev." + MODID + ".proxy.";

    @SidedProxy(clientSide = PROXY_BASE + "ClientProxy", serverSide = PROXY_BASE + "ServerProxy")
    public static CommonProxy proxy;
	
	@EventHandler
	public static void preInit(FMLPreInitializationEvent event) {
		ALTBaseConfigPath = event.getSuggestedConfigurationFile().getParent();
		loot_folder = Paths.get(ALTBaseConfigPath,ALTFolderName);
		config = new Config(event);
		ALTFileUtils.createDirectoryIfNotPresent(loot_folder);
		ResourceLoader.assembleResourcePack();
	}

	private void registerLootTable(Path dir, String tableName) {
		try {
			Files.list(dir).filter( file -> file.toFile().isFile() )
			.filter( file -> Files.isReadable(file) )
			.filter( file -> file.getFileName().toString().endsWith(".json") )
			.forEach( jsonFile -> {
				String jsonName = jsonFile.getFileName().toString();
				String rlocPathSeg = jsonName.substring(0, jsonName.length() - 5);
				LootTableList.register(new ResourceLocation("ALT", String.format("%s/%s", tableName, rlocPathSeg)));
			});
		} catch (IOException e) {
			logger.error("Error registering loot tables: %s", e.getLocalizedMessage());
		}		
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		try {
			Files.list(loot_folder).filter( dir -> dir.toFile().isDirectory() ).forEach( dir -> {
					String tableName = dir.getFileName().toString();
					registerLootTable(dir, tableName);
			});
		} catch(IOException ex ) {
			logger.error("Error registering loot tables: %s", ex.getMessage());
		}
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		// dump existing loot tables here
	}
	
    @EventHandler
    public void onServerStarting(FMLServerStartingEvent ev) {
    	ev.registerServerCommand(new ALTDumpCommand());
    }
    
	public static Path getLootFolder() {
		return loot_folder;
	}

}
