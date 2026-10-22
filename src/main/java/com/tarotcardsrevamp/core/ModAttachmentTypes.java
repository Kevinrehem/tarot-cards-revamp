package com.tarotcardsrevamp.core;

import com.mojang.serialization.Codec;
import com.tarotcardsrevamp.TarotCardsRevampMod;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachmentTypes {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, TarotCardsRevampMod.MOD_ID);

    public static final Supplier<AttachmentType<Integer>> DIVINATION_OFFER = ATTACHMENT_TYPES.register(
            "divination_offer",
            () -> AttachmentType.builder(() -> -1)
                    .serialize(Codec.INT.fieldOf("offer"))
                    .sync(ByteBufCodecs.VAR_INT)
                    .copyOnDeath()
                    .build()
    );
}
