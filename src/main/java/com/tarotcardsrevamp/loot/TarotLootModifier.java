package com.tarotcardsrevamp.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tarotcardsrevamp.config.TarotConfig;
import com.tarotcardsrevamp.core.ModItems;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Injects Unidentified Tarot Cards into structure chest loot.
 * Probabilities:
 * - 1% chance for 3 cards
 * - 10% chance for 2 cards
 * - 25% chance for 1 card
 * - 64% chance for 0 cards
 */
public class TarotLootModifier extends LootModifier {

    public static final Supplier<MapCodec<TarotLootModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.mapCodec(inst -> codecStart(inst).apply(inst, TarotLootModifier::new))
    );

    public TarotLootModifier(Optional<Holder<LootItemCondition>> condition, int priority) {
        super(condition, priority);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (context == null) return generatedLoot;

        Identifier tableId = context.getQueriedLootTableId();
        if (tableId != null && !tableId.getPath().startsWith("chests/")) {
            // Only inject into chest loot tables
            return generatedLoot;
        }

        double c1 = TarotConfig.CHANCE_1_CARD != null ? TarotConfig.CHANCE_1_CARD.get() : 0.25;
        double c2 = TarotConfig.CHANCE_2_CARDS != null ? TarotConfig.CHANCE_2_CARDS.get() : 0.10;
        double c3 = TarotConfig.CHANCE_3_CARDS != null ? TarotConfig.CHANCE_3_CARDS.get() : 0.01;

        int count = LootProbabilityHelper.calculateCardCount(context.getRandom().nextDouble(), c1, c2, c3);
        if (count > 0) {
            generatedLoot.add(new ItemStack(ModItems.UNIDENTIFIED_TAROT_CARD.get(), count));
        }

        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
