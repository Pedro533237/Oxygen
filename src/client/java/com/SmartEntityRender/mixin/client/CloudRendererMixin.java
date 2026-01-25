package com.SmartEntityRender.mixin.client;

import com.SmartEntityRender.config.Config;
import com.SmartEntityRender.memory.CullshapeCache;
import com.mojang.blaze3d.opengl.GlStateManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.EndGatewayBlock;
import net.minecraft.block.HangingSignBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.MangroveRootsBlock;
import net.minecraft.block.PowderSnowBlock;
import net.minecraft.block.SnowBlock;
import net.minecraft.block.SignBlock;
import net.minecraft.block.WallHangingSignBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.render.CloudRenderer;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.WeatherRendering;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.render.block.entity.AbstractSignBlockEntityRenderer;
import net.minecraft.client.render.block.entity.state.BeaconBlockEntityRenderState;
import net.minecraft.client.render.block.entity.state.SignBlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.ItemFrameEntityRenderer;
import net.minecraft.client.render.entity.state.ItemFrameEntityRenderState;
import net.minecraft.client.render.entity.PaintingEntityRenderer;
import net.minecraft.client.render.entity.state.PaintingEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.render.state.WeatherRenderState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.util.Identifier;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CloudRenderer.class)
public abstract class CloudRendererMixin {
    @Inject(method = "renderClouds(ILnet/minecraft/client/option/CloudRenderMode;FLnet/minecraft/util/math/Vec3d;JF)V", at = @At("HEAD"))
    private void ser$enableCloudCulling(int color, CloudRenderMode mode, float cloudHeight, Vec3d cameraPos,
            long time, float tickDelta, CallbackInfo ci) {
        if (!Config.getInstance().isCloudCulling() || mode == CloudRenderMode.OFF) {
            return;
        }
        GlStateManager._enableCull();
        GL11.glCullFace(GL11.GL_BACK);
        int frontFace = (mode == CloudRenderMode.FAST) ? GL11.GL_CW : GL11.GL_CCW;
        GL11.glFrontFace(frontFace);
    }

    @Inject(method = "renderClouds(ILnet/minecraft/client/option/CloudRenderMode;FLnet/minecraft/util/math/Vec3d;JF)V", at = @At("TAIL"))
    private void ser$disableCloudCulling(int color, CloudRenderMode mode, float cloudHeight, Vec3d cameraPos,
            long time, float tickDelta, CallbackInfo ci) {
        if (!Config.getInstance().isCloudCulling() || mode == CloudRenderMode.OFF) {
            return;
        }
        GL11.glFrontFace(GL11.GL_CCW);
        GlStateManager._disableCull();
    }
}

@Mixin(AbstractSignBlockEntityRenderer.class)
class SignTextCullingMixin {
    @Inject(method = "updateRenderState(Lnet/minecraft/block/entity/SignBlockEntity;Lnet/minecraft/client/render/block/entity/state/SignBlockEntityRenderState;FLnet/minecraft/util/math/Vec3d;Lnet/minecraft/client/render/command/ModelCommandRenderer$CrumblingOverlayCommand;)V", at = @At("TAIL"))
    private void ser$signTextCulling(SignBlockEntity blockEntity,
            SignBlockEntityRenderState renderState,
            float tickDelta,
            Vec3d cameraPos,
            ModelCommandRenderer.CrumblingOverlayCommand overlay,
            CallbackInfo ci) {
        if (!Config.getInstance().isSignTextCulling()) {
            return;
        }

        if (renderState == null || renderState.blockState == null || renderState.pos == null || cameraPos == null) {
            return;
        }

        BlockState state = renderState.blockState;
        Block block = state.getBlock();
        Vec3d front = null;

        if (block instanceof WallSignBlock) {
            Direction facing = state.get(WallSignBlock.FACING);
            front = new Vec3d(facing.getOffsetX(), 0.0, facing.getOffsetZ());
        } else if (block instanceof WallHangingSignBlock) {
            Direction facing = state.get(WallHangingSignBlock.FACING);
            front = new Vec3d(facing.getOffsetX(), 0.0, facing.getOffsetZ());
        } else if (block instanceof SignBlock signBlock) {
            float yaw = signBlock.getRotationDegrees(state);
            double rad = Math.toRadians(yaw);
            front = new Vec3d(-Math.sin(rad), 0.0, Math.cos(rad));
        } else if (block instanceof HangingSignBlock hangingSignBlock) {
            float yaw = hangingSignBlock.getRotationDegrees(state);
            double rad = Math.toRadians(yaw);
            front = new Vec3d(-Math.sin(rad), 0.0, Math.cos(rad));
        }

        if (front == null) {
            return;
        }

        Vec3d center = Vec3d.ofCenter(renderState.pos);
        Vec3d toCamera = cameraPos.subtract(center);
        double dot = front.x * toCamera.x + front.z * toCamera.z;

        if (dot > 0.0001) {
            renderState.backText = new SignText();
        } else if (dot < -0.0001) {
            renderState.frontText = new SignText();
        }
    }
}

