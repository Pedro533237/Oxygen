package com.SmartEntityRender.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Configuration System - نظام التكوين
 * يدير جميع إعدادات SmartEntityRender
 */
public class Config {
    private static final Logger LOGGER = LoggerFactory.getLogger("SmartEntityRender");
    private static final String CONFIG_FILE = "config/smartentityrender.properties";
    private static final int CONFIG_VERSION = 10;

    private static Config INSTANCE;

    // إعدادات المسافة والأداء
    private int tracingDistance = 128;
    private int sleepDelay = 10;
    private int hitboxLimit = 50;
    private int captureRate = 5;

    // إعدادات الميزات
    private boolean renderNametagsThroughWalls = true;
    private boolean tickCulling = true;
    private boolean debugMode = false;
    private boolean skipEntityCulling = false;
    private boolean skipBlockEntityCulling = false;
    private boolean blockEntityFrustumCulling = true;
    private boolean strictFrustumCulling = true;
    private boolean entityShadowMobsEnabled = true;
    private boolean entityShadowDropsEnabled = true;
    private boolean forceDisplayCulling = false;
    private boolean disableF3 = false;
    private boolean showFPS = false;
    private boolean cloudCulling = true;
    private boolean signTextCulling = true;
    private boolean weatherFrustumCulling = true;
    private boolean beaconBeamFrustumCulling = true;
    private boolean blockStateCullingAggressive = false;
    private boolean moddedBlockStateCulling = true;
    private final Set<String> blockStateDontCullList = new HashSet<>();
    private boolean itemFrameCustomRenderer = true;
    private boolean itemFrameMapCulling = true;
    private boolean itemFrameLodEnabled = true;
    private int itemFrameLodDistance = 24;
    private int itemFrameMaxDistance = 64;
    private boolean itemFrameThreeFaceCulling = true;
    private int itemFrameThreeFaceDistance = 24;
    private boolean paintingCulling = true;
    private int leavesCullingMode = 0;
    private int leavesCullingAmount = 1;
    private boolean includeMangroveRoots = false;
    private boolean endGatewayCulling = false;
    private boolean powderSnowCulling = true;
    private boolean entityModelCulling = false;
    private boolean endGatewayBeamCulling = true;
    private boolean advancedBlockCulling = true;
    private boolean modelCullshapeOptimization = true;
    private int cloudDistance = 64;

    // Memory optimization system
    private boolean memorySystemEnabled = true;
    private boolean memoryFastMapEnabled = true;
    private boolean memoryCompactBlockStateEnabled = true;
    private boolean memoryMultipartConditionCacheEnabled = true;
    private boolean memoryModelResourceLocationInterningEnabled = true;
    private boolean memoryShapeCacheDedupEnabled = true;
    private boolean memoryQuadDataDedupEnabled = true;
    private boolean memoryThreadingDetectorOptimizedEnabled = true;

    // Density-based culling system - Enhanced for forests and bamboo
    private boolean densityCullingEnabled = true;
    private int densityCellSize = 8;
    private int maxEntitiesPerCell = 20;
    private float highDensityRenderRatio = 0.25f; // تقليل من 0.3 إلى 0.25 للأداء الأفضل
    private float mediumDensityRenderRatio = 0.55f; // تقليل من 0.6 إلى 0.55
    private float lowDensityRenderRatio = 0.8f;

    // Enhanced density culling options
    private boolean smartForestCulling = true; // كشف ذكي للغابات الكثيفة
    private boolean smartBambooCulling = true; // معالجة خاصة للبامبو
    private boolean itemEntityLOD = true; // LOD للأغراض الساقطة
    private boolean aggressiveItemClustering = true; // معالجة عدوانية لتجمعات الأغراض

    // Mob culling options - NEW
    private boolean passiveMobCulling = true; // معالجة الحيوانات الأليفة (دجاج، أبقار، خراف)
    private boolean hostileMobCulling = true; // معالجة الوحوش العدائية (silverfish, slimes)
    private boolean mobFarmOptimization = true; // تحسين خاص لمزارع الحيوانات

    // Camera Zoom Addon
    private boolean cameraZoomEnabled = true;
    private int initialZoomLevel = 200; // 200% zoom (2x)
    private boolean smoothZoomEnabled = true;

    // XP orb merge (performance)
    private boolean mergeXpOrbsEnabled = true;

    // 3D Ladder Model
    private boolean ladder3dModel = false;

    private Config() {
        load();
    }

