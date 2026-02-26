package net.shune;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.shune.network.LuaNetworkHandler;

import java.util.Random;

@Mod(LuasMod.MODID)
public class LuasMod {
    public static final String MODID = "luasmod";

    private static final String SERVIDOR_AUTORIZADO = "br1.xmxcloud.net";
    private static final int PORTA_AUTORIZADA = 10100;
    private static boolean licencaValida = false;

    public enum TipoLua {
        NORMAL,
        DEUSA,
        SORTE,
        VERMELHA,
        SUPER_VERMELHA
    }

    public static TipoLua luaAtual = TipoLua.NORMAL;
    private static final Random random = new Random();

    public LuasMod() {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(LuaEventHandler.class);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(LuaNetworkHandler::register);
    }

    public static boolean isLicencaValida() {
        return licencaValida;
    }

    public static boolean isNoite(long tempoMundo) {
        long tempoDia = tempoMundo % 24000;
        return tempoDia >= 13000 && tempoDia <= 23000;
    }

    public static class LuaEventHandler {

        @SubscribeEvent
        public static void onServerStarted(ServerStartedEvent event) {
            MinecraftServer server = event.getServer();

            if (server.isSingleplayer()) {
                licencaValida = true;
                return;
            }

            int serverPort = server.getPort();

            if (serverPort == PORTA_AUTORIZADA) {
                licencaValida = true;
            } else {
                licencaValida = false;
            }
        }

        @SubscribeEvent
        public static void onWorldTick(TickEvent.LevelTickEvent event) {
            if (!licencaValida) return;
            if (event.phase != TickEvent.Phase.START) return;
            if (!(event.level instanceof ServerLevel serverLevel)) return;

            long tempo = serverLevel.getDayTime();

            if (luaAtual == TipoLua.SORTE && isNoite(tempo)) {
                for (Player player : serverLevel.players()) {
                    if (!player.hasEffect(MobEffects.LUCK)) {
                        player.addEffect(new MobEffectInstance(
                                MobEffects.LUCK,
                                400,
                                1,
                                true,
                                false
                        ));
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onMobDamage(LivingHurtEvent event) {
            if (!licencaValida) return;
            if (event.getSource().getEntity() == null) return;
            if (event.getSource().getEntity() instanceof Player) return;
            if (event.getEntity().level().isClientSide()) return;

            ServerLevel level = (ServerLevel) event.getEntity().level();
            if (!isNoite(level.getDayTime())) return;

            float multiplicador = switch (luaAtual) {
                case VERMELHA -> 1.25f;
                case SUPER_VERMELHA -> 1.75f;
                default -> 1.0f;
            };

            if (multiplicador > 1.0f) {
                event.setAmount(event.getAmount() * multiplicador);
            }
        }

        @SubscribeEvent
        public static void onCheckSpawn(MobSpawnEvent.SpawnPlacementCheck event) {
            if (!licencaValida) return;
            if (luaAtual != TipoLua.VERMELHA && luaAtual != TipoLua.SUPER_VERMELHA) return;

            var entityType = event.getEntityType();
            if (entityType.getCategory() != MobCategory.MONSTER) return;

            if (event.getResult() == Event.Result.DENY || event.getResult() == Event.Result.DEFAULT) {
                float chanceExtra = luaAtual == TipoLua.SUPER_VERMELHA ? 0.20f : 0.10f;

                if (random.nextFloat() < chanceExtra) {
                    event.setResult(Event.Result.ALLOW);
                }
            }
        }

        @SubscribeEvent
        public static void onMobSpawn(MobSpawnEvent.FinalizeSpawn event) {
            if (!licencaValida) return;
            if (event.getLevel().isClientSide()) return;
            if (!(event.getLevel() instanceof ServerLevel level)) return;
            if (!isNoite(level.getDayTime())) return;

            if (luaAtual == TipoLua.VERMELHA || luaAtual == TipoLua.SUPER_VERMELHA) {
                var mob = event.getEntity();

                int nivelForca = luaAtual == TipoLua.SUPER_VERMELHA ? 1 : 0;
                mob.addEffect(new MobEffectInstance(
                        MobEffects.DAMAGE_BOOST,
                        Integer.MAX_VALUE,
                        nivelForca,
                        true,
                        false
                ));

                if (luaAtual == TipoLua.SUPER_VERMELHA) {
                    mob.addEffect(new MobEffectInstance(
                            MobEffects.MOVEMENT_SPEED,
                            Integer.MAX_VALUE,
                            0,
                            true,
                            false
                    ));
                }
            }
        }
    }
}