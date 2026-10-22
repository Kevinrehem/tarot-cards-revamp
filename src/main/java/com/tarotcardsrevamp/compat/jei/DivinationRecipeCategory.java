package com.tarotcardsrevamp.compat.jei;

import com.tarotcardsrevamp.TarotCardsRevampMod;
import com.tarotcardsrevamp.core.ModBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class DivinationRecipeCategory implements IRecipeCategory<DivinationRecipe> {

    public static final IRecipeType<DivinationRecipe> RECIPE_TYPE =
            IRecipeType.create(TarotCardsRevampMod.MOD_ID, "divination", DivinationRecipe.class);

    private final IDrawable icon;

    public DivinationRecipeCategory(IGuiHelper helper) {
        this.icon = helper.createDrawableItemStack(new ItemStack(ModBlocks.DIVINATION_TABLE.get()));
    }

    @Override
    public IRecipeType<DivinationRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.tarotcardsrevamp.divination_table");
    }

    @Override
    public int getWidth() {
        return 120;
    }

    @Override
    public int getHeight() {
        return 36;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DivinationRecipe recipe, IFocusGroup focuses) {
        // Card input slot
        builder.addInputSlot(8, 9)
                .setStandardSlotBackground()
                .add(recipe.getInputCard());

        // Lapis input slot
        builder.addInputSlot(32, 9)
                .setStandardSlotBackground()
                .add(recipe.getLapisCost());

        // Revealed card output slot
        builder.addOutputSlot(88, 9)
                .setOutputSlotBackground()
                .add(recipe.getOutputCard());
    }
}