    public static Config getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Config();
        }
        return INSTANCE;
    }

    /**
     * Load configuration from file
     * تحميل الإعدادات من الملف
     */
    public void load() {
        Path configPath = Paths.get(CONFIG_FILE);

        if (!Files.exists(configPath)) {
            LOGGER.info("Config file not found, creating default configuration");
            save();
            return;
        }

        try (InputStream input = new FileInputStream(configPath.toFile())) {
            Properties props = new Properties();
            props.load(input);

            // التحقق من الإصدار
            int fileVersion = Integer.parseInt(props.getProperty("configVersion", "0"));
            if (fileVersion != CONFIG_VERSION) {
                LOGGER.warn("Config version mismatch. Expected {}, got {}. Using defaults.",
                        CONFIG_VERSION, fileVersion);
                save();
                return;
            }

            // تحميل الإعدادات
            tracingDistance = Integer.parseInt(props.getProperty("tracingDistance", "128"));
            sleepDelay = Integer.parseInt(props.getProperty("sleepDelay", "10"));
            hitboxLimit = Integer.parseInt(props.getProperty("hitboxLimit", "50"));
            captureRate = Integer.parseInt(props.getProperty("captureRate", "5"));

            renderNametagsThroughWalls = Boolean.parseBoolean(
                    props.getProperty("renderNametagsThroughWalls", "true"));
            tickCulling = Boolean.parseBoolean(props.getProperty("tickCulling", "true"));
            debugMode = Boolean.parseBoolean(props.getProperty("debugMode", "false"));
            skipEntityCulling = Boolean.parseBoolean(props.getProperty("skipEntityCulling", "false"));
            skipBlockEntityCulling = Boolean.parseBoolean(
                    props.getProperty("skipBlockEntityCulling", "false"));
            blockEntityFrustumCulling = Boolean.parseBoolean(
                    props.getProperty("blockEntityFrustumCulling", "true"));
            strictFrustumCulling = Boolean.parseBoolean(
                    props.getProperty("strictFrustumCulling", "true"));

            entityShadowMobsEnabled = Boolean.parseBoolean(
                    props.getProperty("entityShadowMobsEnabled", "true"));
            entityShadowDropsEnabled = Boolean.parseBoolean(
                    props.getProperty("entityShadowDropsEnabled", "true"));
            forceDisplayCulling = Boolean.parseBoolean(
                    props.getProperty("forceDisplayCulling", "false"));
            disableF3 = Boolean.parseBoolean(props.getProperty("disableF3", "false"));
            showFPS = Boolean.parseBoolean(props.getProperty("showFPS", "false"));
            cloudCulling = Boolean.parseBoolean(props.getProperty("cloudCulling", "true"));
            signTextCulling = Boolean.parseBoolean(props.getProperty("signTextCulling", "true"));
            weatherFrustumCulling = Boolean.parseBoolean(props.getProperty("weatherFrustumCulling", "true"));
            beaconBeamFrustumCulling = Boolean.parseBoolean(props.getProperty("beaconBeamFrustumCulling", "true"));
            blockStateCullingAggressive = Boolean.parseBoolean(
                    props.getProperty("blockStateCullingAggressive", "false"));
            moddedBlockStateCulling = Boolean.parseBoolean(
                    props.getProperty("moddedBlockStateCulling", "true"));
            blockStateDontCullList.clear();
            blockStateDontCullList.addAll(parseIdList(props.getProperty("blockStateDontCullList", "")));
            itemFrameCustomRenderer = Boolean.parseBoolean(props.getProperty("itemFrameCustomRenderer", "true"));
            itemFrameMapCulling = Boolean.parseBoolean(props.getProperty("itemFrameMapCulling", "true"));
            itemFrameLodEnabled = Boolean.parseBoolean(props.getProperty("itemFrameLodEnabled", "true"));
            itemFrameLodDistance = Integer.parseInt(props.getProperty("itemFrameLodDistance", "24"));
            itemFrameMaxDistance = Integer.parseInt(props.getProperty("itemFrameMaxDistance", "64"));
            itemFrameThreeFaceCulling = Boolean.parseBoolean(
                    props.getProperty("itemFrameThreeFaceCulling", "true"));
            itemFrameThreeFaceDistance = Integer.parseInt(
                    props.getProperty("itemFrameThreeFaceDistance", "24"));
            paintingCulling = Boolean.parseBoolean(props.getProperty("paintingCulling", "true"));
            leavesCullingMode = Integer.parseInt(props.getProperty("leavesCullingMode", "0"));
            leavesCullingAmount = Integer.parseInt(props.getProperty("leavesCullingAmount", "1"));
            includeMangroveRoots = Boolean.parseBoolean(props.getProperty("includeMangroveRoots", "false"));
            endGatewayCulling = Boolean.parseBoolean(props.getProperty("endGatewayCulling", "false"));
            powderSnowCulling = Boolean.parseBoolean(props.getProperty("powderSnowCulling", "true"));
            entityModelCulling = Boolean.parseBoolean(props.getProperty("entityModelCulling", "false"));
            endGatewayBeamCulling = Boolean.parseBoolean(props.getProperty("endGatewayBeamCulling", "true"));
            advancedBlockCulling = Boolean.parseBoolean(props.getProperty("advancedBlockCulling", "true"));
            modelCullshapeOptimization = Boolean.parseBoolean(props.getProperty("modelCullshapeOptimization", "true"));
            cloudDistance = Integer.parseInt(props.getProperty("cloudDistance", "64"));

            memorySystemEnabled = Boolean.parseBoolean(
                    props.getProperty("memorySystemEnabled", "true"));
            memoryFastMapEnabled = Boolean.parseBoolean(
                    props.getProperty("memoryFastMapEnabled", "true"));
            memoryCompactBlockStateEnabled = Boolean.parseBoolean(
                    props.getProperty("memoryCompactBlockStateEnabled", "true"));
            memoryMultipartConditionCacheEnabled = Boolean.parseBoolean(
                    props.getProperty("memoryMultipartConditionCacheEnabled", "true"));
            memoryModelResourceLocationInterningEnabled = Boolean.parseBoolean(
                    props.getProperty("memoryModelResourceLocationInterningEnabled", "true"));
            memoryShapeCacheDedupEnabled = Boolean.parseBoolean(
                    props.getProperty("memoryShapeCacheDedupEnabled", "true"));
            memoryQuadDataDedupEnabled = Boolean.parseBoolean(
                    props.getProperty("memoryQuadDataDedupEnabled", "true"));
            memoryThreadingDetectorOptimizedEnabled = Boolean.parseBoolean(
                    props.getProperty("memoryThreadingDetectorOptimizedEnabled", "true"));

            // Density culling settings
            densityCullingEnabled = Boolean.parseBoolean(
                    props.getProperty("densityCullingEnabled", "true"));
            densityCellSize = Integer.parseInt(
                    props.getProperty("densityCellSize", "8"));
            maxEntitiesPerCell = Integer.parseInt(
                    props.getProperty("maxEntitiesPerCell", "20"));
            highDensityRenderRatio = Float.parseFloat(
                    props.getProperty("highDensityRenderRatio", "0.25"));
            mediumDensityRenderRatio = Float.parseFloat(
                    props.getProperty("mediumDensityRenderRatio", "0.55"));
            lowDensityRenderRatio = Float.parseFloat(
                    props.getProperty("lowDensityRenderRatio", "0.8"));

            // Enhanced density culling
            smartForestCulling = Boolean.parseBoolean(
                    props.getProperty("smartForestCulling", "true"));
            smartBambooCulling = Boolean.parseBoolean(
                    props.getProperty("smartBambooCulling", "true"));
            itemEntityLOD = Boolean.parseBoolean(
                    props.getProperty("itemEntityLOD", "true"));
            aggressiveItemClustering = Boolean.parseBoolean(
                    props.getProperty("aggressiveItemClustering", "true"));

            // Mob culling options
            passiveMobCulling = Boolean.parseBoolean(
                    props.getProperty("passiveMobCulling", "true"));
            hostileMobCulling = Boolean.parseBoolean(
                    props.getProperty("hostileMobCulling", "true"));
            mobFarmOptimization = Boolean.parseBoolean(
                    props.getProperty("mobFarmOptimization", "true"));

            // Camera Zoom Addon
            cameraZoomEnabled = Boolean.parseBoolean(
                    props.getProperty("cameraZoomEnabled", "true"));
            initialZoomLevel = Integer.parseInt(
                    props.getProperty("initialZoomLevel", "200"));
            smoothZoomEnabled = Boolean.parseBoolean(
                    props.getProperty("smoothZoomEnabled", "true"));

            // XP orb merge
            mergeXpOrbsEnabled = Boolean.parseBoolean(
                    props.getProperty("mergeXpOrbsEnabled", "true"));

            // 3D Ladder Model
            ladder3dModel = Boolean.parseBoolean(
                    props.getProperty("ladder3dModel", "false"));

            // no-op

            LOGGER.info("Configuration loaded successfully");
        } catch (IOException | NumberFormatException e) {
            LOGGER.error("Error loading configuration, using defaults", e);
            save();
        }
    }

    /**
     * Save configuration to file
     * حفظ الإعدادات في الملف
     */
    public void save() {
        try {
            Path configPath = Paths.get(CONFIG_FILE);
            Files.createDirectories(configPath.getParent());

            Properties props = new Properties();

            // الإصدار
            props.setProperty("configVersion", String.valueOf(CONFIG_VERSION));

            // إعدادات المسافة والأداء
            props.setProperty("tracingDistance", String.valueOf(tracingDistance));
            props.setProperty("sleepDelay", String.valueOf(sleepDelay));
            props.setProperty("hitboxLimit", String.valueOf(hitboxLimit));
            props.setProperty("captureRate", String.valueOf(captureRate));

            // إعدادات الميزات
            props.setProperty("renderNametagsThroughWalls", String.valueOf(renderNametagsThroughWalls));
            props.setProperty("tickCulling", String.valueOf(tickCulling));
            props.setProperty("debugMode", String.valueOf(debugMode));
            props.setProperty("skipEntityCulling", String.valueOf(skipEntityCulling));
            props.setProperty("skipBlockEntityCulling", String.valueOf(skipBlockEntityCulling));
            props.setProperty("blockEntityFrustumCulling", String.valueOf(blockEntityFrustumCulling));
            props.setProperty("strictFrustumCulling", String.valueOf(strictFrustumCulling));
            props.setProperty("entityShadowMobsEnabled", String.valueOf(entityShadowMobsEnabled));
            props.setProperty("entityShadowDropsEnabled", String.valueOf(entityShadowDropsEnabled));
            props.setProperty("cloudDistance", String.valueOf(cloudDistance));
            props.setProperty("forceDisplayCulling", String.valueOf(forceDisplayCulling));
            props.setProperty("disableF3", String.valueOf(disableF3));
            props.setProperty("showFPS", String.valueOf(showFPS));
            props.setProperty("cloudCulling", String.valueOf(cloudCulling));
            props.setProperty("signTextCulling", String.valueOf(signTextCulling));
            props.setProperty("weatherFrustumCulling", String.valueOf(weatherFrustumCulling));
            props.setProperty("beaconBeamFrustumCulling", String.valueOf(beaconBeamFrustumCulling));
            props.setProperty("blockStateCullingAggressive", String.valueOf(blockStateCullingAggressive));
            props.setProperty("moddedBlockStateCulling", String.valueOf(moddedBlockStateCulling));
            props.setProperty("blockStateDontCullList", String.join(",", sortedList(blockStateDontCullList)));
            props.setProperty("itemFrameCustomRenderer", String.valueOf(itemFrameCustomRenderer));
            props.setProperty("itemFrameMapCulling", String.valueOf(itemFrameMapCulling));
            props.setProperty("itemFrameLodEnabled", String.valueOf(itemFrameLodEnabled));
            props.setProperty("itemFrameLodDistance", String.valueOf(itemFrameLodDistance));
            props.setProperty("itemFrameMaxDistance", String.valueOf(itemFrameMaxDistance));
            props.setProperty("itemFrameThreeFaceCulling", String.valueOf(itemFrameThreeFaceCulling));
            props.setProperty("itemFrameThreeFaceDistance", String.valueOf(itemFrameThreeFaceDistance));
            props.setProperty("paintingCulling", String.valueOf(paintingCulling));
            props.setProperty("leavesCullingMode", String.valueOf(leavesCullingMode));
            props.setProperty("leavesCullingAmount", String.valueOf(leavesCullingAmount));
            props.setProperty("includeMangroveRoots", String.valueOf(includeMangroveRoots));
            props.setProperty("endGatewayCulling", String.valueOf(endGatewayCulling));
            props.setProperty("powderSnowCulling", String.valueOf(powderSnowCulling));
            props.setProperty("entityModelCulling", String.valueOf(entityModelCulling));
            props.setProperty("endGatewayBeamCulling", String.valueOf(endGatewayBeamCulling));
            props.setProperty("advancedBlockCulling", String.valueOf(advancedBlockCulling));
            props.setProperty("modelCullshapeOptimization", String.valueOf(modelCullshapeOptimization));

            props.setProperty("memorySystemEnabled", String.valueOf(memorySystemEnabled));
            props.setProperty("memoryFastMapEnabled", String.valueOf(memoryFastMapEnabled));
            props.setProperty("memoryCompactBlockStateEnabled", String.valueOf(memoryCompactBlockStateEnabled));
            props.setProperty("memoryMultipartConditionCacheEnabled",
                    String.valueOf(memoryMultipartConditionCacheEnabled));
            props.setProperty("memoryModelResourceLocationInterningEnabled",
                    String.valueOf(memoryModelResourceLocationInterningEnabled));
            props.setProperty("memoryShapeCacheDedupEnabled", String.valueOf(memoryShapeCacheDedupEnabled));
            props.setProperty("memoryQuadDataDedupEnabled", String.valueOf(memoryQuadDataDedupEnabled));
            props.setProperty("memoryThreadingDetectorOptimizedEnabled",
                    String.valueOf(memoryThreadingDetectorOptimizedEnabled));

            props.setProperty("densityCullingEnabled", String.valueOf(densityCullingEnabled));
            props.setProperty("densityCellSize", String.valueOf(densityCellSize));
            props.setProperty("maxEntitiesPerCell", String.valueOf(maxEntitiesPerCell));
            props.setProperty("highDensityRenderRatio", String.valueOf(highDensityRenderRatio));
            props.setProperty("mediumDensityRenderRatio", String.valueOf(mediumDensityRenderRatio));
            props.setProperty("lowDensityRenderRatio", String.valueOf(lowDensityRenderRatio));

            // Enhanced density culling
            props.setProperty("smartForestCulling", String.valueOf(smartForestCulling));
            props.setProperty("smartBambooCulling", String.valueOf(smartBambooCulling));
            props.setProperty("itemEntityLOD", String.valueOf(itemEntityLOD));
            props.setProperty("aggressiveItemClustering", String.valueOf(aggressiveItemClustering));

            // Mob culling options
            props.setProperty("passiveMobCulling", String.valueOf(passiveMobCulling));
            props.setProperty("hostileMobCulling", String.valueOf(hostileMobCulling));
            props.setProperty("mobFarmOptimization", String.valueOf(mobFarmOptimization));

            // Camera Zoom Addon
            props.setProperty("cameraZoomEnabled", String.valueOf(cameraZoomEnabled));
            props.setProperty("initialZoomLevel", String.valueOf(initialZoomLevel));
            props.setProperty("smoothZoomEnabled", String.valueOf(smoothZoomEnabled));

            // XP orb merge
            props.setProperty("mergeXpOrbsEnabled", String.valueOf(mergeXpOrbsEnabled));

            // 3D Ladder Model
            props.setProperty("ladder3dModel", String.valueOf(ladder3dModel));

            try (OutputStream output = new FileOutputStream(configPath.toFile())) {
                props.store(output, "SmartEntityRender Configuration v" + CONFIG_VERSION);
            }

            LOGGER.info("Configuration saved successfully");
        } catch (IOException e) {
            LOGGER.error("Error saving configuration", e);
        }
    }

    // Getters
    public int getTracingDistance() {
        return tracingDistance;
    }

    public int getSleepDelay() {
        return sleepDelay;
    }

    public int getHitboxLimit() {
        return hitboxLimit;
    }

    public int getCaptureRate() {
        return captureRate;
    }

    public int getConfigVersion() {
        return CONFIG_VERSION;
    }

    public boolean isRenderNametagsThroughWalls() {
        return renderNametagsThroughWalls;
    }

    public boolean isTickCulling() {
        return tickCulling;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public boolean isSkipEntityCulling() {
        return skipEntityCulling;
    }

    public boolean isSkipBlockEntityCulling() {
        return skipBlockEntityCulling;
    }

    public boolean isBlockEntityFrustumCulling() {
        return blockEntityFrustumCulling;
    }

    public boolean isStrictFrustumCulling() {
        return strictFrustumCulling;
    }

    public boolean isEntityShadowMobsEnabled() {
        return entityShadowMobsEnabled;
    }

    public boolean isEntityShadowDropsEnabled() {
        return entityShadowDropsEnabled;
    }

    public boolean isForceDisplayCulling() {
        return forceDisplayCulling;
    }

    public boolean isDisableF3() {
        return disableF3;
    }

    public boolean isOcclusionCulling() {
        return !skipEntityCulling;
    }

    // Setters
    public void setTracingDistance(int value) {
        this.tracingDistance = value;
        save();
    }

    public void setSleepDelay(int value) {
        this.sleepDelay = value;
        save();
    }

    public void setHitboxLimit(int value) {
        this.hitboxLimit = value;
        save();
    }

    public void setCaptureRate(int value) {
        this.captureRate = value;
        save();
    }

    public void setRenderNametagsThroughWalls(boolean value) {
        this.renderNametagsThroughWalls = value;
        save();
    }

    public void setTickCulling(boolean value) {
        this.tickCulling = value;
        save();
    }

    public void setDebugMode(boolean value) {
        this.debugMode = value;
        save();
    }

    public void setSkipEntityCulling(boolean value) {
        this.skipEntityCulling = value;
        save();
    }

    public void setSkipBlockEntityCulling(boolean value) {
        this.skipBlockEntityCulling = value;
        save();
    }

    public void setBlockEntityFrustumCulling(boolean value) {
        this.blockEntityFrustumCulling = value;
        save();
    }

    public void setStrictFrustumCulling(boolean value) {
        this.strictFrustumCulling = value;
        save();
    }

    public void setEntityShadowMobsEnabled(boolean value) {
        this.entityShadowMobsEnabled = value;
        save();
    }

    public void setEntityShadowDropsEnabled(boolean value) {
        this.entityShadowDropsEnabled = value;
        save();
    }

    public void setForceDisplayCulling(boolean value) {
        this.forceDisplayCulling = value;
        save();
    }

    public void setDisableF3(boolean value) {
        this.disableF3 = value;
        save();
    }

    public boolean isShowFPS() {
        return showFPS;
    }

    public boolean isCloudCulling() {
        return cloudCulling;
    }

    public boolean isSignTextCulling() {
        return signTextCulling;
    }

    public boolean isWeatherFrustumCulling() {
        return weatherFrustumCulling;
    }

    public boolean isBeaconBeamFrustumCulling() {
        return beaconBeamFrustumCulling;
    }

    public boolean isBlockStateCullingAggressive() {
        return blockStateCullingAggressive;
    }

    public boolean isModdedBlockStateCulling() {
        return moddedBlockStateCulling;
    }

    public boolean isBlockStateDontCull(String blockId) {
        if (blockId == null || blockId.isBlank()) {
            return false;
        }
        return blockStateDontCullList.contains(blockId);
    }

    public boolean isItemFrameCustomRenderer() {
        return itemFrameCustomRenderer;
    }

    public boolean isItemFrameMapCulling() {
        return itemFrameMapCulling;
    }

    public boolean isItemFrameLodEnabled() {
        return itemFrameLodEnabled;
    }

    public int getItemFrameLodDistance() {
        return itemFrameLodDistance;
    }

    public int getItemFrameMaxDistance() {
        return itemFrameMaxDistance;
    }

    public boolean isItemFrameThreeFaceCulling() {
        return itemFrameThreeFaceCulling;
    }

    public int getItemFrameThreeFaceDistance() {
        return itemFrameThreeFaceDistance;
    }

    public boolean isPaintingCulling() {
        return paintingCulling;
    }

    public int getLeavesCullingMode() {
        return leavesCullingMode;
    }

    public int getLeavesCullingAmount() {
        return leavesCullingAmount;
    }

    public boolean isIncludeMangroveRoots() {
        return includeMangroveRoots;
    }

    public boolean isEndGatewayCulling() {
        return endGatewayCulling;
    }

    public boolean isPowderSnowCulling() {
        return powderSnowCulling;
    }

    public boolean isEntityModelCulling() {
        return entityModelCulling;
    }

    public boolean isEndGatewayBeamCulling() {
        return endGatewayBeamCulling;
    }

    public boolean isAdvancedBlockCulling() {
        return advancedBlockCulling;
    }

    public boolean isModelCullshapeOptimization() {
        return modelCullshapeOptimization;
    }

    public Set<String> getBlockStateDontCullList() {
        return new HashSet<>(blockStateDontCullList);
    }

    public boolean isMemorySystemEnabled() {
        return memorySystemEnabled;
    }

    public boolean isMemoryFastMapEnabled() {
        return memoryFastMapEnabled;
    }

    public boolean isMemoryCompactBlockStateEnabled() {
        return memoryCompactBlockStateEnabled;
    }

    public boolean isMemoryMultipartConditionCacheEnabled() {
        return memoryMultipartConditionCacheEnabled;
    }

    public boolean isMemoryModelResourceLocationInterningEnabled() {
        return memoryModelResourceLocationInterningEnabled;
    }

    public boolean isMemoryShapeCacheDedupEnabled() {
        return memoryShapeCacheDedupEnabled;
    }

    public boolean isMemoryQuadDataDedupEnabled() {
        return memoryQuadDataDedupEnabled;
    }

    public boolean isMemoryThreadingDetectorOptimizedEnabled() {
        return memoryThreadingDetectorOptimizedEnabled;
    }

    public boolean isDensityCullingEnabled() {
        return densityCullingEnabled;
    }

    public int getDensityCellSize() {
        return densityCellSize;
    }

    public int getMaxEntitiesPerCell() {
        return maxEntitiesPerCell;
    }

    public float getHighDensityRenderRatio() {
        return highDensityRenderRatio;
    }

    public float getMediumDensityRenderRatio() {
        return mediumDensityRenderRatio;
    }

    public float getLowDensityRenderRatio() {
        return lowDensityRenderRatio;
    }

    public boolean isSmartForestCulling() {
        return smartForestCulling;
    }

    public boolean isSmartBambooCulling() {
        return smartBambooCulling;
    }

    public boolean isItemEntityLOD() {
        return itemEntityLOD;
    }

    public boolean isAggressiveItemClustering() {
        return aggressiveItemClustering;
    }

    public boolean isPassiveMobCulling() {
        return passiveMobCulling;
    }

    public boolean isHostileMobCulling() {
        return hostileMobCulling;
    }

    public boolean isMobFarmOptimization() {
        return mobFarmOptimization;
    }

    public void setShowFPS(boolean value) {
        this.showFPS = value;
        save();
    }

    public void setCloudCulling(boolean value) {
        this.cloudCulling = value;
        save();
    }

    public void setSignTextCulling(boolean value) {
        this.signTextCulling = value;
        save();
    }

    public void setWeatherFrustumCulling(boolean value) {
        this.weatherFrustumCulling = value;
        save();
    }

    public void setBeaconBeamFrustumCulling(boolean value) {
        this.beaconBeamFrustumCulling = value;
        save();
    }

    public void setBlockStateCullingAggressive(boolean value) {
        this.blockStateCullingAggressive = value;
        save();
    }

    public void setModdedBlockStateCulling(boolean value) {
        this.moddedBlockStateCulling = value;
        save();
    }

    public void setBlockStateDontCullList(Set<String> values) {
        blockStateDontCullList.clear();
        if (values != null) {
            for (String value : values) {
                if (value != null && !value.isBlank()) {
                    blockStateDontCullList.add(value.trim());
                }
            }
        }
        save();
    }

    public void setItemFrameCustomRenderer(boolean value) {
        this.itemFrameCustomRenderer = value;
        save();
    }

    public void setItemFrameMapCulling(boolean value) {
        this.itemFrameMapCulling = value;
        save();
    }

    public void setItemFrameLodEnabled(boolean value) {
        this.itemFrameLodEnabled = value;
        save();
    }

    public void setItemFrameLodDistance(int value) {
        this.itemFrameLodDistance = Math.max(4, Math.min(128, value));
        if (this.itemFrameLodDistance >= this.itemFrameMaxDistance) {
            this.itemFrameLodDistance = Math.max(4, this.itemFrameMaxDistance - 8);
        }
        save();
    }

    public void setItemFrameMaxDistance(int value) {
        this.itemFrameMaxDistance = Math.max(8, Math.min(256, value));
        if (this.itemFrameLodDistance >= this.itemFrameMaxDistance) {
            this.itemFrameLodDistance = Math.max(4, this.itemFrameMaxDistance - 8);
        }
        save();
    }

    public void setItemFrameThreeFaceCulling(boolean value) {
        this.itemFrameThreeFaceCulling = value;
        save();
    }

    public void setItemFrameThreeFaceDistance(int value) {
        this.itemFrameThreeFaceDistance = Math.max(4, Math.min(128, value));
        if (this.itemFrameThreeFaceDistance >= this.itemFrameMaxDistance) {
            this.itemFrameThreeFaceDistance = Math.max(4, this.itemFrameMaxDistance - 8);
        }
        save();
    }

    public void setPaintingCulling(boolean value) {
        this.paintingCulling = value;
        save();
    }

    public void setLeavesCullingMode(int value) {
        this.leavesCullingMode = Math.max(0, Math.min(3, value));
        save();
    }

    public void setLeavesCullingAmount(int value) {
        this.leavesCullingAmount = Math.max(1, Math.min(4, value));
        save();
    }

    public void setIncludeMangroveRoots(boolean value) {
        this.includeMangroveRoots = value;
        save();
    }

    public void setEndGatewayCulling(boolean value) {
        this.endGatewayCulling = value;
        save();
    }

    public void setPowderSnowCulling(boolean value) {
        this.powderSnowCulling = value;
        save();
    }

    public void setEntityModelCulling(boolean value) {
        this.entityModelCulling = value;
        save();
    }

    public void setEndGatewayBeamCulling(boolean value) {
        this.endGatewayBeamCulling = value;
        save();
    }

    public void setAdvancedBlockCulling(boolean value) {
        this.advancedBlockCulling = value;
        save();
    }

    public void setModelCullshapeOptimization(boolean value) {
        this.modelCullshapeOptimization = value;
        save();
    }

    private static Set<String> parseIdList(String raw) {
        Set<String> out = new HashSet<>();
        if (raw == null || raw.isBlank()) {
            return out;
        }
        String[] parts = raw.split(",");
        for (String part : parts) {
            if (part == null) {
                continue;
            }
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                out.add(trimmed);
            }
        }
        return out;
    }

    private static List<String> sortedList(Set<String> values) {
        List<String> list = new ArrayList<>();
        if (values != null) {
            for (String value : values) {
                if (value != null && !value.isBlank()) {
                    list.add(value.trim());
                }
            }
        }
        Collections.sort(list);
        return list;
    }

    public void setMemorySystemEnabled(boolean value) {
        this.memorySystemEnabled = value;
        if (!value) {
            this.memoryFastMapEnabled = false;
            this.memoryCompactBlockStateEnabled = false;
            this.memoryMultipartConditionCacheEnabled = false;
            this.memoryModelResourceLocationInterningEnabled = false;
            this.memoryShapeCacheDedupEnabled = false;
            this.memoryQuadDataDedupEnabled = false;
            this.memoryThreadingDetectorOptimizedEnabled = false;
        }
        save();
    }

    public void setMemoryFastMapEnabled(boolean value) {
        if (!memorySystemEnabled && value) {
            return;
        }
        this.memoryFastMapEnabled = value;
        save();
    }

    public void setMemoryCompactBlockStateEnabled(boolean value) {
        if (!memorySystemEnabled && value) {
            return;
        }
        this.memoryCompactBlockStateEnabled = value;
        save();
    }

    public void setMemoryMultipartConditionCacheEnabled(boolean value) {
        if (!memorySystemEnabled && value) {
            return;
        }
        this.memoryMultipartConditionCacheEnabled = value;
        save();
    }

    public void setMemoryModelResourceLocationInterningEnabled(boolean value) {
        if (!memorySystemEnabled && value) {
            return;
        }
        this.memoryModelResourceLocationInterningEnabled = value;
        save();
    }

    public void setMemoryShapeCacheDedupEnabled(boolean value) {
        if (!memorySystemEnabled && value) {
            return;
        }
        this.memoryShapeCacheDedupEnabled = value;
        save();
    }

    public void setMemoryQuadDataDedupEnabled(boolean value) {
        if (!memorySystemEnabled && value) {
            return;
        }
        this.memoryQuadDataDedupEnabled = value;
        save();
    }

    public void setMemoryThreadingDetectorOptimizedEnabled(boolean value) {
        if (!memorySystemEnabled && value) {
            return;
        }
        this.memoryThreadingDetectorOptimizedEnabled = value;
        save();
    }

    public void setDensityCullingEnabled(boolean value) {
        this.densityCullingEnabled = value;
        save();
    }

    public void setDensityCellSize(int value) {
        this.densityCellSize = Math.max(4, Math.min(32, value));
        save();
    }

    public void setMaxEntitiesPerCell(int value) {
        this.maxEntitiesPerCell = Math.max(5, Math.min(200, value));
        save();
    }

    public void setHighDensityRenderRatio(float value) {
        this.highDensityRenderRatio = Math.max(0.1f, Math.min(1.0f, value));
        save();
    }

    public void setMediumDensityRenderRatio(float value) {
        this.mediumDensityRenderRatio = Math.max(0.1f, Math.min(1.0f, value));
        save();
    }

    public void setLowDensityRenderRatio(float value) {
        this.lowDensityRenderRatio = Math.max(0.1f, Math.min(1.0f, value));
        save();
    }

    public int getCloudDistance() {
        return cloudDistance;
    }

    public void setCloudDistance(int value) {
        this.cloudDistance = Math.max(2, Math.min(128, value));
        save();
    }

    // Camera Zoom Addon getters and setters
    public boolean isCameraZoomEnabled() {
        return cameraZoomEnabled;
    }

    public void setCameraZoomEnabled(boolean value) {
        this.cameraZoomEnabled = value;
    }

    public int getInitialZoomLevel() {
        return initialZoomLevel;
    }

    public void setInitialZoomLevel(int value) {
        this.initialZoomLevel = Math.max(100, Math.min(500, value));
    }

    public boolean isSmoothZoomEnabled() {
        return smoothZoomEnabled;
    }

    public void setSmoothZoomEnabled(boolean value) {
        this.smoothZoomEnabled = value;
    }

    public boolean isMergeXpOrbsEnabled() {
        return mergeXpOrbsEnabled;
    }

    public void setMergeXpOrbsEnabled(boolean value) {
        this.mergeXpOrbsEnabled = value;
        save();
    }

    public boolean isLadder3dModelEnabled() {
        return ladder3dModel;
    }

    public void setLadder3dModelEnabled(boolean value) {
        this.ladder3dModel = value;
        save();
    }
}