@Mixin(WeatherRendering.class)
class RainSnowFrustumCullingMixin {
    @Inject(method = "renderPrecipitation(Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/client/render/state/WeatherRenderState;)V", at = @At("HEAD"))
    private void ser$cullPrecipitation(VertexConsumerProvider provider,
            Vec3d cameraPos,
            WeatherRenderState state,
            CallbackInfo ci) {
        if (!Config.getInstance().isWeatherFrustumCulling()) {
            return;
        }
        if (state == null || cameraPos == null) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.worldRenderer == null) {
            return;
        }

        Frustum captured = client.worldRenderer.getCapturedFrustum();
        if (captured == null) {
            return;
        }

        Frustum frustum = WorldRenderer.offsetFrustum(captured);
        if (frustum == null) {
            return;
        }
        frustum.setPosition(cameraPos.x, cameraPos.y, cameraPos.z);

        if (state.rainPieces != null) {
            state.rainPieces.removeIf(piece -> piece == null || !frustum.isVisible(new Box(
                    piece.x(),
                    piece.bottomY(),
                    piece.z(),
                    piece.x() + 1.0,
                    piece.topY() + 1.0,
                    piece.z() + 1.0)));
        }

        if (state.snowPieces != null) {
            state.snowPieces.removeIf(piece -> piece == null || !frustum.isVisible(new Box(
                    piece.x(),
                    piece.bottomY(),
                    piece.z(),
                    piece.x() + 1.0,
                    piece.topY() + 1.0,
                    piece.z() + 1.0)));
        }
    }
}

@Mixin(BeaconBlockEntityRenderer.class)
class BeaconBeamFrustumCullingMixin {
    @Inject(method = "render(Lnet/minecraft/client/render/block/entity/state/BeaconBlockEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V", at = @At("HEAD"), cancellable = true)
    private void ser$cullBeaconBeam(BeaconBlockEntityRenderState renderState,
            net.minecraft.client.util.math.MatrixStack matrices,
            net.minecraft.client.render.command.OrderedRenderCommandQueue queue,
            CameraRenderState cameraState,
            CallbackInfo ci) {
        if (!Config.getInstance().isBeaconBeamFrustumCulling()) {
            return;
        }
        if (renderState == null || renderState.pos == null || cameraState == null || cameraState.pos == null) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.worldRenderer == null) {
            return;
        }

        Frustum captured = client.worldRenderer.getCapturedFrustum();
        if (captured == null) {
            return;
        }

        Frustum frustum = WorldRenderer.offsetFrustum(captured);
        frustum.setPosition(cameraState.pos.x, cameraState.pos.y, cameraState.pos.z);

        double x = renderState.pos.getX();
        double y = renderState.pos.getY();
        double z = renderState.pos.getZ();
        double height = BeaconBlockEntityRenderer.MAX_BEAM_HEIGHT;
        Box beamBox = new Box(x, y, z, x + 1.0, y + height, z + 1.0);

        if (!frustum.isVisible(beamBox)) {
            ci.cancel();
        }
    }
}

