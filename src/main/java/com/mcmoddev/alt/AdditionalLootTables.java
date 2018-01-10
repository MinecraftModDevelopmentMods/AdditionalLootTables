package com.mcmoddev.alt;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mcmoddev.alt.data.Config;
import com.mcmoddev.alt.util.ALTFileUtils;
import com.mcmoddev.alt.proxy.CommonProxy;
import com.mcmoddev.alt.api.PluginLoader;
import com.mcmoddev.alt.commands.ALTDumpCommand;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent; 
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = AdditionalLootTables.MODID,
	version = AdditionalLootTables.VERSION,
	name = AdditionalLootTables.NAME,
	acceptedMinecraftVersions = "[1.11.2,)",
	certificateFingerprint = "@FINGERPRINT@")
public class AdditionalLootTables {
	public static final String NAME = "Additional Loot Tables";
	public static final String MODID = "alt";
	public static final String VERSION = "2.0.3";

	@Instance
	public static AdditionalLootTables INSTANCE = null;
	public static Config config = null;
	public static Logger logger = LogManager.getFormatterLogger(MODID);
	public static final String ALT_FOLDER_NAME = "additional-loot-tables";
	private static Path loot_folder;
    private static final String PROXY_BASE = "com.mcmoddev." + MODID + ".proxy.";
    private static final PluginLoader pluginLoader = new PluginLoader();
    
    @SidedProxy(clientSide = PROXY_BASE + "ClientProxy", serverSide = PROXY_BASE + "ServerProxy")
    public static CommonProxy proxy;

	@EventHandler
	public void onFingerprintViolation(FMLFingerprintViolationEvent event) {
		logger.warn("Invalid fingerprint detected!");
	}

	@EventHandler
	public static void preInit(FMLPreInitializationEvent event) {
		final String ALT_BASE_CONFIG_PATH = event.getSuggestedConfigurationFile().getParent();
		loot_folder = Paths.get(ALT_BASE_CONFIG_PATH, ALT_FOLDER_NAME);
		config = new Config(event);
		ALTFileUtils.createDirectoryIfNotPresent(loot_folder);
		pluginLoader.load(event);
		MinecraftForge.EVENT_BUS.register(ALTEventHandler.class);
	}

	
	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		pluginLoader.loadResources();
		// do nothing here
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		// we do nothing for now
	}
	
    @EventHandler
    public void onServerStarting(FMLServerStartingEvent ev) {
    	ev.registerServerCommand(new ALTDumpCommand());
    }
    
    @EventHandler
    public void onIMC(FMLInterModComms.IMCEvent event) {
        event.getMessages().stream().filter(message -> message.key.equalsIgnoreCase("register"))
        .forEach(message -> {
            ResourceLocation value = message.getResourceLocationValue();
            ALTFileUtils.copyFromResourceIfNotPresent(value);
        });
    }
    
	public static Path getLootFolder() {
		return loot_folder;
	}

}
