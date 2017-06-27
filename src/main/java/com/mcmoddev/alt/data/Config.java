package com.mcmoddev.alt.data;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class Config {
	public final boolean enabled;
	public final boolean strict_mode;
	
	public Config(FMLPreInitializationEvent event) {
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		enabled = config.getBoolean("enable", "Additional Loot Tables", true,
				"If true, then this mod will look in the config/additional-loot-tables folder for loot_table json files and merge \n" +
				"them with the existing loot tables");
		strict_mode = config.getBoolean("strict", "Additional Loot Tables", false,
				"If true, then any errors while parsing/loading loot tables will crash the game. If false, then there will be an \n" +
				"error message in the log but no crash.");

		config.save();
	}
}
