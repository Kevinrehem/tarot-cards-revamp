package com.tarotcardsrevamp.compat;

import com.tarotcardsrevamp.card.TarotCardItem;
import com.tarotcardsrevamp.card.TarotCardType;
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
            // CuriosApi.getCuriosInventory(entity)
            Class<?> curiosApiClass = Class.forName("top.theillusivec4.curios.api.CuriosApi");
            Method getInventoryMethod = curiosApiClass.getMethod("getCuriosInventory", LivingEntity.class);
            Object inventoryOptional = getInventoryMethod.invoke(null, entity);

            if (inventoryOptional instanceof Optional<?> opt && opt.isPresent()) {
                Object curiosHandler = opt.get();
                Method findCuriosMethod = curiosHandler.getClass().getMethod("findCurios", java.util.function.Predicate.class);
                // Inspect equipped items
            }
        } catch (Throwable ignored) {
            // Silently fallback if Curios internals differ
        }
    }
}
