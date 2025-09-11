package com.moigferdsrte.rareice.blocks.entities;

import com.moigferdsrte.rareice.ItemLocation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.NonNullList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@Environment(EnvType.CLIENT)
public class RareIceBlockEntityRenderer implements BlockEntityRenderer<RareIceBlockEntity> {

    private final ItemRenderer itemRenderer;

    public RareIceBlockEntityRenderer(BlockEntityRendererProvider.Context dispatcher) {
        this.itemRenderer = dispatcher.getItemRenderer();
    }
    
    @Override
    public void render(RareIceBlockEntity blockEntity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        if (blockEntity.isRemoved()) return;
//        if (locations.isEmpty()) {
//            assert blockEntity.getLevel() != null;
//            RandomSource random = blockEntity.getLevel().random;
//            for (var i = 0; i < contained.size(); i++) {
//                locations.add(new ItemLocation(random.nextDouble() * .85 + .1, random.nextDouble() * .7 + .1, random.nextDouble() * .85 + .1));
//            }
//        }
        NonNullList<ItemStack> contained = blockEntity.getItemsContained();
        List<ItemLocation> locations = blockEntity.getItemsLocations();


        if (locations.size() < contained.size()) {
            return;
        }

        if (blockEntity.getLevel() == null || blockEntity.getLevel().isClientSide()) {
            for (int i = 0; i < contained.size(); i++) {
                ItemStack stack = contained.get(i);
                if (i >= locations.size()) break;
                ItemLocation location = locations.get(i);
                if (!stack.isEmpty()) {
                    matrices.pushPose();

                    matrices.translate(
                            location.x,
                            location.y,
                            location.z
                    );


                    matrices.mulPose(Axis.YP.rotationDegrees(location.yaw * 360.0F));
                    matrices.mulPose(Axis.ZP.rotationDegrees(location.pitch * 360.0F));


                    float scale = 0.53f;
                    matrices.scale(scale, scale, scale);


                    itemRenderer.renderStatic(
                            stack,
                            ItemDisplayContext.FIXED,
                            light,
                            OverlayTexture.NO_OVERLAY,
                            matrices,
                            vertexConsumers,
                            blockEntity.getLevel(),
                            0
                    );

                    matrices.popPose();
                }
            }
        }
    }
}