@Mixin(Block.class)
class BlockStateAggressiveCullingMixin {
    @Inject(method = "shouldDrawSide", at = @At("HEAD"), cancellable = true)
    private static void ser$aggressiveBlockStateCulling(BlockState state,
            BlockState neighborState,
            Direction direction,
            CallbackInfoReturnable<Boolean> cir) {
        Config config = Config.getInstance();
        if (state == null || neighborState == null || direction == null) {
            return;
        }

        // لا تخفي البلوكات إذا كان الاتجاه لأعلى (لتجنب إخفاء البلوكات تحت الكيانات مثل
        // الصناديق)
        // Don't cull blocks if direction is UP (to avoid hiding blocks under block
        // entities like chests)
        if (direction == Direction.UP) {
            return;
        }

        // لا تخفي البلوكات إذا كان الجار فوقها block entity أو ثلج
        // Don't cull if neighbor above is a block entity (chest, furnace, etc.) or snow
        Block neighborBlk = neighborState.getBlock();
        if (neighborBlk instanceof net.minecraft.block.BlockWithEntity
                || neighborBlk instanceof net.minecraft.block.ChestBlock
                || neighborBlk instanceof net.minecraft.block.EnderChestBlock
                || neighborBlk instanceof net.minecraft.block.ShulkerBoxBlock
                || neighborBlk instanceof net.minecraft.block.BarrelBlock
                || neighborBlk instanceof net.minecraft.block.FurnaceBlock
                || neighborBlk instanceof net.minecraft.block.BlastFurnaceBlock
                || neighborBlk instanceof net.minecraft.block.SmokerBlock
                || neighborBlk instanceof net.minecraft.block.HopperBlock
                || neighborBlk instanceof net.minecraft.block.DispenserBlock
                || neighborBlk instanceof net.minecraft.block.DropperBlock
                || neighborBlk instanceof net.minecraft.block.BrewingStandBlock
                || neighborBlk instanceof net.minecraft.block.BeaconBlock
                || neighborBlk instanceof net.minecraft.block.EnchantingTableBlock
                || neighborBlk instanceof SnowBlock
                || neighborBlk instanceof PowderSnowBlock) {
            return;
        }

        // فحص إذا كان البلوك المجاور صغيراً (مثل fence، walls، وغيرها)
        // Check if neighbor block is small/partial (like fence, walls, etc.)
        VoxelShape neighborShape = CullshapeCache.getCachedCullshape(neighborState, direction.getOpposite());
        if (neighborShape != VoxelShapes.fullCube() && neighborShape != VoxelShapes.empty()) {
            // البلوك المجاور ليس مكعباً كاملاً - لا تخفي
            // Neighbor is not a full cube - don't cull
            return;
        }

        if (config.isEndGatewayCulling() && neighborState.getBlock() instanceof EndGatewayBlock) {
            VoxelShape face = CullshapeCache.getCachedCullshape(state, direction);
            if (face != VoxelShapes.empty()) {
                cir.setReturnValue(false);
                return;
            }
        }

        if (config.isPowderSnowCulling() && state.getBlock() instanceof PowderSnowBlock) {
            VoxelShape neighborFace = CullshapeCache.getCachedCullshape(neighborState, direction.getOpposite());
            if (neighborFace == VoxelShapes.fullCube()) {
                cir.setReturnValue(false);
                return;
            }
        }

        boolean isLeaves = state.getBlock() instanceof LeavesBlock
                || (config.isIncludeMangroveRoots()
                        && state.getBlock() instanceof MangroveRootsBlock);
        if (isLeaves) {
            Identifier leafId = Registries.BLOCK.getId(state.getBlock());
            String leafIdStr = leafId != null ? leafId.toString() : "";
            if (!config.isBlockStateDontCull(leafIdStr)) {
                int mode = config.getLeavesCullingMode();
                if (mode != 0) {
                    boolean neighborLeaves = neighborState.getBlock() instanceof LeavesBlock
                            || (config.isIncludeMangroveRoots()
                                    && neighborState.getBlock() instanceof MangroveRootsBlock);
                    if (neighborLeaves) {
                        int amount = config.getLeavesCullingAmount();
                        VoxelShape face = CullshapeCache.getCachedCullshape(state, direction);
                        VoxelShape neighborFace = CullshapeCache.getCachedCullshape(neighborState,
                                direction.getOpposite());
                        boolean fullFaces = face == VoxelShapes.fullCube()
                                && neighborFace == VoxelShapes.fullCube();
                        boolean allowCull = (amount >= 4) || (fullFaces && amount >= 2);
                        if (mode == 1 && allowCull) {
                            cir.setReturnValue(false);
                            return;
                        }
                        if (mode == 2 && allowCull && direction.getAxis() == Direction.Axis.Y) {
                            cir.setReturnValue(false);
                            return;
                        }
                        if (mode == 3 && allowCull && direction.getAxis() != Direction.Axis.Y) {
                            cir.setReturnValue(false);
                            return;
                        }
                    }
                }
            }
        }

        Identifier id = Registries.BLOCK.getId(state.getBlock());
        String idStr = id != null ? id.toString() : "";
        if (config.isBlockStateDontCull(idStr)) {
            return;
        }

        if (config.isAdvancedBlockCulling()) {
            Block block = state.getBlock();
            Block neighborBlock = neighborState.getBlock();

            if (block instanceof net.minecraft.block.DoorBlock || block instanceof net.minecraft.block.TrapdoorBlock) {
                VoxelShape face = CullshapeCache.getCachedCullshape(state, direction);
                VoxelShape neighborFace = CullshapeCache.getCachedCullshape(neighborState, direction.getOpposite());
                if (face != VoxelShapes.empty() && neighborFace == VoxelShapes.fullCube()) {
                    cir.setReturnValue(false);
                    return;
                }
            }

            if (block instanceof net.minecraft.block.StairsBlock) {
                VoxelShape neighborFace = CullshapeCache.getCachedCullshape(neighborState, direction.getOpposite());
                if (neighborFace == VoxelShapes.fullCube()) {
                    VoxelShape face = CullshapeCache.getCachedCullshape(state, direction);
                    if (face != VoxelShapes.empty()) {
                        cir.setReturnValue(false);
                        return;
                    }
                }
            }

            if (block instanceof net.minecraft.block.RailBlock
                    || block instanceof net.minecraft.block.AbstractRailBlock) {
                if (direction == Direction.DOWN) {
                    VoxelShape neighborFace = CullshapeCache.getCachedCullshape(neighborState, Direction.UP);
                    if (neighborFace == VoxelShapes.fullCube()) {
                        cir.setReturnValue(false);
                        return;
                    }
                }
            }

            if (block instanceof net.minecraft.block.ShulkerBoxBlock) {
                VoxelShape face = CullshapeCache.getCachedCullshape(state, direction);
                VoxelShape neighborFace = CullshapeCache.getCachedCullshape(neighborState, direction.getOpposite());
                if (face == VoxelShapes.fullCube() && neighborFace == VoxelShapes.fullCube()) {
                    cir.setReturnValue(false);
                    return;
                }
            }

            if (block instanceof net.minecraft.block.FenceBlock || block instanceof net.minecraft.block.FenceGateBlock
                    || block instanceof net.minecraft.block.WallBlock) {
                if (neighborBlock instanceof net.minecraft.block.FenceBlock
                        || neighborBlock instanceof net.minecraft.block.WallBlock) {
                    VoxelShape face = CullshapeCache.getCachedCullshape(state, direction);
                    VoxelShape neighborFace = CullshapeCache.getCachedCullshape(neighborState, direction.getOpposite());
                    if (face != VoxelShapes.empty() && neighborFace != VoxelShapes.empty()) {
                        cir.setReturnValue(false);
                        return;
                    }
                }
            }

            if (block instanceof net.minecraft.block.PaneBlock
                    || block instanceof net.minecraft.block.StainedGlassPaneBlock) {
                if (neighborBlock.getClass() == block.getClass()) {
                    VoxelShape face = CullshapeCache.getCachedCullshape(state, direction);
                    VoxelShape neighborFace = CullshapeCache.getCachedCullshape(neighborState, direction.getOpposite());
                    if (face != VoxelShapes.empty() && neighborFace != VoxelShapes.empty()) {
                        cir.setReturnValue(false);
                        return;
                    }
                }
            }
        }

        if (!config.isBlockStateCullingAggressive()) {
            if (!config.isModdedBlockStateCulling()) {
                return;
            }
            if (id == null || "minecraft".equals(id.getNamespace())) {
                return;
            }
        }

        VoxelShape face = CullshapeCache.getCachedCullshape(state, direction);
        if (face == VoxelShapes.empty()) {
            return;
        }

        VoxelShape neighborFace = CullshapeCache.getCachedCullshape(neighborState, direction.getOpposite());
        if (neighborFace == VoxelShapes.empty()) {
            return;
        }

        cir.setReturnValue(false);
    }
}

