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
	private class PluginData {
		private final String modId;
		private final String resourcePath;
		
		PluginData( String modId, String resourcePath ) {
			this.modId = modId;
			this.resourcePath = resourcePath;
		}
		
		String getModId() {
			return this.modId;
		}

		String getResourcePath() {
			return this.resourcePath;
		}

		String getCompletePath() {
			return String.format("assets/%s/%s", this.modId, this.resourcePath);
		}

		ResourceLocation getResourceLocation() {
			return new ResourceLocation(this.modId, this.resourcePath);
		}
	}

	private final List<PluginData> dataStore = new ArrayList<>();

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
    				PluginData pd = new PluginData(modId, resourceBase);
    				dataStore.add(pd);
                });
	}

	public void loadResources() {
		dataStore.forEach(pd -> ALTFileUtils.copyFromResourceIfNotPresent(pd.getResourceLocation(), pd.getResourcePath()));
	}
}
