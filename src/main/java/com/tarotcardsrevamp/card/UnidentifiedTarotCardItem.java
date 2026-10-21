package com.tarotcardsrevamp.card;

import com.tarotcardsrevamp.core.ModItems;
import com.tarotcardsrevamp.loot.SmartCardSelector;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class UnidentifiedTarotCardItem extends Item {

    private static final SmartCardSelector SELECTOR = new SmartCardSelector();

    public UnidentifiedTarotCardItem(Properties properties) {
        super(properties.stacksTo(16));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            TarotCardType chosen = SELECTOR.pickCard(null);
            ItemStack revealed = new ItemStack(ModItems.getCardItem(chosen));

            held.shrink(1);
            if (!player.getInventory().add(revealed)) {
                player.drop(revealed, false, Prediction.SERVER_ONLY);
            }

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8f, 1.4f);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        tooltipAdder.accept(Component.translatable("item.tarotcardsrevamp.unidentified_tarot_card.desc")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, tooltipFlag);
    }
}
