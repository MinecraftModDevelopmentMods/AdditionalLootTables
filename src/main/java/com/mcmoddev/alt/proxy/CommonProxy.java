package com.mcmoddev.alt.proxy;

import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) {
    	// nothing to see here, move along
    }

    public void init(FMLInitializationEvent event) {
    	// nothing to see here, move along
    }

    public void postInit(FMLPostInitializationEvent event) {
    	// nothing to see here, move along
    }

    public World getWorld() {
    	return null;
    }
}
