package com.mcmoddev.alt.commands;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.mcmoddev.alt.AdditionalLootTables;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.LootTableManager;

import static com.mcmoddev.alt.data.Constants.GSON;

public class ALTDumpCommand extends CommandBase {

	@Override
	public int compareTo(ICommand arg0) {
		return this.getName().compareTo(arg0.getName());
	}

	@Override
	public String getName() {
		return "alt-dump";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/alt-dump";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		World w = AdditionalLootTables.proxy.getWorld();
		
		if( w.isRemote ) {
			return;
		}

		Gson prettyPrinter = new GsonBuilder().setPrettyPrinting().create();
		JsonParser parser = new JsonParser();
		LootTableManager manager = w.getLootTableManager();
		LootTableList.getAll().forEach( resLoc -> {
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

				String basic = GSON.toJson(table);
				String prettified = prettyPrinter.toJson(parser.parse(basic));
				FileUtils.writeStringToFile(f, prettified);
			} catch( IOException e ) {
				AdditionalLootTables.logger.error("Error writing loot table %s : %s", f.getPath(), e.getMessage());
			}
		});
	}
}
