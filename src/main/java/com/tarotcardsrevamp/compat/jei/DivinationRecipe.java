package com.tarotcardsrevamp.compat.jei;

import com.tarotcardsrevamp.card.TarotCardType;
import com.tarotcardsrevamp.core.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public record DivinationRecipe(TarotCardType cardType) {

    public ItemStack getInputCard() {
        return new ItemStack(ModItems.UNIDENTIFIED_TAROT_CARD.get());
    }

    public ItemStack getLapisCost() {
        return new ItemStack(Items.LAPIS_LAZULI, 4);
    }

    public ItemStack getOutputCard() {
        return new ItemStack(ModItems.getCardItem(cardType));
    }
}
