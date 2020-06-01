package com.mcmoddev.alt;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mcmoddev.alt.data.Config;
import com.mcmoddev.alt.util.ALTFileUtils;

import com.mcmoddev.alt.proxy.ClientProxy;
import com.mcmoddev.alt.proxy.CommonProxy;
import com.mcmoddev.alt.api.PluginLoader;
import com.mcmoddev.alt.commands.ALTDumpCommand;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.loading.FMLPaths;


@Mod(AdditionalLootTables.MODID)
public class AdditionalLootTables {
	public static final String MODID = "alt";
	public static final String VERSION = "2.0.3";

	public static AdditionalLootTables INSTANCE = null;
	public static Config config = null;
	public static Logger logger = LogManager.getFormatterLogger(MODID);
	public static final String ALT_FOLDER_NAME = "additional-loot-tables";
	private static Path lootFolder;
    private static final PluginLoader pluginLoader = new PluginLoader();

    public static CommonProxy proxy = DistExecutor.runForDist(() -> () -> new ClientProxy(), () -> () -> new CommonProxy());

	public AdditionalLootTables() {
		if (INSTANCE == null)
			INSTANCE = this;
		else
			throw new RuntimeException("Duplicated Class Instantiation: AdditionalLootTables");
		
		config = Config.construct();
		lootFolder = Paths.get(FMLPaths.CONFIGDIR.get().toString(), ALT_FOLDER_NAME);
		ALTFileUtils.createDirectoryIfNotPresent(lootFolder);
		
		pluginLoader.load();
	}
    
    // TODO: Fix fingerprint detection, FMLFingerprintViolationEvent is not fired anywhere in FML/Forge!
//	@EventHandler
//	public void onFingerprintViolation(FMLFingerprintViolationEvent event) {
//		logger.warn("Invalid fingerprint detected!");
//	}
    
	@Mod.EventBusSubscriber(modid = AdditionalLootTables.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class ModEventHandler {		
		@SubscribeEvent
		public static void onCommonSetup(FMLCommonSetupEvent event) {
			pluginLoader.loadResources();
		}

		// TODO: check and test inter-mod communication
		@SubscribeEvent
	    public void onIMCProcess(InterModProcessEvent event) {
			/*
			 * Enqueue an IMC named "register" and the resource location with your MODID as domain name
			 * net.minecraftforge.fml.InterModComms.sendTo(Your_MODID, "register", ()->new ResourceLocation(Your_MODID, "anything"));
			 */
	    	event.getIMCStream()
	    	.filter(message -> message.getMethod().equalsIgnoreCase("register"))
	        .forEach(message -> {
	        	Object value = message.getMessageSupplier().get();
	        	if (value instanceof ResourceLocation) {
		            ALTFileUtils.copyFromResourceIfNotPresentFixed((ResourceLocation) value);
	        	}
	        });
	    }
	}
	
	@Mod.EventBusSubscriber(modid = AdditionalLootTables.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
	public static class ForgeEventHandler {
		@SubscribeEvent
		public static void onLootTableLoad(LootTableLoadEvent e) {
			ALTEventHandler.lootLoad(e);
		}
		
		@SubscribeEvent
	    public static void onServerStarting(FMLServerStartingEvent e) {
			ALTDumpCommand.register(e.getCommandDispatcher());
	    }
		
		@SubscribeEvent
	    public static void onServerStopped(FMLServerStoppedEvent e) {
			ALTEventHandler.reset();
		}
	}

	public static Path getLootFolder() {
		return lootFolder;
	}
}
