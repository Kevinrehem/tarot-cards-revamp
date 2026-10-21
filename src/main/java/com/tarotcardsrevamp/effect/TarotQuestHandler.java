package com.tarotcardsrevamp.effect;

import com.tarotcardsrevamp.card.CardDataComponent;
import com.tarotcardsrevamp.card.TarotCardItem;
import com.tarotcardsrevamp.card.TarotCardType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

public class TarotQuestHandler {

    public static void progressQuest(Player player, TarotCardType targetCard, int amount) {
        if (player == null || player.level().isClientSide() || amount <= 0) return;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof TarotCardItem cardItem && cardItem.getCardType() == targetCard) {
                CardDataComponent data = TarotCardItem.getData(stack);
                if (data.active() && !data.ascended()) {
                    int goal = targetCard.getDefaultGoal();
                    CardDataComponent updated = data.addProgress(amount, goal);
                    TarotCardItem.setData(stack, updated);

                    if (updated.ascended()) {
                        triggerAscension(player, targetCard);
                    }
                }
            }
        }
    }

    private static void triggerAscension(Player player, TarotCardType card) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0f, 1.0f);

        player.sendSystemMessage(Component.translatable("tarotcardsrevamp.status.ascended")
                .append(": " + card.getEnglishName())
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        if (player instanceof ServerPlayer serverPlayer) {
            // Trigger advancement
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BreakBlockEvent event) {
        progressQuest(event.getPlayer(), TarotCardType.THE_STAR, 1);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            progressQuest(player, TarotCardType.DEATH, 1);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        if (event.getEntity() instanceof Player player) {
            progressQuest(player, TarotCardType.THE_HANGED_MAN, (int) event.getInflictedDamage());
        }
    }
}
