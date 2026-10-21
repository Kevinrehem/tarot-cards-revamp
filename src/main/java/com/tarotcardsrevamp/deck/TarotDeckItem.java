package com.tarotcardsrevamp.deck;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        // The deck GUI is not available yet (no registered Screen, deck contents not persisted), so opening the
        // menu is intentionally disabled until the next phase to avoid opening a menu without a client screen.
        if (!level.isClientSide()) {
            player.sendOverlayMessage(Component.translatable("tarotcardsrevamp.gui.deck.coming_soon")
                    .withStyle(ChatFormatting.YELLOW));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        tooltipAdder.accept(Component.translatable("tarotcardsrevamp.gui.deck.title")
                .withStyle(ChatFormatting.GOLD));
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, tooltipFlag);
    }
}
