package com.tarotcardsrevamp.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record DoubleJumpPayload() implements CustomPacketPayload {

    public static final Type<DoubleJumpPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("tarotcardsrevamp", "double_jump"));
    public static final StreamCodec<ByteBuf, DoubleJumpPayload> STREAM_CODEC = StreamCodec.unit(new DoubleJumpPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
