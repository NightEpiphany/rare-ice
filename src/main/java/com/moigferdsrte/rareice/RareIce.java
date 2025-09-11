package com.moigferdsrte.rareice;

import com.moigferdsrte.rareice.blocks.RareIceBlock;
import com.moigferdsrte.rareice.blocks.entities.RareIceBlockEntity;
import com.moigferdsrte.rareice.network.RareIceCustomPayload;
import com.moigferdsrte.rareice.world.gen.feature.RareIceConfig;
import com.moigferdsrte.rareice.world.gen.feature.RareIceCountPlacement;
import com.moigferdsrte.rareice.world.gen.feature.RareIceFeature;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

public class RareIce implements ModInitializer {

    public static final Block RARE_ICE_BLOCK = new RareIceBlock(FabricBlockSettings.copyOf(Blocks.ICE).isValidSpawn((state, world, pos, type) -> type == EntityType.POLAR_BEAR));
    public static final BlockEntityType<RareIceBlockEntity> RARE_ICE_BLOCK_ENTITY_TYPE = FabricBlockEntityTypeBuilder.create(RareIceBlockEntity::new, RARE_ICE_BLOCK).build(null);
    public static final Feature<RareIceConfig> RARE_ICE_FEATURE = new RareIceFeature(RareIceConfig.CODEC);
    public static final PlacementModifierType<RareIceCountPlacement> COUNT_PLACEMENT = register("count_placement", RareIceCountPlacement.CODEC);
    private static final Minecraft mc = Minecraft.getInstance();
    public static boolean allowInsertingItemsToIce = true;
    public static int probabilityOfRareIce = 3;

    private static void loadConfig(Path file) {
        allowInsertingItemsToIce = true;
        probabilityOfRareIce = 3;

        if (Files.exists(file)) {
            try {
                Properties properties = new Properties();
                properties.load(Files.newBufferedReader(file));
                allowInsertingItemsToIce = properties.getProperty("allowInsertingItemsToIce", "true").equals("true");
                probabilityOfRareIce = Integer.parseInt(properties.getProperty("probabilityOfRareIce", "3"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        saveConfig(file);
    }

    private static void saveConfig(Path file) {
        try {
            Files.createDirectories(file.getParent());
            Properties properties = new Properties();
            properties.setProperty("allowInsertingItemsToIce", String.valueOf(allowInsertingItemsToIce));
            properties.setProperty("probabilityOfRareIce", String.valueOf(probabilityOfRareIce));
            properties.store(Files.newBufferedWriter(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE), "Rare Ice Configuration");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInitialize() {
        loadConfig(FabricLoader.getInstance().getConfigDir().resolve("rare-ice.properties"));
        Registry.register(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, ResourceLocation.fromNamespaceAndPath("rare-ice", "rare_ice_count"), COUNT_PLACEMENT);
        Registry.register(BuiltInRegistries.FEATURE, ResourceLocation.fromNamespaceAndPath("rare-ice", "rare_ice"), RARE_ICE_FEATURE);
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("rare-ice", "rare_ice"), RARE_ICE_BLOCK_ENTITY_TYPE);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath("rare-ice", "rare_ice"), RARE_ICE_BLOCK);
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!allowInsertingItemsToIce) return InteractionResult.PASS;
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);
            if (player == null || player.isShiftKeyDown())
                return InteractionResult.PASS;


            if ((state.getBlock() == Blocks.ICE || state.getBlock() == RareIce.RARE_ICE_BLOCK)) {
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity == null) {
                    world.setBlockAndUpdate(pos, RareIce.RARE_ICE_BLOCK.defaultBlockState());
                    blockEntity = world.getBlockEntity(pos);
                }
                if (blockEntity instanceof RareIceBlockEntity rareIceBlockEntity) {
                    ItemStack itemStack = player.getItemInHand(hand);
                    itemStack = player.isCreative() ? itemStack.copy() : itemStack;
                    return rareIceBlockEntity.addItem(world, itemStack, player, true);
                }
            }
            return InteractionResult.PASS;
        });
        PayloadTypeRegistry.playC2S().register(RareIceCustomPayload.TYPE, RareIceCustomPayload.CODEC);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (mc.player != null)
                ClientPlayNetworking.send(new RareIceCustomPayload(allowInsertingItemsToIce, probabilityOfRareIce));
        });
        ServerPlayNetworking.registerGlobalReceiver(RareIceCustomPayload.TYPE, (payLoad, ctx) -> {
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeBoolean(payLoad.allowInsertingItemsToIce());
            buf.writeInt(payLoad.probabilityOfRareIce());
        });
        BiomeModifications.addFeature(ctx -> ctx.getBiome().getBaseTemperature() < 0.15F, GenerationStep.Decoration.UNDERGROUND_ORES,
                ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath("rare-ice", "rare_ice")));
    }

    private static <P extends PlacementModifier> PlacementModifierType<P> register(String string, MapCodec<P> mapCodec) {
        return Registry.register(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, string, () -> mapCodec);
    }
}