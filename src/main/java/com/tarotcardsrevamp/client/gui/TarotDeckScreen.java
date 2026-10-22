package com.tarotcardsrevamp.client.gui;

import com.tarotcardsrevamp.card.TarotCardType;
import com.tarotcardsrevamp.deck.DeckDataComponent;
import com.tarotcardsrevamp.deck.TarotDeckMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class TarotDeckScreen extends AbstractContainerScreen<TarotDeckMenu> {

    public TarotDeckScreen(TarotDeckMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 212, 156);
        this.inventoryLabelX = 26;
        this.inventoryLabelY = 58;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
    }

    @Override
    protected void init() {
        super.init();

        int buttonWidth = 74;
        int buttonHeight = 14;
        int buttonX = this.leftPos + this.imageWidth - buttonWidth - 8;
        int buttonY = this.topPos + 3;

        this.addRenderableWidget(Button.builder(Component.translatable("tarotcardsrevamp.gui.deck.auto_collect"), btn -> {
            if (this.minecraft != null && this.minecraft.gameMode != null) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
            }
        }).bounds(buttonX, buttonY, buttonWidth, buttonHeight).build());
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor extractor, int mouseX, int mouseY) {
        super.extractTooltip(extractor, mouseX, mouseY);

        // Tooltip for empty deck slots showing which card belongs there
        if (this.hoveredSlot != null && !this.hoveredSlot.hasItem() && this.hoveredSlot.index < DeckDataComponent.TOTAL_SLOTS) {
            TarotCardType cardType = TarotCardType.values()[this.hoveredSlot.index];
            Component slotInfo = Component.translatable("tarotcardsrevamp.gui.deck.empty_slot", cardType.getEnglishName())
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
            extractor.setTooltipForNextFrame(this.font, slotInfo, mouseX, mouseY);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        int left = this.leftPos;
        int top = this.topPos;

        // Container main background panel
        extractor.fill(left, top, left + this.imageWidth, top + this.imageHeight, 0xFFC6C6C6);
        // Bevel borders
        extractor.fill(left, top, left + this.imageWidth, top + 1, 0xFFFFFFFF);
        extractor.fill(left, top, left + 1, top + this.imageHeight, 0xFFFFFFFF);
        extractor.fill(left + this.imageWidth - 1, top, left + this.imageWidth, top + this.imageHeight, 0xFF555555);
        extractor.fill(left, top + this.imageHeight - 1, left + this.imageWidth, top + this.imageHeight, 0xFF555555);

        // Render slots recessed boxes
        for (Slot slot : this.menu.slots) {
            int sx = left + slot.x - 1;
            int sy = top + slot.y - 1;

            extractor.fill(sx, sy, sx + 18, sy + 18, 0xFF373737);
            extractor.fill(sx + 1, sy + 1, sx + 18, sy + 18, 0xFFFFFFFF);
            extractor.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF8B8B8B);
        }
    }
}
