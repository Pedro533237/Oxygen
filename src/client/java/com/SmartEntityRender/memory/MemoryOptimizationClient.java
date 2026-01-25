package com.SmartEntityRender.memory;

import com.SmartEntityRender.config.Config;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.MultipartModelCondition;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.state.State;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.WeakHashMap;
import java.io.StringReader;
import java.util.function.Predicate;

public final class MemoryOptimizationClient {
    private static final Identifier LADDER_MODEL_ID = Identifier.of("minecraft", "block/ladder");
    private static final String LADDER_3D_MODEL_JSON = """
            {
            	"credit": "Made with Blockbench",
            	"textures": {
            		"0": "block/ladder",
            		"particle": "block/ladder"
            	},
            	"elements": [
            		{
            			"from": [12, 0, 12],
            			"to": [14, 16, 16],
            			"faces": {
            				"north": {"uv": [12, 0, 14, 16], "texture": "#0"},
            				"east": {"uv": [12, 0, 16, 16], "texture": "#0"},
            				"south": {"uv": [12, 0, 14, 16], "texture": "#0"},
            				"west": {"uv": [12, 0, 16, 16], "texture": "#0"},
            				"up": {"uv": [12, 12, 14, 16], "texture": "#0"},
            				"down": {"uv": [12, 12, 14, 16], "texture": "#0"}
            			}
            		},
            		{
            			"from": [2, 0, 12],
            			"to": [4, 16, 16],
            			"faces": {
            				"north": {"uv": [2, 0, 4, 16], "texture": "#0"},
            				"east": {"uv": [2, 0, 6, 16], "texture": "#0"},
            				"south": {"uv": [2, 0, 4, 16], "texture": "#0"},
            				"west": {"uv": [2, 0, 6, 16], "texture": "#0"},
            				"up": {"uv": [2, 12, 4, 16], "texture": "#0"},
            				"down": {"uv": [2, 12, 4, 16], "texture": "#0"}
            			}
            		},
            		{
            			"from": [4, 13, 14],
            			"to": [12, 15, 16],
            			"faces": {
            				"north": {"uv": [4, 1, 12, 3], "texture": "#0"},
            				"east": {"uv": [4, 1, 6, 3], "texture": "#0"},
            				"south": {"uv": [4, 1, 12, 3], "texture": "#0"},
            				"west": {"uv": [4, 1, 6, 3], "texture": "#0"},
            				"up": {"uv": [4, 14, 12, 16], "texture": "#0"},
            				"down": {"uv": [4, 14, 12, 16], "texture": "#0"}
            			}
            		},
            		{
            			"from": [4, 9, 14],
            			"to": [12, 11, 16],
            			"faces": {
            				"north": {"uv": [4, 5, 12, 7], "texture": "#0"},
            				"east": {"uv": [4, 5, 6, 7], "texture": "#0"},
            				"south": {"uv": [4, 5, 12, 7], "texture": "#0"},
            				"west": {"uv": [4, 5, 6, 7], "texture": "#0"},
            				"up": {"uv": [4, 14, 12, 16], "texture": "#0"},
            				"down": {"uv": [4, 14, 12, 16], "texture": "#0"}
            			}
            		},
            		{
            			"from": [4, 5, 14],
            			"to": [12, 7, 16],
            			"faces": {
            				"north": {"uv": [4, 9, 12, 11], "texture": "#0"},
            				"east": {"uv": [4, 9, 6, 11], "texture": "#0"},
            				"south": {"uv": [4, 9, 12, 11], "texture": "#0"},
            				"west": {"uv": [4, 9, 6, 11], "texture": "#0"},
            				"up": {"uv": [4, 14, 12, 16], "texture": "#0"},
            				"down": {"uv": [4, 14, 12, 16], "texture": "#0"}
            			}
            		},
            		{
            			"from": [4, 1, 14],
            			"to": [12, 3, 16],
            			"faces": {
            				"north": {"uv": [4, 13, 12, 15], "texture": "#0"},
            				"east": {"uv": [4, 13, 6, 15], "texture": "#0"},
            				"south": {"uv": [4, 13, 12, 15], "texture": "#0"},
            				"west": {"uv": [4, 13, 6, 15], "texture": "#0"},
            				"up": {"uv": [4, 14, 12, 16], "texture": "#0"},
            				"down": {"uv": [4, 14, 12, 16], "texture": "#0"}
            			}
            		},
            		{
            			"from": [1, 9, 14],
            			"to": [2, 11, 16],
            			"faces": {
            				"north": {"uv": [1, 5, 2, 7], "texture": "#0"},
            				"east": {"uv": [1, 5, 3, 7], "texture": "#0"},
            				"south": {"uv": [1, 5, 2, 7], "texture": "#0"},
            				"west": {"uv": [1, 5, 3, 7], "texture": "#0"},
            				"up": {"uv": [1, 14, 2, 16], "texture": "#0"},
            				"down": {"uv": [1, 14, 2, 16], "texture": "#0"}
            			}
            		},
            		{
            			"from": [1, 5, 14],
            			"to": [2, 7, 16],
            			"faces": {
            				"north": {"uv": [1, 9, 2, 11], "texture": "#0"},
            				"east": {"uv": [1, 9, 3, 11], "texture": "#0"},
            				"south": {"uv": [1, 9, 2, 11], "texture": "#0"},
            				"west": {"uv": [1, 9, 3, 11], "texture": "#0"},
            				"up": {"uv": [1, 14, 2, 16], "texture": "#0"},
            				"down": {"uv": [1, 14, 2, 16], "texture": "#0"}
            			}
            		},
            		{
            			"from": [1, 1, 14],
            			"to": [2, 3, 16],
            			"faces": {
            				"north": {"uv": [1, 13, 2, 15], "texture": "#0"},
            				"east": {"uv": [1, 13, 3, 15], "texture": "#0"},
            				"south": {"uv": [1, 13, 2, 15], "texture": "#0"},
            				"west": {"uv": [1, 13, 3, 15], "texture": "#0"},
            				"up": {"uv": [1, 14, 2, 16], "texture": "#0"},
            				"down": {"uv": [1, 14, 2, 16], "texture": "#0"}
            			}
            		},
            		{
            			"from": [14, 1, 14],
            			"to": [15, 3, 16],
            			"faces": {
            				"north": {"uv": [14, 13, 15, 15], "texture": "#0"},
            				"east": {"uv": [14, 13, 16, 15], "texture": "#0"},
            				"south": {"uv": [14, 13, 15, 15], "texture": "#0"},
            				"west": {"uv": [14, 13, 16, 15], "texture": "#0"},
            				"up": {"uv": [14, 14, 15, 16], "texture": "#0"},
            				"down": {"uv": [14, 14, 15, 16], "texture": "#0"}
            			}
            		},
            		{
            			"from": [14, 5, 14],
            			"to": [15, 7, 16],
            			"faces": {
            				"north": {"uv": [14, 9, 15, 11], "texture": "#0"},
            				"east": {"uv": [14, 9, 16, 11], "texture": "#0"},
            				"south": {"uv": [14, 9, 15, 11], "texture": "#0"},
            				"west": {"uv": [14, 9, 16, 11], "texture": "#0"},
            				"up": {"uv": [14, 14, 15, 16], "texture": "#0"},
            				"down": {"uv": [14, 14, 15, 16], "texture": "#0"}
            			}
            		},
            		{
            			"from": [14, 9, 14],
            			"to": [15, 11, 16],
            			"faces": {
            				"north": {"uv": [14, 5, 15, 7], "texture": "#0"},
            				"east": {"uv": [14, 5, 16, 7], "texture": "#0"},
            				"south": {"uv": [14, 5, 15, 7], "texture": "#0"},
            				"west": {"uv": [14, 5, 16, 7], "texture": "#0"},
            				"up": {"uv": [14, 14, 15, 16], "texture": "#0"},
            				"down": {"uv": [14, 14, 15, 16], "texture": "#0"}
            			}
            		},
            		{
            			"from": [14, 13, 14],
            			"to": [15, 15, 16],
            			"faces": {
            				"north": {"uv": [14, 1, 15, 3], "texture": "#0"},
            				"east": {"uv": [14, 1, 16, 3], "texture": "#0"},
            				"south": {"uv": [14, 1, 15, 3], "texture": "#0"},
            				"west": {"uv": [14, 1, 16, 3], "texture": "#0"},
            				"up": {"uv": [14, 14, 15, 16], "texture": "#0"},
            				"down": {"uv": [14, 14, 15, 16], "texture": "#0"}
            			}
            		},
            		{
            			"from": [1, 13, 14],
            			"to": [2, 15, 16],
            			"faces": {
            				"north": {"uv": [1, 1, 2, 3], "texture": "#0"},
            				"east": {"uv": [1, 1, 3, 3], "texture": "#0"},
            				"south": {"uv": [1, 1, 2, 3], "texture": "#0"},
            				"west": {"uv": [1, 1, 3, 3], "texture": "#0"},
            				"up": {"uv": [1, 14, 2, 16], "texture": "#0"},
            				"down": {"uv": [1, 14, 2, 16], "texture": "#0"}
            			}
            		}
            	]
            }
            """;
    private static final Map<Identifier, Identifier> MODEL_ID_CACHE = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<MultipartConditionKey, Predicate<?>> MULTIPART_CONDITION_CACHE = Collections
            .synchronizedMap(new WeakHashMap<>());
    private static final Map<BakedQuad, BakedQuad> QUAD_CACHE = Collections.synchronizedMap(new WeakHashMap<>());

