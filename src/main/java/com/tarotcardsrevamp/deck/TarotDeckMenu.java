package com.tarotcardsrevamp.deck;

import com.tarotcardsrevamp.card.TarotCardItem;
import com.tarotcardsrevamp.card.TarotCardType;
import com.tarotcardsrevamp.core.ModMenuTypes;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TarotDeckMenu extends AbstractContainerMenu {

    private final Container deckContainer;
    private final ItemStack deckStack;

    public TarotDeckMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ItemStack.EMPTY);
    }

    public TarotDeckMenu(int containerId, Inventory playerInventory, ItemStack deckStack) {
        super(ModMenuTypes.TAROT_DECK_MENU.get(), containerId);
        this.deckStack = deckStack;
        this.deckContainer = new SimpleContainer(DeckDataComponent.TOTAL_SLOTS);

        // 22 dedicated Arcana slots arranged in two rows of 11
        for (int i = 0; i < DeckDataComponent.TOTAL_SLOTS; i++) {
            final int slotIndex = i;
            int x = 8 + (i % 11) * 18;
            int y = 18 + (i / 11) * 20;

            this.addSlot(new Slot(deckContainer, slotIndex, x, y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    if (stack.getItem() instanceof TarotCardItem cardItem) {
                        return cardItem.getCardType().ordinal() == slotIndex;
                    }
                    return false;
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }
            });
        }

        // Player main inventory (3 rows of 9)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 26 + col * 18, 70 + row * 18));
            }
        }

        // Player hotbar (9 slots)
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 26 + col * 18, 128));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < DeckDataComponent.TOTAL_SLOTS) {
                // Moving from deck to player inventory
                if (!this.moveItemStackTo(itemstack1, DeckDataComponent.TOTAL_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Moving from inventory into deck
                if (itemstack1.getItem() instanceof TarotCardItem cardItem) {
                    int targetSlot = cardItem.getCardType().ordinal();
                    if (!this.moveItemStackTo(itemstack1, targetSlot, targetSlot + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }
}
