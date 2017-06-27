package com.mcmoddev.alt.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

public class ClientProxy extends CommonProxy {

	@Override
    public World getWorld() {
		Minecraft mc = Minecraft.getMinecraft();
		
		if( mc.isIntegratedServerRunning() ) {
			return mc.getIntegratedServer().getEntityWorld();
		} else {
			return mc.world;
		}
    }

}
