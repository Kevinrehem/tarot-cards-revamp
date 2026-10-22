package com.tarotcardsrevamp.block.menu;

import com.tarotcardsrevamp.card.TarotCardType;
import com.tarotcardsrevamp.card.UnidentifiedTarotCardItem;
import com.tarotcardsrevamp.core.ModAttachmentTypes;
import com.tarotcardsrevamp.core.ModBlocks;
import com.tarotcardsrevamp.core.ModItems;
import com.tarotcardsrevamp.core.ModMenuTypes;
import com.tarotcardsrevamp.loot.SmartCardSelector;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DivinationMenu extends AbstractContainerMenu {

    private final ContainerLevelAccess access;
    private final Player player;
    private final Container inputContainer = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            DivinationMenu.this.slotsChanged(this);
        }
    };
    private final Container resultContainer = new SimpleContainer(1);

    private static final SmartCardSelector SELECTOR = new SmartCardSelector();

    public DivinationMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    public DivinationMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(ModMenuTypes.DIVINATION_MENU.get(), containerId);
        this.access = access;
        this.player = playerInventory.player;

        // Slot 0: Input Slot (only accepts UnidentifiedTarotCardItem)
        this.addSlot(new Slot(this.inputContainer, 0, 48, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof UnidentifiedTarotCardItem;
            }
        });

        // Slot 1: Result / Revealed Card Slot (take-only slot)
        this.addSlot(new Slot(this.resultContainer, 0, 112, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                super.onTake(player, stack);

                // Consume 1 card from input slot
                ItemStack input = inputContainer.getItem(0);
                if (!input.isEmpty()) {
                    input.shrink(1);
                    inputContainer.setItem(0, input);
                }

                // Sound & particle feedback
                access.execute((level, pos) -> {
                    level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.1F);
                    level.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6F, 1.5F);
                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.ENCHANT,
                                pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                                30, 0.4, 0.3, 0.4, 0.1);
                    }
                });

                // The card was divined! Clear the stored offer so a new card will be generated next
                player.setData(ModAttachmentTypes.DIVINATION_OFFER, -1);

                // Trigger next divination if more cards remain in input
                updateResult();
            }
        });

        // Player Inventory (3 rows of 9)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Player Hotbar (9 slots)
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public void slotsChanged(Container container) {
        if (container == this.inputContainer) {
            updateResult();
        }
    }

    private void updateResult() {
        if (this.player.level().isClientSide()) {
            return;
        }

        ItemStack input = this.inputContainer.getItem(0);
        if (input.isEmpty() || !(input.getItem() instanceof UnidentifiedTarotCardItem)) {
            this.resultContainer.setItem(0, ItemStack.EMPTY);
        } else {
            if (this.resultContainer.getItem(0).isEmpty()) {
                // Check if player already has a persistent divination offer
                int storedOffer = this.player.getData(ModAttachmentTypes.DIVINATION_OFFER);
                TarotCardType chosen;

                if (storedOffer >= 0 && storedOffer < TarotCardType.values().length) {
                    chosen = TarotCardType.values()[storedOffer];
                } else {
                    // Divinate a new card using player card scanning / smart selector
                    var tracker = com.tarotcardsrevamp.loot.PlayerCardScanner.scanPlayerCards(this.player);
                    chosen = SELECTOR.pickCard(tracker);
                    this.player.setData(ModAttachmentTypes.DIVINATION_OFFER, chosen.ordinal());
                }

                ItemStack revealed = new ItemStack(ModItems.getCardItem(chosen));
                this.resultContainer.setItem(0, revealed);
            }
        }
        this.broadcastChanges();
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.DIVINATION_TABLE.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack current = slot.getItem();
            itemstack = current.copy();

            if (index == 1) { // Taking from result slot
                if (!this.moveItemStackTo(current, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(current, itemstack);
            } else if (index == 0) { // Moving from input slot to inventory
                if (!this.moveItemStackTo(current, 2, 38, false)) {
                    return ItemStack.EMPTY;
                }
            } else { // Moving from inventory to table
                if (current.getItem() instanceof UnidentifiedTarotCardItem) {
                    if (!this.moveItemStackTo(current, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 2 && index < 29) { // Main inventory to hotbar
                    if (!this.moveItemStackTo(current, 29, 38, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 29 && index < 38) { // Hotbar to main inventory
                    if (!this.moveItemStackTo(current, 2, 29, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (current.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (current.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, current);
        }

        return itemstack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> {
            this.clearContainer(player, this.inputContainer);
            this.resultContainer.setItem(0, ItemStack.EMPTY);
        });
    }
}
