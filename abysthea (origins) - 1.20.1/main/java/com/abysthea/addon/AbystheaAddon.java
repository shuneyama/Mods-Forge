package com.abysthea.addon;

import com.abysthea.addon.rede.RedeAbysthea;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(AbystheaAddon.MODID)
public class AbystheaAddon {
    public static final String MODID = "abysthea_addon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public AbystheaAddon() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
        LOGGER.info("Abysthea Addon carregado!");
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(RedeAbysthea::registrar);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        AbystheaCommands.register(event.getDispatcher());
    }
}
