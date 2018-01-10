package com.mcmoddev.alt.proxy;

import com.mcmoddev.alt.AdditionalLootTables;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.server.FMLServerHandler;

public class ServerProxy extends CommonProxy {
	@Override
    public World getWorld() {
		MinecraftServer mcServer = FMLServerHandler.instance().getServer();
		
		if( mcServer.worldServers.length == 0 ) {
			AdditionalLootTables.logger.warn("No World Ready Yet!");
			return null;
		} else {
			return mcServer.getEntityWorld();
		}
    }

}