@Mixin(ItemFrameEntityRenderer.class)
class ItemFrameCustomRendererMixin {
    @Inject(method = "updateRenderState(Lnet/minecraft/entity/decoration/ItemFrameEntity;Lnet/minecraft/client/render/entity/state/ItemFrameEntityRenderState;F)V", at = @At("TAIL"))
    private void ser$itemFrameLod(net.minecraft.entity.decoration.ItemFrameEntity entity,
            ItemFrameEntityRenderState renderState,
            float tickDelta,
            CallbackInfo ci) {
        Config config = Config.getInstance();
        if (!config.isItemFrameCustomRenderer()) {
            return;
        }
        if (!config.isItemFrameLodEnabled()) {
            return;
        }
        if (renderState == null) {
            return;
        }

        double distSq = renderState.squaredDistanceToCamera;
        int lodDistance = config.getItemFrameLodDistance();
        if (distSq > (double) lodDistance * (double) lodDistance) {
            renderState.itemRenderState.clear();
            renderState.mapId = null;
            renderState.mapRenderState.texture = null;
            renderState.mapRenderState.decorations.clear();
        }
    }

    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/ItemFrameEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V", at = @At("HEAD"), cancellable = true)
    private void ser$itemFrameCulling(ItemFrameEntityRenderState renderState,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            CameraRenderState cameraState,
            CallbackInfo ci) {
        Config config = Config.getInstance();
        if (!config.isItemFrameCustomRenderer()) {
            return;
        }
        if (renderState == null || cameraState == null || cameraState.pos == null) {
            return;
        }

        if (config.isItemFrameLodEnabled()) {
            int maxDistance = config.getItemFrameMaxDistance();
            double distSq = renderState.squaredDistanceToCamera;
            if (distSq > (double) maxDistance * (double) maxDistance) {
                ci.cancel();
                return;
            }
        }

        if (config.isItemFrameThreeFaceCulling()) {
            int start = config.getItemFrameThreeFaceDistance();
            double distSq = renderState.squaredDistanceToCamera;
            if (distSq > (double) start * (double) start && renderState.facing != null) {
                Vec3d cameraPos = cameraState.pos;
                Vec3d center = new Vec3d(renderState.x, renderState.y, renderState.z);
                Vec3d toCamera = cameraPos.subtract(center);
                Vec3d facing = new Vec3d(renderState.facing.getOffsetX(), renderState.facing.getOffsetY(),
                        renderState.facing.getOffsetZ());
                double len = Math.sqrt(toCamera.x * toCamera.x + toCamera.y * toCamera.y + toCamera.z * toCamera.z);
                if (len > 0.0001) {
                    double cos = (facing.x * toCamera.x + facing.y * toCamera.y + facing.z * toCamera.z) / len;
                    if (cos <= 0.2) {
                        ci.cancel();
                        return;
                    }
                }
            }
        }

        if (config.isItemFrameMapCulling() && renderState.mapId != null && renderState.facing != null) {
            Vec3d cameraPos = cameraState.pos;
            Vec3d center = new Vec3d(renderState.x, renderState.y, renderState.z);
            Vec3d toCamera = cameraPos.subtract(center);
            Vec3d facing = new Vec3d(renderState.facing.getOffsetX(), renderState.facing.getOffsetY(),
                    renderState.facing.getOffsetZ());
            double dot = facing.x * toCamera.x + facing.y * toCamera.y + facing.z * toCamera.z;

            boolean hideMap = dot <= 0.0001;
            if (!hideMap) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client != null && client.worldRenderer != null) {
                    Frustum captured = client.worldRenderer.getCapturedFrustum();
                    if (captured != null) {
                        Frustum frustum = WorldRenderer.offsetFrustum(captured);
                        if (frustum != null) {
                            frustum.setPosition(cameraPos.x, cameraPos.y, cameraPos.z);
                            double halfW = Math.max(0.1, renderState.width * 0.5);
                            double halfH = Math.max(0.1, renderState.height * 0.5);
                            Box box = new Box(center.x - halfW, center.y - halfH, center.z - halfW,
                                    center.x + halfW, center.y + halfH, center.z + halfW);
                            hideMap = !frustum.isVisible(box);
                        }
                    }
                }
            }

