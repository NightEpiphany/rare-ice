package com.moigferdsrte.rareice.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record RareIceCustomPayload(boolean allowInsertingItemsToIce, int probabilityOfRareIce) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RareIceCustomPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("rare-ice", "config_sync"));

    public static final StreamCodec<FriendlyByteBuf, RareIceCustomPayload> CODEC = StreamCodec.of(
            (buf, playLoad) -> {
                buf.writeBoolean(playLoad.allowInsertingItemsToIce);
                buf.writeInt(playLoad.probabilityOfRareIce);
            },
            buf -> new RareIceCustomPayload(buf.readBoolean(), buf.readInt())
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
