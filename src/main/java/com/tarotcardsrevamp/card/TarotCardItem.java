package com.tarotcardsrevamp.card;

import com.tarotcardsrevamp.core.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class TarotCardItem extends Item {

    private final TarotCardType cardType;

    public TarotCardItem(TarotCardType cardType, Properties properties) {
        // No default component here: reading a DeferredHolder during the item RegisterEvent depends on registry
        // ordering. getData() already falls back to CardDataComponent.DEFAULT when the component is absent.
        super(properties.stacksTo(1));
        this.cardType = cardType;
    }

    public TarotCardType getCardType() {
        return cardType;
    }

    public static CardDataComponent getData(ItemStack stack) {
        CardDataComponent data = stack.get(ModDataComponents.CARD_DATA.get());
        return data != null ? data : CardDataComponent.DEFAULT;
    }

    public static void setData(ItemStack stack, CardDataComponent data) {
        stack.set(ModDataComponents.CARD_DATA.get(), data);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return getData(stack).ascended();
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            CardDataComponent current = getData(stack);
            boolean newActive = !current.active();
            setData(stack, current.withActive(newActive));

            if (newActive) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.6f, 1.2f);
                player.sendOverlayMessage(Component.translatable("tarotcardsrevamp.status.active")
                        .withStyle(ChatFormatting.GREEN));
            } else {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.5f, 0.8f);
                player.sendOverlayMessage(Component.translatable("tarotcardsrevamp.status.inactive")
                        .withStyle(ChatFormatting.RED));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        CardDataComponent data = getData(stack);
        int goal = cardType.getDefaultGoal();

        if (data.ascended()) {
            tooltipAdder.accept(Component.translatable("tarotcardsrevamp.status.ascended")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            tooltipAdder.accept(Component.translatable("tarotcardsrevamp.tooltip.ascended_power_label")
                    .withStyle(ChatFormatting.GOLD)
                    .append(Component.translatable("tarotcardsrevamp.card." + cardType.getId() + ".desc.ascended")
                            .withStyle(ChatFormatting.YELLOW)));
            tooltipAdder.accept(Component.translatable("tarotcardsrevamp.tooltip.quest_completed_label")
                    .withStyle(ChatFormatting.DARK_GREEN)
                    .append(Component.translatable(cardType.getQuestTranslationKey())
                            .withStyle(ChatFormatting.GRAY)));
        } else {
            tooltipAdder.accept(Component.translatable("tarotcardsrevamp.status.base")
                    .withStyle(ChatFormatting.AQUA));
            tooltipAdder.accept(Component.translatable("tarotcardsrevamp.tooltip.buff_label")
                    .withStyle(ChatFormatting.BLUE)
                    .append(Component.translatable("tarotcardsrevamp.card." + cardType.getId() + ".desc.base")
                            .withStyle(ChatFormatting.GRAY)));
            tooltipAdder.accept(Component.translatable("tarotcardsrevamp.tooltip.ascended_power_label")
                    .withStyle(ChatFormatting.DARK_PURPLE)
                    .append(Component.translatable("tarotcardsrevamp.card." + cardType.getId() + ".desc.ascended")
                            .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)));
            tooltipAdder.accept(Component.translatable("tarotcardsrevamp.tooltip.quest_objective_label")
                    .withStyle(ChatFormatting.GOLD)
                    .append(Component.translatable(cardType.getQuestTranslationKey())
                            .withStyle(ChatFormatting.YELLOW)));
            tooltipAdder.accept(Component.translatable("tarotcardsrevamp.tooltip.quest_progress",
                    data.questProgress(), goal, data.getPercent(goal))
                    .withStyle(ChatFormatting.YELLOW));
        }

        if (data.active()) {
            tooltipAdder.accept(Component.translatable("tarotcardsrevamp.status.active")
                    .withStyle(ChatFormatting.GREEN));
        } else {
            tooltipAdder.accept(Component.translatable("tarotcardsrevamp.status.inactive")
                    .withStyle(ChatFormatting.RED));
        }

        tooltipAdder.accept(Component.translatable("tarotcardsrevamp.tooltip.toggle_hint")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));

        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, tooltipFlag);
    }
}
