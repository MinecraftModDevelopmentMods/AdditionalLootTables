package com.mcmoddev.alt.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Type;

import com.mcmoddev.alt.util.ALTFileUtils;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;

public class PluginLoader {
	private final List<ResourceLocation> dataStore = new ArrayList<>();

	public void load() {
		Type altplugin = Type.getType(ALTPlugin.class);
        ModList.get().getAllScanData().stream()
                .map(ModFileScanData::getAnnotations)
                .flatMap(Collection::stream)
                .filter(annoData -> altplugin.equals(annoData.getAnnotationType()))
                .forEach(annoData -> {
                	Map<String,Object> data = annoData.getAnnotationData();
    				final String modId = (String) data.get("modid");
    				final String resourceBase = (String) data.get("resourcePath");
    				dataStore.add(new ResourceLocation(modId, resourceBase));
                });
	}

	public void loadResources() {
		dataStore.forEach(resLoc -> ALTFileUtils.copyFromResourceIfNotPresent(resLoc));
	}
}
