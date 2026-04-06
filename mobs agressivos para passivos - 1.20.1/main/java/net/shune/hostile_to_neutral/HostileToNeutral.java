package net.shune.hostile_to_neutral;

import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Mod(HostileToNeutral.MODID)
public class HostileToNeutral {

    public static final String MODID = "hostile_to_neutral";
    private static final Logger LOGGER = LogUtils.getLogger();

    public HostileToNeutral() {
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("[NoHostileMobs] Mod carregado! Todos os mobs serao pacificos.");
    }

    @SubscribeEvent
    public void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;

        if (event.getEntity() instanceof Mob mob) {
            tornarPacifico(mob);
        }
    }

    @SubscribeEvent
    public void onTargetChange(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof Mob) {
            if (event.getNewTarget() instanceof Player) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        if (event.getSource().getEntity() instanceof Mob) {
            event.setCanceled(true);
        }

        if (event.getSource().getDirectEntity() instanceof Mob) {
            event.setCanceled(true);
        }

        String nomeDano = event.getSource().getMsgId();
        if (nomeDano.contains("mob") || nomeDano.contains("wither") ||
                nomeDano.contains("sonic") || nomeDano.contains("dragon") ||
                nomeDano.contains("fireball") || nomeDano.contains("arrow") ||
                nomeDano.contains("thrown") || nomeDano.contains("sting") ||
                nomeDano.contains("thorns")) {

            if (event.getSource().getEntity() != null && !(event.getSource().getEntity() instanceof Player)) {
                event.setCanceled(true);
            }
        }
    }

    private void tornarPacifico(Mob mob) {
        try {
            List<Goal> goalsParaRemover = new ArrayList<>();
            for (WrappedGoal wrappedGoal : mob.targetSelector.getAvailableGoals()) {
                goalsParaRemover.add(wrappedGoal.getGoal());
            }
            for (Goal goal : goalsParaRemover) {
                mob.targetSelector.removeGoal(goal);
            }

            List<Goal> ataquesParaRemover = new ArrayList<>();
            for (WrappedGoal wrappedGoal : mob.goalSelector.getAvailableGoals()) {
                String nomeGoal = wrappedGoal.getGoal().getClass().getSimpleName().toLowerCase();
                if (nomeGoal.contains("attack") || nomeGoal.contains("melee") ||
                        nomeGoal.contains("ranged") || nomeGoal.contains("bow") ||
                        nomeGoal.contains("shoot") || nomeGoal.contains("spit") ||
                        nomeGoal.contains("fireball") || nomeGoal.contains("sonic") ||
                        nomeGoal.contains("roar") || nomeGoal.contains("hurt") ||
                        nomeGoal.contains("revenge") || nomeGoal.contains("defend") ||
                        nomeGoal.contains("charge") || nomeGoal.contains("leap") ||
                        nomeGoal.contains("pounce") || nomeGoal.contains("ram")) {
                    ataquesParaRemover.add(wrappedGoal.getGoal());
                }
            }
            for (Goal goal : ataquesParaRemover) {
                mob.goalSelector.removeGoal(goal);
            }

            mob.setTarget(null);

        } catch (Exception e) {
            LOGGER.debug("[NoHostileMobs] Nao foi possivel tornar {} pacifico: {}",
                    mob.getType().getDescriptionId(), e.getMessage());
        }
    }
}