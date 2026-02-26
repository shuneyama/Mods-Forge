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

    // ========================
    // CONFIGURACAO DE PARTICULAS
    // ========================
    private static final int TICK_INTERVALO = 10;         // roda a cada X ticks (maior = menos lag)
    private static final int RAIO_BUSCA = 24;             // raio em blocos ao redor do jogador

    private static final int CHANCE_NEVE_LATERAL = 3;     // 1 em X blocos gera neve lateral (menor = mais particulas)
    private static final int CHANCE_NEVE_POR_LADO = 4;    // 1 em X lados spawna neve (menor = mais particulas)
    private static final int CHANCE_NEVE_INTERNA = 10;    // 1 em X blocos gera neve interna (menor = mais particulas)
    private static final int CHANCE_VAPOR = 3;            // 1 em X blocos gera vapor (menor = mais particulas)
    // ========================

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
        if (serverLevel.getGameTime() % TICK_INTERVALO != 0) return;

        net.minecraft.core.Direction[] lados = {
                net.minecraft.core.Direction.NORTH,
                net.minecraft.core.Direction.SOUTH,
                net.minecraft.core.Direction.EAST,
                net.minecraft.core.Direction.WEST
        };

        for (net.minecraft.world.entity.player.Player player : serverLevel.players()) {
            BlockPos centro = player.blockPosition();

            for (int dx = -RAIO_BUSCA; dx <= RAIO_BUSCA; dx++) {
                for (int dz = -RAIO_BUSCA; dz <= RAIO_BUSCA; dz++) {
                    for (int dy = -4; dy <= 4; dy++) {
                        BlockPos pos = centro.offset(dx, dy, dz);
                        FluidState state = serverLevel.getFluidState(pos);

                        boolean ehFonte = state.getType() == ModFluidos.LIQUIDO_CRIOGENICO_FONTE.get();
                        boolean ehCorrenteza = state.getType() == ModFluidos.LIQUIDO_CRIOGENICO_CORRENTEZA.get();
                        if (!ehFonte && !ehCorrenteza) continue;

                        // NEVE LATERAL (fora do liquido, nao entra)
                        if (random.nextInt(CHANCE_NEVE_LATERAL) == 0) {
                            for (net.minecraft.core.Direction lado : lados) {
                                BlockPos vizinho = pos.relative(lado);
                                FluidState vizinhoFluid = serverLevel.getFluidState(vizinho);
                                boolean vizinhoEhLiquido = vizinhoFluid.getType() == ModFluidos.LIQUIDO_CRIOGENICO_FONTE.get()
                                        || vizinhoFluid.getType() == ModFluidos.LIQUIDO_CRIOGENICO_CORRENTEZA.get();
                                if (!serverLevel.getBlockState(vizinho).isAir() && !vizinhoEhLiquido) continue;
                                if (random.nextInt(CHANCE_NEVE_POR_LADO) != 0) continue;

                                double px = pos.getX() + 0.5 + lado.getStepX() * 0.55;
                                double py = pos.getY() + 0.1 + random.nextDouble() * state.getHeight(serverLevel, pos);
                                double pz = pos.getZ() + 0.5 + lado.getStepZ() * 0.55;

                                double vx = lado.getStepX() * 0.04;
                                double vz = lado.getStepZ() * 0.04;

                                serverLevel.sendParticles(ModParticulas.NEVE_CRIOGENICA.get(), px, py, pz, 1, vx, 0, vz, 0);
                            }
                        }

                        // NEVE INTERNA (dentro do liquido, nao sai)
                        if (random.nextInt(CHANCE_NEVE_INTERNA) == 0) {
                            double px = pos.getX() + 0.1 + random.nextDouble() * 0.8;
                            double py = pos.getY() + random.nextDouble() * state.getHeight(serverLevel, pos);
                            double pz = pos.getZ() + 0.1 + random.nextDouble() * 0.8;
                            serverLevel.sendParticles(ModParticulas.NEVE_CRIOGENICA.get(), px, py, pz, 1, 0, -1, 0, 0);
                        }

                        // VAPOR (apenas da fonte, acima da superficie)
                        FluidState acima = serverLevel.getFluidState(pos.above());
                        boolean temLiquidoAcima = acima.getType() == ModFluidos.LIQUIDO_CRIOGENICO_FONTE.get()
                                || acima.getType() == ModFluidos.LIQUIDO_CRIOGENICO_CORRENTEZA.get();
                        if (!temLiquidoAcima && ehFonte && random.nextInt(CHANCE_VAPOR) == 0) {
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