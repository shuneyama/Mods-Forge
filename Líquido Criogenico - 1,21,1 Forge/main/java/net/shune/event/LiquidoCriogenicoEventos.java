package net.shune.event;

import net.shune.CriogeniaKorpsMod;
import net.shune.fluid.ModFluidos;
import net.shune.particle.ModParticulas;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = CriogeniaKorpsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LiquidoCriogenicoEventos {
    private static final Random random = new Random();

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        BlockPos eyePos = BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ());
        FluidState fluidState = entity.level().getFluidState(eyePos);
        if (fluidState.getType() == ModFluidos.LIQUIDO_CRIOGENICO_FONTE.get()
                || fluidState.getType() == ModFluidos.LIQUIDO_CRIOGENICO_CORRENTEZA.get()) {
            entity.setAirSupply(entity.getMaxAirSupply());
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide()) return;
        if (!(event.level instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;
        if (serverLevel.getGameTime() % 3 != 0) return;

        net.minecraft.core.Direction[] lados = {
                net.minecraft.core.Direction.NORTH,
                net.minecraft.core.Direction.SOUTH,
                net.minecraft.core.Direction.EAST,
                net.minecraft.core.Direction.WEST
        };

        for (net.minecraft.world.entity.player.Player player : serverLevel.players()) {
            BlockPos centro = player.blockPosition();
            int raio = 24;

            for (int dx = -raio; dx <= raio; dx++) {
                for (int dz = -raio; dz <= raio; dz++) {
                    for (int dy = -4; dy <= 4; dy++) {
                        BlockPos pos = centro.offset(dx, dy, dz);
                        FluidState state = serverLevel.getFluidState(pos);

                        boolean ehFonte = state.getType() == ModFluidos.LIQUIDO_CRIOGENICO_FONTE.get();
                        boolean ehCorrenteza = state.getType() == ModFluidos.LIQUIDO_CRIOGENICO_CORRENTEZA.get();
                        if (!ehFonte && !ehCorrenteza) continue;

                        if (random.nextInt(6) == 0) {
                            for (net.minecraft.core.Direction lado : lados) {
                                BlockPos vizinho = pos.relative(lado);
                                FluidState vizinhoFluid = serverLevel.getFluidState(vizinho);
                                boolean vizinhoEhLiquido = vizinhoFluid.getType() == ModFluidos.LIQUIDO_CRIOGENICO_FONTE.get()
                                        || vizinhoFluid.getType() == ModFluidos.LIQUIDO_CRIOGENICO_CORRENTEZA.get();
                                if (!serverLevel.getBlockState(vizinho).isAir() && !vizinhoEhLiquido) continue;
                                if (random.nextInt(2) != 0) continue;

                                double px = pos.getX() + 0.5 + lado.getStepX() * 0.5;
                                double py = pos.getY() + 0.1 + random.nextDouble() * (state.getHeight(serverLevel, pos));
                                double pz = pos.getZ() + 0.5 + lado.getStepZ() * 0.5;

                                serverLevel.sendParticles(ModParticulas.NEVE_CRIOGENICA.get(), px, py, pz, 1, 0, 0, 0, 0);
                            }
                        }

                        if (random.nextInt(6) == 0) {
                            double px = pos.getX() + 0.1 + random.nextDouble() * 0.8;
                            double py = pos.getY() + random.nextDouble() * state.getHeight(serverLevel, pos);
                            double pz = pos.getZ() + 0.1 + random.nextDouble() * 0.8;
                            serverLevel.sendParticles(ModParticulas.NEVE_CRIOGENICA.get(), px, py, pz, 1, 0, 0, 0, 0);
                        }

                        FluidState acima = serverLevel.getFluidState(pos.above());
                        boolean temLiquidoAcima = acima.getType() == ModFluidos.LIQUIDO_CRIOGENICO_FONTE.get()
                                || acima.getType() == ModFluidos.LIQUIDO_CRIOGENICO_CORRENTEZA.get();
                        if (!temLiquidoAcima && ehFonte && random.nextInt(6) == 0) {
                            double px = pos.getX() + 0.1 + random.nextDouble() * 0.8;
                            double py = pos.getY() + state.getHeight(serverLevel, pos) + (3.0 / 16.0);
                            double pz = pos.getZ() + 0.1 + random.nextDouble() * 0.8;
                            serverLevel.sendParticles(ModParticulas.VAPOR_CRIOGENICO.get(), px, py, pz, 1, 0, 0, 0, 0);
                        }
                    }
                }
            }
        }
    }
}