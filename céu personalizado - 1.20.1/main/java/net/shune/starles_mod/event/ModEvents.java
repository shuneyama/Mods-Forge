package net.shune.starles_mod.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.shune.starles_mod.StarlesMod;

public class ModEvents {

    @SubscribeEvent
    public void onPlayerSleep(PlayerSleepInBedEvent event) {
        if (event.getEntity().level().dimension() == StarlesMod.VAZIO_LEVEL) {
            event.setResult(Player.BedSleepingProblem.NOT_POSSIBLE_HERE);
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel serverLevel) {
            if (serverLevel.dimension() == StarlesMod.VAZIO_LEVEL) {
                serverLevel.setDayTime(18000);
            }
        }
    }

    @SubscribeEvent
    public void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity().level().dimension() == StarlesMod.VAZIO_LEVEL) {
            event.getEntity().setDeltaMovement(
                    event.getEntity().getDeltaMovement().x,
                    event.getEntity().getDeltaMovement().y * 1.5,
                    event.getEntity().getDeltaMovement().z
            );
        }
    }

    @SubscribeEvent
    public void onLivingFall(LivingFallEvent event) {
        if (event.getEntity().level().dimension() == StarlesMod.VAZIO_LEVEL) {
            event.setDistance(event.getDistance() * 0.5f);
            event.setDamageMultiplier(event.getDamageMultiplier() * 0.5f);
        }
    }

    @SubscribeEvent
    public void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity().level().dimension() == StarlesMod.VAZIO_LEVEL) {
            if (event.getEntity().getDeltaMovement().y < 0) {
                event.getEntity().setDeltaMovement(
                        event.getEntity().getDeltaMovement().x,
                        event.getEntity().getDeltaMovement().y * 0.85,
                        event.getEntity().getDeltaMovement().z
                );
            }
        }
    }
}