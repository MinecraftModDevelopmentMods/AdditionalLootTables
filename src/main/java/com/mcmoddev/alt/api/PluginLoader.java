package com.mcmoddev.alt.api;

import java.util.ArrayList;
import java.util.List;

import com.mcmoddev.alt.util.ALTFileUtils;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.discovery.ASMDataTable.ASMData;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

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

	private List<PluginData> dataStore = new ArrayList<>();

	private String getAnnotationItem(String item, final ASMData asmData) {
		if (asmData.getAnnotationInfo().get(item) != null) {
			return asmData.getAnnotationInfo().get(item).toString();
		} else {
			if( "resourcePath".equals(item) ) {
				// for some reason the default value is never presevered
				return "alt";
			} else {
				return "";
			}
		}
	}

	public void load(FMLPreInitializationEvent event) {
		for (final ASMData asmDataItem : event.getAsmData().getAll(ALTPlugin.class.getCanonicalName())) {
			final String modId = getAnnotationItem("modid", asmDataItem);
			final String resourceBase = getAnnotationItem("resourcePath", asmDataItem);
			PluginData pd = new PluginData( modId, resourceBase);
			dataStore.add(pd);
		}
	}

	public void loadResources() {
		dataStore.stream().forEach(pd -> ALTFileUtils.copyFromResourceIfNotPresent(pd.getResourceLocation(), pd.getResourcePath()));
	}
}
