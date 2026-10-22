package com.tarotcardsrevamp.client.gui;

import com.tarotcardsrevamp.block.menu.DivinationMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class DivinationScreen extends AbstractContainerScreen<DivinationMenu> {

    public DivinationScreen(DivinationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 166);
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 72;
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor extractor, int mouseX, int mouseY) {
        super.extractTooltip(extractor, mouseX, mouseY);

        if (this.hoveredSlot != null && !this.hoveredSlot.hasItem()) {
            if (this.hoveredSlot.index == 0) {
                Component hint = Component.translatable("tarotcardsrevamp.gui.divination.card_hint")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
                extractor.setTooltipForNextFrame(this.font, hint, mouseX, mouseY);
            } else if (this.hoveredSlot.index == 1) {
                Component hint = Component.translatable("tarotcardsrevamp.gui.divination.lapis_hint")
                        .withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC);
                extractor.setTooltipForNextFrame(this.font, hint, mouseX, mouseY);
            }
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        int left = this.leftPos;
        int top = this.topPos;

        // Container main background panel
        extractor.fill(left, top, left + this.imageWidth, top + this.imageHeight, 0xFFC6C6C6);

        // Bevel borders (light top/left, dark bottom/right)
        extractor.fill(left, top, left + this.imageWidth, top + 1, 0xFFFFFFFF);
        extractor.fill(left, top, left + 1, top + this.imageHeight, 0xFFFFFFFF);
        extractor.fill(left + this.imageWidth - 1, top, left + this.imageWidth, top + this.imageHeight, 0xFF555555);
        extractor.fill(left, top + this.imageHeight - 1, left + this.imageWidth, top + this.imageHeight, 0xFF555555);

        // Altar decorative header strip (mystical deep purple & gold)
        extractor.fill(left + 6, top + 16, left + this.imageWidth - 6, top + 20, 0xFF4A148C);
        extractor.fill(left + 6, top + 19, left + this.imageWidth - 6, top + 20, 0xFFFFD700);

        // Render slots recessed boxes
        for (Slot slot : this.menu.slots) {
            int sx = left + slot.x - 1;
            int sy = top + slot.y - 1;

            if (slot.index == 2) { // Result slot highlight (gold border)
                extractor.fill(sx - 1, sy - 1, sx + 19, sy + 19, 0xFFFFD700);
            } else if (slot.index == 1) { // Lapis slot highlight (deep blue border)
                extractor.fill(sx - 1, sy - 1, sx + 19, sy + 19, 0xFF1565C0);
            }

            extractor.fill(sx, sy, sx + 18, sy + 18, 0xFF373737);
            extractor.fill(sx + 1, sy + 1, sx + 18, sy + 18, 0xFFFFFFFF);
            extractor.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF8B8B8B);
        }

        // Draw divination rune / arrow indicator between inputs and output
        int arrowX = left + 86;
        int arrowY = top + 42;
        extractor.fill(arrowX, arrowY - 2, arrowX + 18, arrowY + 4, 0xFF4A148C);
        extractor.fill(arrowX + 2, arrowY - 1, arrowX + 16, arrowY + 3, 0xFFFFD700);
        extractor.fill(arrowX + 16, arrowY - 4, arrowX + 22, arrowY + 6, 0xFFFFD700);
    }
}