    private MemoryOptimizationClient() {
    }

    private static boolean isSystemEnabled() {
        return Config.getInstance().isMemorySystemEnabled();
    }

    public static Identifier internModelId(Identifier id) {
        if (!isSystemEnabled() || !Config.getInstance().isMemoryModelResourceLocationInterningEnabled() || id == null) {
            return id;
        }
        synchronized (MODEL_ID_CACHE) {
            Identifier cached = MODEL_ID_CACHE.get(id);
            if (cached != null) {
                return cached;
            }
            MODEL_ID_CACHE.put(id, id);
            return id;
        }
    }

    public static <O, S extends State<O, S>> Predicate<S> getCachedMultipartPredicate(
            Optional<MultipartModelCondition> selector,
            StateManager<O, S> manager) {
        if (!isSystemEnabled() || !Config.getInstance().isMemoryMultipartConditionCacheEnabled()) {
            return null;
        }
        MultipartConditionKey key = new MultipartConditionKey(selector.orElse(null), manager);
        synchronized (MULTIPART_CONDITION_CACHE) {
            @SuppressWarnings("unchecked")
            Predicate<S> cached = (Predicate<S>) MULTIPART_CONDITION_CACHE.get(key);
            return cached;
        }
    }

    public static <O, S extends State<O, S>> void cacheMultipartPredicate(Optional<MultipartModelCondition> selector,
            StateManager<O, S> manager, Predicate<S> predicate) {
        if (!isSystemEnabled() || !Config.getInstance().isMemoryMultipartConditionCacheEnabled() || predicate == null) {
            return;
        }
        MultipartConditionKey key = new MultipartConditionKey(selector.orElse(null), manager);
        synchronized (MULTIPART_CONDITION_CACHE) {
            MULTIPART_CONDITION_CACHE.putIfAbsent(key, predicate);
        }
    }

