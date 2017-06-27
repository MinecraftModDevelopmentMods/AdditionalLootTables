package com.mcmoddev.alt.commands;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.mcmoddev.alt.AdditionalLootTables;
import com.mcmoddev.alt.util.PatchedLootEntrySerialiser;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;

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

		Gson gson = new GsonBuilder()
				.registerTypeAdapter(RandomValueRange.class, new RandomValueRange.Serializer())
				.registerTypeAdapter(LootPool.class, new LootPool.Serializer())
				.registerTypeAdapter(LootTable.class, new LootTable.Serializer())
				.registerTypeHierarchyAdapter(LootEntry.class, new PatchedLootEntrySerialiser())
				.registerTypeHierarchyAdapter(LootFunction.class, new LootFunctionManager.Serializer())
				.registerTypeHierarchyAdapter(LootCondition.class, new LootConditionManager.Serializer())
				.registerTypeHierarchyAdapter(LootContext.EntityTarget.class, new LootContext.EntityTarget.Serializer())
				.setPrettyPrinting().create();

		LootTableList.getAll().forEach( resLoc -> {
			LootTable table = w.getLootTableManager().getLootTableFromLocation(resLoc);
			String dirName = resLoc.getResourceDomain();
			String fileName = String.format("%s.json", resLoc.getResourcePath());
			File f = Paths.get(AdditionalLootTables.getLootFolder().toString(),dirName,fileName).toFile();
			
			try {
				if( !f.getParentFile().exists() ) {
					f.getParentFile().mkdirs();
				} 
				
				if( !f.exists() ) {
					f.createNewFile();
				}
				
				FileUtils.writeStringToFile(f, gson.toJson(table));
			} catch( IOException e ) {
				AdditionalLootTables.logger.error("Error writing loot table %s : %s", f.getPath(), e.getMessage());
			}
		});
	}
}
