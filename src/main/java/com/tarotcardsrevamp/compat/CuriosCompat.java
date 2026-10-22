package com.tarotcardsrevamp.compat;

import com.tarotcardsrevamp.card.TarotCardItem;
import com.tarotcardsrevamp.card.TarotCardType;
import com.tarotcardsrevamp.deck.DeckDataComponent;
import com.tarotcardsrevamp.deck.TarotDeckItem;
import com.tarotcardsrevamp.effect.ActiveCardResolver;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Soft Curios API compatibility layer.
 * Uses reflection to query Curios slots without a hard compile or runtime dependency.
 */
public class CuriosCompat {

    private static final boolean CURIOS_LOADED = ModList.get().isLoaded("curios");
    private static boolean loggedError = false;

    public static boolean isCuriosLoaded() {
        return CURIOS_LOADED;
    }

    public static void scanCuriosSlots(LivingEntity entity, ActiveCardResolver resolver) {
        if (!CURIOS_LOADED || entity == null || resolver == null) {
            return;
        }

        try {
            Class<?> curiosApiClass = Class.forName("top.theillusivec4.curios.api.CuriosApi");
            Method getInventoryMethod = curiosApiClass.getMethod("getCuriosInventory", LivingEntity.class);
            Object inventoryOptional = getInventoryMethod.invoke(null, entity);

            if (inventoryOptional instanceof Optional<?> opt && opt.isPresent()) {
                Object curiosHandler = opt.get();

                Method findCuriosMethod = null;
                try {
                    Class<?> handlerInterface = Class.forName("top.theillusivec4.curios.api.type.capability.ICuriosItemHandler");
                    findCuriosMethod = handlerInterface.getMethod("findCurios", Predicate.class);
                } catch (Throwable ignored) {
                    findCuriosMethod = curiosHandler.getClass().getMethod("findCurios", Predicate.class);
                }
                findCuriosMethod.setAccessible(true);

                Predicate<ItemStack> matchPredicate = stack -> stack != null && !stack.isEmpty() &&
                        (stack.getItem() instanceof TarotDeckItem || stack.getItem() instanceof TarotCardItem);

                Object resultsObj = findCuriosMethod.invoke(curiosHandler, matchPredicate);
                if (resultsObj instanceof List<?> results) {
                    for (Object slotResult : results) {
                        if (slotResult == null) continue;

                        Method stackMethod = null;
                        try {
                            stackMethod = slotResult.getClass().getMethod("stack");
                        } catch (NoSuchMethodException e) {
                            stackMethod = slotResult.getClass().getMethod("getStack");
                        }
                        stackMethod.setAccessible(true);
                        Object stackObj = stackMethod.invoke(slotResult);

                        if (stackObj instanceof ItemStack stack && !stack.isEmpty()) {
                            if (stack.getItem() instanceof TarotCardItem cardItem) {
                                var data = TarotCardItem.getData(stack);
                                resolver.offer(cardItem.getCardType(), data.active(), data.ascended(), data.questProgress());
                            } else if (stack.getItem() instanceof TarotDeckItem) {
                                DeckDataComponent deck = TarotDeckItem.getData(stack);
                                for (int s = 0; s < DeckDataComponent.TOTAL_SLOTS; s++) {
                                    var entry = deck.getSlot(s);
                                    if (entry != null && entry.present()) {
                                        TarotCardType cardType = TarotCardType.values()[s];
                                        resolver.offer(cardType, entry.active(), entry.ascended(), entry.questProgress());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable t) {
            if (!loggedError) {
                loggedError = true;
                System.err.println("[TarotCardsRevamp] CuriosCompat error during slot scanning: " + t);
                t.printStackTrace();
            }
        }
    }
}
