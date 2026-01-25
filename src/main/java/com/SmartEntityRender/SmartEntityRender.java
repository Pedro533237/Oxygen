package com.SmartEntityRender;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmartEntityRender implements ModInitializer {
	public static final String MOD_ID = "smartentityrender";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");

		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			ser$initClient();
		}
	}

	private static void ser$initClient() {
		try {
			LOGGER.info("=================================================");
			LOGGER.info("SmartEntityRender initializing...");
			LOGGER.info("=================================================");

			ClassLoader cl = SmartEntityRender.class.getClassLoader();

			// Config
			Class<?> configClass = Class.forName("com.SmartEntityRender.config.Config", true, cl);
			Object config = configClass.getMethod("getInstance").invoke(null);
			Object configVersion = 7;
			LOGGER.info("✓ Configuration loaded (v{})", configVersion);

			// WhitelistManager
			Class<?> whitelistClass = Class.forName("com.SmartEntityRender.config.WhitelistManager", true, cl);
			Object whitelist = whitelistClass.getMethod("getInstance").invoke(null);
			Object blockEntities = whitelistClass.getMethod("getBlockEntityWhitelist").invoke(whitelist);
			Object entities = whitelistClass.getMethod("getEntityWhitelist").invoke(whitelist);
			Object tickCulling = whitelistClass.getMethod("getTickCullingWhitelist").invoke(whitelist);
			int blockCount = ((java.util.Collection<?>) blockEntities).size();
			int entityCount = ((java.util.Collection<?>) entities).size();
			int tickCount = ((java.util.Collection<?>) tickCulling).size();
			LOGGER.info("✓ Whitelists initialized:");
			LOGGER.info("  - Block Entities: {} entries", blockCount);
			LOGGER.info("  - Entities: {} entries", entityCount);
			LOGGER.info("  - Tick Culling: {} entries", tickCount);

			// Occlusion culling
			Class<?> occClass = Class.forName("com.SmartEntityRender.culling.OcclusionCullingInstance", true, cl);
			occClass.getMethod("getInstance").invoke(null);
			LOGGER.info("✓ Occlusion Culling System initialized");
			LOGGER.info("  - Async path-tracing: ENABLED");
			int tracingDistance = (int) configClass.getMethod("getTracingDistance").invoke(config);
			int sleepDelay = (int) configClass.getMethod("getSleepDelay").invoke(config);
			boolean tickEnabled = (boolean) configClass.getMethod("isTickCulling").invoke(config);
			LOGGER.info("  - Tracing distance: {} blocks", tracingDistance);
			LOGGER.info("  - Sleep delay: {}ms", sleepDelay);
			LOGGER.info("  - Tick culling: {}", tickEnabled ? "ENABLED" : "DISABLED");

			// Active features
			boolean skipEntity = (boolean) configClass.getMethod("isSkipEntityCulling").invoke(config);
			boolean skipBlockEntity = (boolean) configClass.getMethod("isSkipBlockEntityCulling").invoke(config);
			boolean frustum = (boolean) configClass.getMethod("isBlockEntityFrustumCulling").invoke(config);
			boolean nametags = (boolean) configClass.getMethod("isRenderNametagsThroughWalls").invoke(config);
			boolean debug = (boolean) configClass.getMethod("isDebugMode").invoke(config);
			LOGGER.info("");
			LOGGER.info("Active Features:");
			LOGGER.info("  ✓ Entity Culling: {}", !skipEntity);
			LOGGER.info("  ✓ Block Entity Culling: {}", !skipBlockEntity);
			LOGGER.info("  ✓ Frustum Culling: {}", frustum);
			LOGGER.info("  ✓ Tick Culling: {}", tickEnabled);
			LOGGER.info("  ✓ Nametags Through Walls: {}", nametags);
			LOGGER.info("  ✓ Debug Mode: {}", debug);

			// Debug renderer
			Class<?> debugClass = Class.forName("com.SmartEntityRender.debug.DebugRenderer", true, cl);
			debugClass.getMethod("getInstance").invoke(null);
			LOGGER.info("  ✓ Debug Renderer: INITIALIZED");

			// Camera Zoom
			Class<?> zoomClass = Class
					.forName("com.SmartEntityRender.video_settings.includes.addons.zoom.Zoom", true, cl);
			zoomClass.getMethod("register").invoke(null);
			LOGGER.info("  ✓ Camera Zoom: REGISTERED (Key: C)");

			LOGGER.info("");
			LOGGER.info("=================================================");
			LOGGER.info("SmartEntityRender initialized successfully!");
			LOGGER.info("=================================================");
		} catch (Throwable t) {
			LOGGER.warn("SmartEntityRender client init failed", t);
		}
	}
}