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
import java.util.Optional;

/**
 * Soft Curios API compatibility layer.
 * Uses reflection to query Curios slots without a hard compile or runtime dependency.
 */
public class CuriosCompat {

    private static final boolean CURIOS_LOADED = ModList.get().isLoaded("curios");

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
                Method getEquippedMethod = curiosHandler.getClass().getMethod("getEquippedCurios");
                Object itemHandler = getEquippedMethod.invoke(curiosHandler);
                Method getSlotsMethod = itemHandler.getClass().getMethod("getSlots");
                int slots = (int) getSlotsMethod.invoke(itemHandler);
                Method getStackMethod = itemHandler.getClass().getMethod("getStackInSlot", int.class);

                for (int i = 0; i < slots; i++) {
                    ItemStack stack = (ItemStack) getStackMethod.invoke(itemHandler, i);
                    if (stack == null || stack.isEmpty()) continue;

                    if (stack.getItem() instanceof TarotCardItem cardItem) {
                        var data = TarotCardItem.getData(stack);
                        resolver.offer(cardItem.getCardType(), data.active(), data.ascended(), data.questProgress());
                    } else if (stack.getItem() instanceof TarotDeckItem) {
                        var deck = TarotDeckItem.getData(stack);
                        for (int s = 0; s < DeckDataComponent.TOTAL_SLOTS; s++) {
                            var entry = deck.getSlot(s);
                            if (entry.present()) {
                                TarotCardType cardType = TarotCardType.values()[s];
                                resolver.offer(cardType, entry.active(), entry.ascended(), entry.questProgress());
                            }
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
            // Silently fallback if Curios internals differ
        }
    }
}
