package com.moigferdsrte.rareice.world.gen.feature;

import com.moigferdsrte.rareice.RareIce;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.placement.RepeatingPlacement;
import org.jetbrains.annotations.NotNull;

public class RareIceCountPlacement extends RepeatingPlacement {
    public static final MapCodec<RareIceCountPlacement> CODEC = MapCodec.unit(RareIceCountPlacement::new);

    protected int count(RandomSource randomSource, BlockPos blockPos) {
        return RareIce.probabilityOfRareIce;
    }

    @Override
    public @NotNull PlacementModifierType<?> type() {
        return RareIce.COUNT_PLACEMENT;
    }
}
