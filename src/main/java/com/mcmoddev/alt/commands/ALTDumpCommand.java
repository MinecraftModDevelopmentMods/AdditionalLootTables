package com.mcmoddev.alt.commands;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.mcmoddev.alt.ALTEventHandler;
import com.mcmoddev.alt.AdditionalLootTables;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableManager;

public class ALTDumpCommand {
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		LiteralArgumentBuilder<CommandSource> literalargumentbuilder = Commands.literal("alt-dump")
				.requires(source -> source.hasPermissionLevel(2))
				.executes(context -> execute(context.getSource()));
		
		dispatcher.register(literalargumentbuilder);
	}

	public static int execute(CommandSource sender) throws CommandException {
		if(sender.getWorld().isRemote) {
			// The execution should only happen on the logical server side
			return 0;
		}

		Gson prettyPrinter = new GsonBuilder().setPrettyPrinting().create();
		JsonParser parser = new JsonParser();

		LootTableManager manager = sender.getServer().getLootTableManager();
		manager.getLootTableKeys().forEach( resLoc -> {
			LootTable table = manager.getLootTableFromLocation(resLoc);
			String dirName = resLoc.getNamespace();
			String fileName = String.format("%s.json", resLoc.getPath());
			File f = Paths.get(AdditionalLootTables.getLootFolder().toString()+"-dumps",dirName,fileName).toFile();
			
			try {
				if( !f.getParentFile().exists() ) {
					f.getParentFile().mkdirs();
				} 
				
				if( !f.exists() ) {
					f.createNewFile();
				}

				String basic = ALTEventHandler.GSON.getValue().toJson(table);
				String prettified = prettyPrinter.toJson(parser.parse(basic));
				FileUtils.writeStringToFile(f, prettified, Charset.defaultCharset(), false);
			} catch( IOException e ) {
				AdditionalLootTables.logger.error("Error writing loot table %s : %s", f.getPath(), e.getMessage());
			}
		});
		
		return Command.SINGLE_SUCCESS;
	}
}
