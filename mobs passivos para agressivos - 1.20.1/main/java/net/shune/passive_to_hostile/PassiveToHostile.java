package net.shune.passive_to_hostile;

import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.EnumSet;

@Mod(PassiveToHostile.MODID)
public class PassiveToHostile {

    public static final String MODID = "passive_to_hostile";
    private static final Logger LOGGER = LogUtils.getLogger();

    public PassiveToHostile() {
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("[NoPassiveMobs] Mod carregado! Todos os mobs serao hostis.");
    }

    @SubscribeEvent
    public void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;

        if (event.getEntity() instanceof Mob mob) {
            if (mob instanceof Monster) return;

            if (mob instanceof PathfinderMob pathfinderMob) {
                tornarHostil(pathfinderMob);
            }
        }
    }

    private void tornarHostil(PathfinderMob mob) {
        try {
            mob.goalSelector.addGoal(1, new AtaquePassivoGoal(mob, 1.2D));
            mob.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(mob, Player.class, true));
        } catch (Exception e) {
            LOGGER.debug("[NoPassiveMobs] Nao foi possivel tornar {} hostil: {}",
                    mob.getType().getDescriptionId(), e.getMessage());
        }
    }

    public static class AtaquePassivoGoal extends Goal {
        private final PathfinderMob mob;
        private final double velocidade;
        private Player alvo;
        private int cooldownAtaque;
        private long ultimoCheck;

        public AtaquePassivoGoal(PathfinderMob mob, double velocidade) {
            this.mob = mob;
            this.velocidade = velocidade;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            long tempo = this.mob.level().getGameTime();
            if (tempo - this.ultimoCheck < 20L) {
                return false;
            }
            this.ultimoCheck = tempo;

            this.alvo = this.mob.level().getNearestPlayer(this.mob, 16.0D);
            return this.alvo != null && this.alvo.isAlive() && !this.alvo.isCreative() && !this.alvo.isSpectator();
        }

        @Override
        public boolean canContinueToUse() {
            return this.alvo != null && this.alvo.isAlive() && !this.alvo.isCreative() && !this.alvo.isSpectator() && this.mob.distanceToSqr(this.alvo) < 256.0D;
        }

        @Override
        public void start() {
            this.mob.getNavigation().moveTo(this.alvo, this.velocidade);
            this.cooldownAtaque = 0;
        }

        @Override
        public void stop() {
            this.alvo = null;
            this.mob.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (this.alvo == null) return;

            this.mob.getLookControl().setLookAt(this.alvo, 30.0F, 30.0F);

            double distancia = this.mob.distanceToSqr(this.alvo);

            this.cooldownAtaque = Math.max(this.cooldownAtaque - 1, 0);

            this.mob.getNavigation().moveTo(this.alvo, this.velocidade);

            double alcance = (double)(this.mob.getBbWidth() * 2.0F * this.mob.getBbWidth() * 2.0F + this.alvo.getBbWidth());

            if (distancia <= alcance && this.cooldownAtaque <= 0) {
                this.cooldownAtaque = 20;
                this.alvo.hurt(this.mob.damageSources().mobAttack(this.mob), 2.0F);
            }
        }
    }
}