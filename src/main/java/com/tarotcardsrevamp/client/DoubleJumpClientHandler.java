package com.tarotcardsrevamp.client;

import com.tarotcardsrevamp.card.TarotCardType;
import com.tarotcardsrevamp.config.TarotConfig;
import com.tarotcardsrevamp.effect.ActiveCardResolver;
import com.tarotcardsrevamp.effect.TarotEffectHandler;
import com.tarotcardsrevamp.network.DoubleJumpPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;

@EventBusSubscriber(modid = "tarotcardsrevamp", value = Dist.CLIENT)
public class DoubleJumpClientHandler {

    private static boolean canDoubleJump = false;
    private static boolean wasJumpPressed = false;

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (!(event.getEntity() instanceof LocalPlayer player)) return;

        if (TarotConfig.ENABLE_ASCENDED_FOOL_DOUBLE_JUMP != null && !TarotConfig.ENABLE_ASCENDED_FOOL_DOUBLE_JUMP.get()) {
            canDoubleJump = false;
            return;
        }

        // Check if player has The Fool Ascended active
        ActiveCardResolver active = TarotEffectHandler.resolvePlayerActiveCards(player);
        if (!active.isActive(TarotCardType.THE_FOOL) || !active.isAscended(TarotCardType.THE_FOOL)) {
            canDoubleJump = false;
            return;
        }

        boolean isJumping = event.getInput().keyPresses.jump();

        // Reset double jump availability whenever player is on ground, swimming, in lava, climbing, or flying
        if (player.onGround() || player.isInWater() || player.isInLava() || player.onClimbable() || player.getAbilities().flying) {
            canDoubleJump = true;
            wasJumpPressed = isJumping;
            return;
        }

        // Player is airborne
        if (!isJumping) {
            // Player released the jump key while in air: now primed for the double jump
            wasJumpPressed = false;
        } else if (!wasJumpPressed && canDoubleJump) {
            // Player pressed jump again in mid-air: TRIGGER DOUBLE JUMP
            canDoubleJump = false;
            wasJumpPressed = true;

            // Execute Double Jump motion on client
            Vec3 motion = player.getDeltaMovement();
            player.setDeltaMovement(motion.x * 1.15, 0.52, motion.z * 1.15);
            player.resetFallDistance();

            // Client-side sound and particles for instant feedback
            player.playSound(SoundEvents.WIND_CHARGE_BURST.value(), 0.6f, 1.4f);
            for (int i = 0; i < 6; i++) {
                double offsetX = (player.getRandom().nextDouble() - 0.5) * 0.5;
                double offsetZ = (player.getRandom().nextDouble() - 0.5) * 0.5;
                player.level().addParticle(ParticleTypes.CLOUD,
                        player.getX() + offsetX,
                        player.getY() + 0.1,
                        player.getZ() + offsetZ,
                        0, 0, 0);
            }

            // Send packet to server
            if (Minecraft.getInstance().getConnection() != null) {
                Minecraft.getInstance().getConnection().send(new ServerboundCustomPayloadPacket(new DoubleJumpPayload()));
            }
        }
    }
}
