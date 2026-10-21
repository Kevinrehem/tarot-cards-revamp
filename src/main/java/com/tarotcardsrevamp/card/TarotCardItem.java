package com.tarotcardsrevamp.card;

import com.tarotcardsrevamp.core.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class TarotCardItem extends Item {

    private final TarotCardType cardType;

    public TarotCardItem(TarotCardType cardType, Properties properties) {
        super(properties.stacksTo(1).component(ModDataComponents.CARD_DATA.get(), CardDataComponent.DEFAULT));
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
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            CardDataComponent current = getData(stack);
            boolean newActive = !current.active();
            setData(stack, current.withActive(newActive));

            if (newActive) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.6f, 1.2f);
                player.displayClientMessage(Component.translatable("tarotcardsrevamp.status.active")
                        .withStyle(ChatFormatting.GREEN), true);
            } else {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.5f, 0.8f);
                player.displayClientMessage(Component.translatable("tarotcardsrevamp.status.inactive")
                        .withStyle(ChatFormatting.RED), true);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        CardDataComponent data = getData(stack);
        int goal = cardType.getDefaultGoal();

        if (data.ascended()) {
            tooltipComponents.add(Component.translatable("tarotcardsrevamp.status.ascended")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        } else {
            tooltipComponents.add(Component.translatable("tarotcardsrevamp.status.base")
                    .withStyle(ChatFormatting.AQUA));
            tooltipComponents.add(Component.translatable("tarotcardsrevamp.tooltip.quest",
                    data.questProgress(), goal, data.getPercent(goal))
                    .withStyle(ChatFormatting.YELLOW));
        }

        if (data.active()) {
            tooltipComponents.add(Component.translatable("tarotcardsrevamp.status.active")
                    .withStyle(ChatFormatting.GREEN));
        } else {
            tooltipComponents.add(Component.translatable("tarotcardsrevamp.status.inactive")
                    .withStyle(ChatFormatting.RED));
        }

        tooltipComponents.add(Component.translatable("tarotcardsrevamp.tooltip.toggle_hint")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
