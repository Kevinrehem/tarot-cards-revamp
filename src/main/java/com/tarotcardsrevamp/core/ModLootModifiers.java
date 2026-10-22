package com.tarotcardsrevamp.core;

import com.mojang.serialization.MapCodec;
import com.tarotcardsrevamp.TarotCardsRevampMod;
import com.tarotcardsrevamp.loot.TarotLootModifier;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModLootModifiers {

    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, TarotCardsRevampMod.MOD_ID);

    public static final Supplier<MapCodec<TarotLootModifier>> CHEST_UNIDENTIFIED_TAROT_CARDS =
            GLOBAL_LOOT_MODIFIER_SERIALIZERS.register("chest_unidentified_tarot_cards", () -> TarotLootModifier.CODEC.get());
}
