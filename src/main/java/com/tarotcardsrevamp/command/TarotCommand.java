package com.tarotcardsrevamp.command;

import com.mojang.brigadier.CommandDispatcher;
import com.tarotcardsrevamp.card.CardDataComponent;
import com.tarotcardsrevamp.card.TarotCardItem;
import com.tarotcardsrevamp.card.TarotCardType;
import com.tarotcardsrevamp.card.UnidentifiedTarotCardItem;
import com.tarotcardsrevamp.core.ModItems;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class TarotCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tarot")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
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
                .then(Commands.literal("testloot")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            ServerLevel level = (ServerLevel) player.level();
                            LootTable table = level.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.DESERT_PYRAMID);

                            int totalRolls = 100;
                            int cardsFound = 0;
                            int chestsWithCards = 0;
                            int count1 = 0;
                            int count2 = 0;
                            int count3 = 0;

                            for (int i = 0; i < totalRolls; i++) {
                                LootParams params = new LootParams.Builder(level)
                                        .withParameter(LootContextParams.ORIGIN, player.position())
                                        .create(LootContextParamSets.CHEST);
                                ObjectArrayList<ItemStack> items = table.getRandomItems(params);
                                int foundInThisRoll = 0;
                                for (ItemStack item : items) {
                                    if (item.getItem() instanceof UnidentifiedTarotCardItem) {
                                        foundInThisRoll += item.getCount();
                                    }
                                }
                                if (foundInThisRoll > 0) {
                                    chestsWithCards++;
                                    cardsFound += foundInThisRoll;
                                    if (foundInThisRoll == 1) count1++;
                                    else if (foundInThisRoll == 2) count2++;
                                    else if (foundInThisRoll >= 3) count3++;
                                }
                            }

                            int finalChests = chestsWithCards;
                            int finalCards = cardsFound;
                            int f1 = count1, f2 = count2, f3 = count3;
                            context.getSource().sendSuccess(() -> Component.literal(String.format(
                                    "[TarotLoot] 100 rolls of chests/desert_pyramid: %d chests had cards (Total cards: %d). Details: [1 card: %d, 2 cards: %d, 3 cards: %d]",
                                    finalChests, finalCards, f1, f2, f3
                            )).withStyle(ChatFormatting.GREEN), true);
                            return 1;
                        })
                )
        );
    }
}
