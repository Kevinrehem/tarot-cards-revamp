package com.tarotcardsrevamp.deck;

import com.tarotcardsrevamp.card.CardDataComponent;
import com.tarotcardsrevamp.card.TarotCardItem;
import com.tarotcardsrevamp.core.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class TarotDeckItem extends Item {

    public TarotDeckItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static DeckDataComponent getData(ItemStack stack) {
        DeckDataComponent data = stack.get(ModDataComponents.DECK_DATA.get());
        return data != null ? data : DeckDataComponent.DEFAULT;
    }

    public static void setData(ItemStack stack, DeckDataComponent data) {
        stack.set(ModDataComponents.DECK_DATA.get(), data);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack thisStack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (action == ClickAction.SECONDARY && slot.allowModification(player)) {
            if (other.getItem() instanceof TarotCardItem) {
                return insertCardIntoDeck(thisStack, other, player);
            }
        }
        return super.overrideOtherStackedOnMe(thisStack, other, slot, action, player, access);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack thisStack, Slot slot, ClickAction action, Player player) {
        if (action == ClickAction.SECONDARY && slot.allowModification(player)) {
            ItemStack other = slot.getItem();
            if (other.getItem() instanceof TarotCardItem) {
                return insertCardIntoDeck(thisStack, other, player);
            }
        }
        return super.overrideStackedOnOther(thisStack, slot, action, player);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        InteractionHand otherHand = (hand == InteractionHand.MAIN_HAND) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack thisStack = player.getItemInHand(hand);
        ItemStack otherStack = player.getItemInHand(otherHand);

        if (otherStack.getItem() instanceof TarotCardItem) {
            if (!level.isClientSide()) {
                insertCardIntoDeck(thisStack, otherStack, player);
            }
            return InteractionResult.SUCCESS;
        }

        if (!level.isClientSide()) {
            player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                    (containerId, playerInv, p) -> new TarotDeckMenu(containerId, playerInv, thisStack),
                    Component.translatable("tarotcardsrevamp.gui.deck.title")
            ));
        }
        return InteractionResult.SUCCESS;
    }

    public static boolean insertCardIntoDeck(ItemStack deckStack, ItemStack cardStack, Player player) {
        if (!(cardStack.getItem() instanceof TarotCardItem cardItem)) {
            return false;
        }

        DeckDataComponent deck = getData(deckStack);
        CardDataComponent cardData = TarotCardItem.getData(cardStack);
        var cardType = cardItem.getCardType();
        var currentEntry = deck.getSlot(cardType);

        if (currentEntry.present()) {
            // Check if card is an upgrade (e.g. from Base to Ascended)
            if (!currentEntry.ascended() && cardData.ascended()) {
                DeckDataComponent updated = deck.withSlot(cardType.ordinal(),
                        new DeckDataComponent.DeckSlotEntry(true, cardData.active(), cardData.questProgress(), true));
                setData(deckStack, updated);
                cardStack.shrink(1);

                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.7f, 1.2f);
                player.sendOverlayMessage(Component.translatable("tarotcardsrevamp.gui.deck.card_upgraded", cardType.getEnglishName())
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                return true;
            } else if (!currentEntry.ascended() && cardData.questProgress() > currentEntry.questProgress()) {
                // Higher progress
                DeckDataComponent updated = deck.withSlot(cardType.ordinal(),
                        new DeckDataComponent.DeckSlotEntry(true, cardData.active(), cardData.questProgress(), false));
                setData(deckStack, updated);
                cardStack.shrink(1);

                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BUNDLE_INSERT, SoundSource.PLAYERS, 0.8f, 1.2f);
                player.sendOverlayMessage(Component.translatable("tarotcardsrevamp.gui.deck.card_inserted", cardType.getEnglishName())
                        .withStyle(ChatFormatting.GREEN));
                return true;
            } else {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.5f, 0.8f);
                player.sendOverlayMessage(Component.translatable("tarotcardsrevamp.gui.deck.already_present")
                        .withStyle(ChatFormatting.RED));
                return false;
            }
        }

        // Slot is empty - insert new card
        DeckDataComponent updated = deck.withSlot(cardType.ordinal(),
                new DeckDataComponent.DeckSlotEntry(true, cardData.active(), cardData.questProgress(), cardData.ascended()));
        setData(deckStack, updated);
        cardStack.shrink(1);

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BUNDLE_INSERT, SoundSource.PLAYERS, 0.8f, 1.1f);
        player.sendOverlayMessage(Component.translatable("tarotcardsrevamp.gui.deck.card_inserted", cardType.getEnglishName())
                .withStyle(ChatFormatting.GREEN));
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        DeckDataComponent deck = getData(stack);

        tooltipAdder.accept(Component.translatable("tarotcardsrevamp.gui.deck.title")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        tooltipAdder.accept(Component.translatable("tarotcardsrevamp.gui.deck.cards_stored", deck.getCardCount(), deck.getAscendedCount())
                .withStyle(ChatFormatting.YELLOW));

        tooltipAdder.accept(Component.translatable("tarotcardsrevamp.gui.deck.insert_hint")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));

        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, tooltipFlag);
    }
}
