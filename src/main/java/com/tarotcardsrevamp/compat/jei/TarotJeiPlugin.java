package com.tarotcardsrevamp.compat.jei;

import com.tarotcardsrevamp.TarotCardsRevampMod;
import com.tarotcardsrevamp.card.TarotCardType;
import com.tarotcardsrevamp.core.ModBlocks;
import com.tarotcardsrevamp.core.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class TarotJeiPlugin implements IModPlugin {

    public static final Identifier PLUGIN_UID = Identifier.fromNamespaceAndPath(TarotCardsRevampMod.MOD_ID, "jei_plugin");

    @Override
    public Identifier getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(DivinationRecipeCategory.RECIPE_TYPE, ModBlocks.DIVINATION_TABLE.get());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new DivinationRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // Register Divination Table recipes (1 Unidentified Tarot Card + 4 Lapis -> Any of the 22 Tarot Cards)
        List<DivinationRecipe> recipes = new ArrayList<>();
        for (TarotCardType type : TarotCardType.values()) {
            recipes.add(new DivinationRecipe(type));
        }
        registration.addRecipes(DivinationRecipeCategory.RECIPE_TYPE, recipes);

        // Information tabs
        registration.addIngredientInfo(
                new ItemStack(ModItems.UNIDENTIFIED_TAROT_CARD.get()),
                VanillaTypes.ITEM_STACK,
                Component.translatable("tarotcardsrevamp.jei.info.unidentified_card")
        );

        registration.addIngredientInfo(
                new ItemStack(ModItems.TAROT_DECK.get()),
                VanillaTypes.ITEM_STACK,
                Component.translatable("tarotcardsrevamp.jei.info.tarot_deck")
        );

        registration.addIngredientInfo(
                new ItemStack(ModBlocks.DIVINATION_TABLE.get()),
                VanillaTypes.ITEM_STACK,
                Component.translatable("tarotcardsrevamp.jei.info.divination_table")
        );
    }
}
