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

    public static final Codec<DeckDataComponent.DeckSlotEntry> DECK_SLOT_ENTRY_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.BOOL.fieldOf("present").forGetter(DeckDataComponent.DeckSlotEntry::present),
                    Codec.BOOL.fieldOf("active").forGetter(DeckDataComponent.DeckSlotEntry::active),
                    Codec.INT.fieldOf("quest_progress").forGetter(DeckDataComponent.DeckSlotEntry::questProgress),
                    Codec.BOOL.fieldOf("ascended").forGetter(DeckDataComponent.DeckSlotEntry::ascended)
            ).apply(instance, DeckDataComponent.DeckSlotEntry::new));

    public static final StreamCodec<ByteBuf, DeckDataComponent.DeckSlotEntry> DECK_SLOT_ENTRY_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, DeckDataComponent.DeckSlotEntry::present,
            ByteBufCodecs.BOOL, DeckDataComponent.DeckSlotEntry::active,
            ByteBufCodecs.VAR_INT, DeckDataComponent.DeckSlotEntry::questProgress,
            ByteBufCodecs.BOOL, DeckDataComponent.DeckSlotEntry::ascended,
            DeckDataComponent.DeckSlotEntry::new
    );

    public static final Codec<DeckDataComponent> DECK_DATA_CODEC = DECK_SLOT_ENTRY_CODEC.listOf().xmap(
            list -> {
                DeckDataComponent.DeckSlotEntry[] arr = new DeckDataComponent.DeckSlotEntry[DeckDataComponent.TOTAL_SLOTS];
                for (int i = 0; i < DeckDataComponent.TOTAL_SLOTS; i++) {
                    arr[i] = (i < list.size() && list.get(i) != null) ? list.get(i) : DeckDataComponent.DeckSlotEntry.EMPTY;
                }
                return new DeckDataComponent(arr);
            },
            comp -> java.util.Arrays.asList(comp.getSlots())
    );

    public static final StreamCodec<ByteBuf, DeckDataComponent> DECK_DATA_STREAM_CODEC = StreamCodec.of(
            (buf, comp) -> {
                for (int i = 0; i < DeckDataComponent.TOTAL_SLOTS; i++) {
                    DECK_SLOT_ENTRY_STREAM_CODEC.encode(buf, comp.getSlot(i));
                }
            },
            buf -> {
                DeckDataComponent.DeckSlotEntry[] arr = new DeckDataComponent.DeckSlotEntry[DeckDataComponent.TOTAL_SLOTS];
                for (int i = 0; i < DeckDataComponent.TOTAL_SLOTS; i++) {
                    arr[i] = DECK_SLOT_ENTRY_STREAM_CODEC.decode(buf);
                }
                return new DeckDataComponent(arr);
            }
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<DeckDataComponent>> DECK_DATA =
            DATA_COMPONENT_TYPES.register("deck_data", () -> DataComponentType.<DeckDataComponent>builder()
                    .persistent(DECK_DATA_CODEC)
                    .networkSynchronized(DECK_DATA_STREAM_CODEC)
                    .build());
}
