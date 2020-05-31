package com.mcmoddev.alt.api;

import java.util.ArrayList;
import java.util.List;

import com.mcmoddev.alt.util.ALTFileUtils;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModList;

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
		ModList.get().forEachModContainer((modid, container) -> {
			Object modInstance = container.getMod();
			Class<?> modClass = modInstance.getClass();
			ALTPlugin annotation = modClass.getAnnotation(ALTPlugin.class);
			if (annotation != null) {
				final String modId = annotation.modid();
				final String resourceBase = annotation.resourcePath();
				PluginData pd = new PluginData( modId, resourceBase);
				dataStore.add(pd);
			}
		});
	}

	public void loadResources() {
		dataStore.forEach(pd -> ALTFileUtils.copyFromResourceIfNotPresent(pd.getResourceLocation(), pd.getResourcePath()));
	}
}