            if (hideMap && renderState.mapRenderState != null) {
                renderState.mapId = null;
                renderState.mapRenderState.texture = null;
                if (renderState.mapRenderState.decorations != null) {
                    renderState.mapRenderState.decorations.clear();
                }
            }
        }
    }
}

@Mixin(PaintingEntityRenderer.class)
class PaintingCullingMixin {
    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/PaintingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V", at = @At("HEAD"), cancellable = true)
    private void ser$paintingCulling(PaintingEntityRenderState renderState,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            CameraRenderState cameraState,
            CallbackInfo ci) {
        if (!Config.getInstance().isPaintingCulling()) {
            return;
        }
        if (renderState == null || renderState.facing == null || cameraState == null || cameraState.pos == null) {
            return;
        }

        Vec3d cameraPos = cameraState.pos;
        Vec3d center = new Vec3d(renderState.x, renderState.y, renderState.z);
        Vec3d toCamera = cameraPos.subtract(center);
        Vec3d facing = new Vec3d(renderState.facing.getOffsetX(), renderState.facing.getOffsetY(),
                renderState.facing.getOffsetZ());
        double dot = facing.x * toCamera.x + facing.y * toCamera.y + facing.z * toCamera.z;
        if (dot <= 0.0001) {
            ci.cancel();
        }
    }
}

