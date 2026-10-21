package com.tarotcardsrevamp.command;

import com.mojang.brigadier.CommandDispatcher;
import com.tarotcardsrevamp.card.CardDataComponent;
import com.tarotcardsrevamp.card.TarotCardItem;
import com.tarotcardsrevamp.card.TarotCardType;
import com.tarotcardsrevamp.core.ModItems;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class TarotCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tarot")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("giveall")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            for (TarotCardType type : TarotCardType.values()) {
                                ItemStack stack = new ItemStack(ModItems.getCardItem(type));
                                player.getInventory().add(stack);
                            }
                            player.getInventory().add(new ItemStack(ModItems.TAROT_DECK.get()));
                            context.getSource().sendSuccess(() -> Component.literal("Given all 22 Tarot Cards and Tarot Deck!"), true);
                            return 1;
                        })
                )
                .then(Commands.literal("ascend_held")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            ItemStack held = player.getMainHandItem();
                            if (held.getItem() instanceof TarotCardItem cardItem) {
                                CardDataComponent data = TarotCardItem.getData(held);
                                TarotCardItem.setData(held, data.withProgress(cardItem.getCardType().getDefaultGoal(), cardItem.getCardType().getDefaultGoal()));
                                context.getSource().sendSuccess(() -> Component.literal("Ascended " + cardItem.getCardType().getEnglishName() + "!"), true);
                                return 1;
                            } else {
                                context.getSource().sendFailure(Component.literal("You must hold a Tarot Card in your main hand!"));
                                return 0;
                            }
                        })
                )
        );
    }
}
