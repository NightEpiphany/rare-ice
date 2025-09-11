package com.moigferdsrte.rareice.blocks.entities;

import com.moigferdsrte.rareice.ItemLocation;
import com.moigferdsrte.rareice.RareIce;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RareIceBlockEntity extends RandomizableContainerBlockEntity implements Clearable {
    private static final RandomSource RANDOM = RandomSource.create();
    private static final ResourceKey<LootTable> LOOT_TABLE = ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse("rare-ice:chests/rare_ice"));
    private NonNullList<ItemStack> itemsContained;
    private final List<ItemLocation> itemsLocations;
    private boolean setup = false;
    private int delay = 0;

    public RareIceBlockEntity(BlockPos pos, BlockState state) {
        super(RareIce.RARE_ICE_BLOCK_ENTITY_TYPE, pos, state);
        this.itemsContained = NonNullList.create();
        this.itemsLocations = new ArrayList<>();
    }

    @Override
    public void clearContent() {
        this.itemsContained.clear();
        this.itemsLocations.clear();
    }

    @Override
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return null;
    }


    public NonNullList<ItemStack> getItemsContained() {
        return itemsContained;
    }

    public List<ItemLocation> getItemsLocations() {
        return itemsLocations;
    }



    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return this.saveWithoutMetadata(provider);
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);
        if (!this.trySaveLootTable(compoundTag)) {
            ContainerHelper.saveAllItems(compoundTag, this.itemsContained, provider);
        }
        for (int i = 0; i < this.itemsContained.size(); i++) {
            this.getItemsLocations().get(i).toTag(compoundTag, i);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.loadAdditional(compoundTag, provider);
        this.itemsContained = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(compoundTag)) {
            ContainerHelper.loadAllItems(compoundTag, this.itemsContained, provider);
        }
        for (int i = 0; i < this.itemsContained.size(); i++) {
            this.getItemsLocations().add(ItemLocation.fromTag(compoundTag, i));
        }
    }

    @Override
    protected @NotNull Component getDefaultName() {
        return Component.translatable("block.minecraft.ice");
    }

    @Override
    protected @NotNull NonNullList<ItemStack> getItems() {
        return this.itemsContained;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> nonNullList) {
    }


    @Override
    public void setLootTable(ResourceKey<LootTable> resourceKey, long l) {
        super.setLootTable(resourceKey, l);
    }

    public void addLootTable(Level world) {
        setup = true;
    }

    public static void tick(Level world, BlockPos pos, BlockState blockState, RareIceBlockEntity blockEntity) {
        if (blockEntity.setup) {
            blockEntity.setup = false;
            blockEntity.delay = 0;
            LootTable lootTable = Objects.requireNonNull(world.getServer()).reloadableRegistries().getLootTable(LOOT_TABLE);
            LootParams.Builder builder = new LootParams.Builder((ServerLevel) world);
            List<ItemStack> drops = lootTable.getRandomItems(builder.create(LootContextParamSets.EMPTY));
            int size = Mth.clamp(world.random.nextInt(5) - (world.random.nextInt(1) + 2), 0, drops.size());
            if (!drops.isEmpty()) {
                for (int i = 0; i < size; i++) {
                    int index = world.random.nextInt(drops.size());
                    blockEntity.addItem(world, drops.get(index), null);
                    drops.remove(index);
                }
            }
        } else if (blockEntity.itemsContained.isEmpty()) {
            blockEntity.delay++;
            if (blockEntity.delay > 20) {
                world.setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState());
                blockEntity.setRemoved();
            }
        } else {
            blockEntity.delay = 0;
        }
    }

    public void addItem(Level world, ItemStack itemStack, Player nullablePlayer) {
        addItem(world, itemStack, nullablePlayer, true);
    }

    public InteractionResult addItem(Level world, ItemStack itemStack, Player nullablePlayer, boolean actuallyDoIt) {

        if (itemStack.isEmpty() || itemStack.getCount() < 1) {
            return InteractionResult.PASS;
        }

        if (itemStack.getItem() instanceof BlockItem) {
            if (((BlockItem) itemStack.getItem()).getBlock().builtInRegistryHolder().is(BlockTags.ICE))
                return InteractionResult.PASS;
        }
        if (getItemsContained().size() < 8 && itemStack.getCount() >= 1) {
            if (actuallyDoIt) {
                ItemStack copy = itemStack.copyWithCount(1);
                getItemsContained().add(copy);
                itemStack.shrink(1);
                ItemLocation itemLocation = new ItemLocation(
                        RANDOM.nextDouble() * .85 + .1,
                        RANDOM.nextDouble() * .7 + .1,
                        RANDOM.nextDouble() * .85 + .1,
                        (float) (Math.random() * 360.0F),
                        (float) (Math.random() * 360.0F));
                getItemsLocations().add(itemLocation);
                updateListeners();
            }
            if (nullablePlayer != null && world.isClientSide())
                nullablePlayer.playSound(SoundEvents.CORAL_BLOCK_BREAK, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    private void updateListeners() {
        this.setChanged();
        assert this.getLevel() != null;
        this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    @Override
    public int getContainerSize() {
        return 8;
    }
}