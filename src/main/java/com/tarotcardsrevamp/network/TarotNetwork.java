package com.tarotcardsrevamp.network;

import com.tarotcardsrevamp.card.TarotCardType;
import com.tarotcardsrevamp.config.TarotConfig;
import com.tarotcardsrevamp.effect.ActiveCardResolver;
import com.tarotcardsrevamp.effect.TarotEffectHandler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class TarotNetwork {

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                DoubleJumpPayload.TYPE,
                DoubleJumpPayload.STREAM_CODEC,
                TarotNetwork::handleDoubleJump
        );
    }

    private static void handleDoubleJump(DoubleJumpPayload payload, IPayloadContext context) {
        if (TarotConfig.ENABLE_ASCENDED_FOOL_DOUBLE_JUMP != null && !TarotConfig.ENABLE_ASCENDED_FOOL_DOUBLE_JUMP.get()) {
            return;
        }

        context.enqueueWork(() -> {
            Player player = context.player();
            if (player instanceof ServerPlayer serverPlayer) {
                ActiveCardResolver active = TarotEffectHandler.resolvePlayerActiveCards(serverPlayer);
                if (active.isActive(TarotCardType.THE_FOOL) && active.isAscended(TarotCardType.THE_FOOL)) {
                    // Reset fall distance on server
                    serverPlayer.resetFallDistance();

                    // Play double jump wind sound and spawn cloud particles around player
                    if (serverPlayer.level() instanceof ServerLevel serverLevel) {
                        serverLevel.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                                SoundEvents.WIND_CHARGE_BURST, SoundSource.PLAYERS, 0.6f, 1.4f);

                        for (int i = 0; i < 8; i++) {
                            double offsetX = (serverPlayer.getRandom().nextDouble() - 0.5) * 0.6;
                            double offsetZ = (serverPlayer.getRandom().nextDouble() - 0.5) * 0.6;
                            serverLevel.sendParticles(ParticleTypes.CLOUD,
                                    serverPlayer.getX() + offsetX,
                                    serverPlayer.getY() + 0.1,
                                    serverPlayer.getZ() + offsetZ,
                                    1, 0, 0, 0, 0.05);
                        }
                    }
                }
            }
        });
    }
}
