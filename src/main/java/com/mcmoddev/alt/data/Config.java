package com.mcmoddev.alt.data;

import org.apache.commons.lang3.tuple.Pair;

import com.mcmoddev.alt.AdditionalLootTables;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class Config {
	public final ForgeConfigSpec.BooleanValue enabled;
	public final ForgeConfigSpec.BooleanValue strict_mode;
	
	
	public static Config construct() {
		Pair<Config, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Config::new);
		ForgeConfigSpec configSpec = specPair.getRight();

		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, configSpec);
		return specPair.getLeft();
	}
	
	public Config(ForgeConfigSpec.Builder builder) {
		builder.push("Common Settings");
		enabled = buildBoolean(builder, "enable", true,
				"If true, then this mod will look in the config/additional-loot-tables folder for loot_table json files and merge \n" +
				"them with the existing loot tables");
		strict_mode = buildBoolean(builder, "strict", false,
				"If true, then any errors while parsing/loading loot tables will crash the game. If false, then there will be an \n" +
				"error message in the log but no crash.");
		builder.pop();
	}
	
	private static ForgeConfigSpec.BooleanValue buildBoolean(ForgeConfigSpec.Builder builder, String key,
			boolean defaultVal, String comment) {
		String modID = AdditionalLootTables.MODID;
		return builder.comment(comment + "\r\nDefault: " + defaultVal)
				.translation(modID + ".config." + key.toLowerCase().replace(' ', '_')).define(key, defaultVal);
	}
}
