package com.mcmoddev.alt.proxy;

import com.mcmoddev.alt.AdditionalLootTables;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public class ServerProxy extends CommonProxy {
	@Override
    public World getWorld() {
//		MinecraftServer mcServer = FMLServerHandler.instance().getServer();
//		
//		if( mcServer.worlds.length == 0 ) {
//			AdditionalLootTables.logger.warn("No World Ready Yet!");
			return null;
//		} else {
//			return mcServer.getEntityWorld();
//		}
    }

}
