package com.lootrnerf;

import com.lootrnerf.config.LootrNerfConfig;
import com.lootrnerf.data.FirstOpenerTracker;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * LootrNerf - Um addon para o Lootr que implementa vantagem do primeiro a abrir
 * 
 * Funcionalidade:
 * - Primeiro jogador a abrir: ~90% chance de loot bom (normal)
 * - Demais jogadores: ~10% chance, com loot significativamente reduzido
 * - Objetivo: impedir farm fácil, mas ainda dar mínimo de conforto
 */
@Mod(LootrNerf.MOD_ID)
public class LootrNerf {
    public static final String MOD_ID = "lootrnerf";
    public static final Logger LOGGER = LogManager.getLogger();

    public LootrNerf(FMLJavaModLoadingContext context) {
        LOGGER.info("LootrNerf - Inicializando sistema de nerf do Lootr");

        // Registrar configurações
        context.registerConfig(ModConfig.Type.SERVER, LootrNerfConfig.SERVER_SPEC);

        // Registrar eventos
        context.getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("LootrNerf - Setup completo!");
        // Não acessar config aqui - ela ainda não está carregada
        // As configs serão lidas quando forem necessárias durante o jogo
    }
}
