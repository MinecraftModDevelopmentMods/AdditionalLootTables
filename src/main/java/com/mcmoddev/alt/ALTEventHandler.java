package com.mcmoddev.alt;

//import static com.mcmoddev.alt.data.Constants.GSON;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Queues;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.EventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class ALTEventHandler {
	private static Gson GSON = null;
	private static JsonParser parser = new JsonParser();
	private static final AtomicInteger hashCounter = new AtomicInteger(0);
	private static final List<String> alreadyLoaded = new ArrayList<>();

	public static Gson getGson() {
		if (ALTEventHandler.GSON == null) {
			ALTEventHandler.GSON = ObfuscationReflectionHelper.getPrivateValue(LootTableManager.class, null, "field_186526_b");
		}

		return ALTEventHandler.GSON;
	}

	public static void lootLoad(LootTableLoadEvent evt) {

		
		if (evt.getName().toString().startsWith("minecraft:")) {
			String filename = String.format("%s.json", evt.getName().toString().split(":")[1]);
			Stream<Path> stream = null;
			try {
				Path p = Paths.get(AdditionalLootTables.getLootFolder().toString());
				stream = Files.list(p);

				stream
				.filter( pos -> pos.toFile().isDirectory() )
				.filter( pos -> Paths.get(pos.toString(), filename).toFile().exists() )
				.forEach( pos -> {
					try {
						File theFile = Paths.get(pos.toString(), filename).toFile();
						if( !alreadyLoaded.contains(theFile.getCanonicalPath())) {
							String data = FileUtils.readFileToString(theFile, Charset.forName("UTF-8"));
							JsonElement baseParse = parser.parse(data);
							if( baseParse.isJsonObject() && !baseParse.isJsonNull()) {
								JsonObject root = baseParse.getAsJsonObject();
								JsonArray pools = root.get("pools").getAsJsonArray();
								for( int i = 0; i < pools.size(); i++ ) {
									JsonElement pool = pools.get(i);
									// TODO: We may not have to disable the event bus
//									Object busCache = hackDisableEventBus();
									String category = evt.getName().toString().split(":")[1].split("/")[0];
									String entry = evt.getName().toString().split(":")[1].split("/")[1]; 
									pushLootTableContext(category,entry);

									JsonObject work = pool.getAsJsonObject();
									work.addProperty("name", String.format("_entry_%d", hashCounter.incrementAndGet()));

									LootPool thePool = getGson().fromJson(getGson().toJson(work), LootPool.class);
									if( thePool != null ) {
										evt.getTable().addPool(thePool);
									}
									popLootTableContext();
//									hackEnableEventBus(busCache);
								}
							}
							alreadyLoaded.add(theFile.getCanonicalPath());
						}
					}catch(NoSuchFieldException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalStateException | InstantiationException | IOException e) {
						AdditionalLootTables.logger.fatal("Exception in adding potential added loot table: %s", e.toString());
					}
				});
			} catch(IOException e) {
				AdditionalLootTables.logger.fatal("Exception in finding potential added loot tables: %s", e.toString());
			} finally {
				if (stream != null) {
					stream.close();
				}
			}
		}
	}
	
	public static void reset() {
		hashCounter.set(0);
		alreadyLoaded.clear();
	}

	private static final String LOOT_TABLE_CONTEXT = ForgeHooks.class.getCanonicalName()+"$LootTableContext";
	private static final String LOOT_CONTEXT = "lootContext";
	private static final String EVENT_BUS_FIELD_NAME = "EVENT_BUS";

	private static final void removeFinalModifierFromField(Field f) throws NoSuchFieldException, IllegalAccessException {
		// Warning: invoking shadow magic
		if((f.getModifiers() & Modifier.FINAL) == 0)
			return; // already done
		Field modifiers = Field.class.getDeclaredField("modifiers");
		modifiers.setAccessible(true);
		modifiers.set(f,(int)modifiers.get(f) & ~Modifier.FINAL);
	}

	private static Object hackDisableEventBus() throws NoSuchFieldException, IllegalAccessException {
		Object cache = MinecraftForge.EVENT_BUS;
		Field busField = MinecraftForge.class.getDeclaredField(EVENT_BUS_FIELD_NAME);
		busField.setAccessible(true);
		removeFinalModifierFromField(busField);
//		busField.set(null, new EventBus());	// TODO: Check this

		return cache;
	}

	private static void hackEnableEventBus(Object cache) throws NoSuchFieldException, IllegalAccessException {
		Field busField = MinecraftForge.class.getDeclaredField(EVENT_BUS_FIELD_NAME);
		busField.setAccessible(true);
		removeFinalModifierFromField(busField);
		busField.set(null,cache);
	}

	private static void popLootTableContext() throws NoSuchFieldException, IllegalAccessException {
		ThreadLocal<Deque> contextQ = hackLootTableContextDeque();
		if(contextQ.get() != null){
			contextQ.get().pop();
		}
	}


	private static void pushLootTableContext(String category, String entry) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
		ThreadLocal<Deque> contextQ = hackLootTableContextDeque();
		if(contextQ.get() == null){
			contextQ.set(Queues.newArrayDeque());
		}
		Object ctx = hackNewLootTableContext(new ResourceLocation(category,entry),false);
		contextQ.get().push(ctx);
	}

	private static ThreadLocal<Deque> hackLootTableContextDeque() throws IllegalAccessException, NoSuchFieldException {
		Field variable = ForgeHooks.class.getDeclaredField(LOOT_CONTEXT);
		variable.setAccessible(true);
		return (ThreadLocal<Deque>) variable.get(null);
	}

	private static Object hackNewLootTableContext(ResourceLocation rsrc, boolean isCustom) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

		Class<?> ctxClass = Class.forName(LOOT_TABLE_CONTEXT);
		Constructor<?> constructor = ctxClass.getDeclaredConstructor(ResourceLocation.class, boolean.class);
		constructor.setAccessible(true);
		return constructor.newInstance(rsrc,isCustom);
	}

}
