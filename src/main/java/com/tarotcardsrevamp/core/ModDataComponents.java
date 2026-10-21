package com.tarotcardsrevamp.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tarotcardsrevamp.card.CardDataComponent;
import com.tarotcardsrevamp.deck.DeckDataComponent;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

public class ModDataComponents {

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, "tarotcardsrevamp");

    public static final Codec<CardDataComponent> CARD_DATA_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.BOOL.fieldOf("active").forGetter(CardDataComponent::active),
                    Codec.INT.fieldOf("quest_progress").forGetter(CardDataComponent::questProgress),
                    Codec.BOOL.fieldOf("ascended").forGetter(CardDataComponent::ascended)
            ).apply(instance, CardDataComponent::new));

    public static final StreamCodec<ByteBuf, CardDataComponent> CARD_DATA_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, CardDataComponent::active,
            ByteBufCodecs.VAR_INT, CardDataComponent::questProgress,
            ByteBufCodecs.BOOL, CardDataComponent::ascended,
            CardDataComponent::new
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CardDataComponent>> CARD_DATA =
            DATA_COMPONENT_TYPES.register("card_data", () -> DataComponentType.<CardDataComponent>builder()
                    .persistent(CARD_DATA_CODEC)
                    .networkSynchronized(CARD_DATA_STREAM_CODEC)
                    .build());
}