    public static BakedQuad internBakedQuad(BakedQuad quad) {
        if (!isSystemEnabled() || !Config.getInstance().isMemoryQuadDataDedupEnabled() || quad == null) {
            return quad;
        }
        synchronized (QUAD_CACHE) {
            BakedQuad cached = QUAD_CACHE.get(quad);
            if (cached != null) {
                return cached;
            }
            QUAD_CACHE.put(quad, quad);
            return quad;
        }
    }

    private static final class MultipartConditionKey {
        private final MultipartModelCondition condition;
        private final StateManager<?, ?> manager;

        private MultipartConditionKey(MultipartModelCondition condition, StateManager<?, ?> manager) {
            this.condition = condition;
            this.manager = manager;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof MultipartConditionKey that)) {
                return false;
            }
            return manager == that.manager && Objects.equals(condition, that.condition);
        }

        @Override
        public int hashCode() {
            return Objects.hash(System.identityHashCode(manager), condition);
        }
    }

    public static void registerModelOverrides() {
        ModelLoadingPlugin.register(pluginContext -> {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger("SmartEntityRender");
            logger.info("ModelLoadingPlugin callback triggered - registering ladder override");

            pluginContext.modifyModelOnLoad().register(
                    ModelModifier.OVERRIDE_PHASE,
                    (model, context) -> {
                        // Check if this is the ladder model
                        if (LADDER_MODEL_ID.equals(context.id())) {
                            logger.info("Found ladder model at: {}", context.id());

                            // Check if 3D ladder is enabled
                            boolean enabled = Config.getInstance().isLadder3dModelEnabled();
                            logger.info("3D Ladder enabled in config: {}", enabled);

                            if (!enabled) {
                                logger.info("3D Ladder disabled, using vanilla model");
                                return model;
                            }

                            try {
                                logger.info("Deserializing 3D ladder model...");
                                UnbakedModel ladder = JsonUnbakedModel
                                        .deserialize(new StringReader(LADDER_3D_MODEL_JSON));
                                if (ladder != null) {
                                    logger.info("✓ Successfully loaded 3D ladder model!");
                                    return ladder;
                                } else {
                                    logger.warn("Deserialization returned null, using vanilla model");
                                    return model;
                                }
                            } catch (Exception e) {
                                logger.error("Failed to deserialize 3D ladder model", e);
                                return model;
                            }
                        }
                        return model;
                    });
        });
    }
}