@Mixin(net.minecraft.client.render.block.entity.EndGatewayBlockEntityRenderer.class)
class EndGatewayBeamFrustumCullingMixin {
    @Inject(method = "render(Lnet/minecraft/client/render/block/entity/state/EndGatewayBlockEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V", at = @At("HEAD"), cancellable = true)
    private void ser$cullEndGatewayBeam(
            net.minecraft.client.render.block.entity.state.EndGatewayBlockEntityRenderState renderState,
            MatrixStack matrices,
            net.minecraft.client.render.command.OrderedRenderCommandQueue queue,
            CameraRenderState cameraState,
            CallbackInfo ci) {
        if (!Config.getInstance().isEndGatewayBeamCulling()) {
            return;
        }
        if (renderState == null || renderState.pos == null || cameraState == null || cameraState.pos == null) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.worldRenderer == null) {
            return;
        }

        Frustum captured = client.worldRenderer.getCapturedFrustum();
        if (captured == null) {
            return;
        }

        Frustum frustum = WorldRenderer.offsetFrustum(captured);
        if (frustum == null) {
            return;
        }
        frustum.setPosition(cameraState.pos.x, cameraState.pos.y, cameraState.pos.z);

        double x = renderState.pos.getX();
        double y = renderState.pos.getY();
        double z = renderState.pos.getZ();
        double beamHeight = 256.0;
        Box beamBox = new Box(x + 0.3, y, z + 0.3, x + 0.7, y + beamHeight, z + 0.7);

        if (!frustum.isVisible(beamBox)) {
            ci.cancel();
        }
    }
}
