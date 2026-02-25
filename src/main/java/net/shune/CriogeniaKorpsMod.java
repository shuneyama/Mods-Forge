package net.shune;

import net.shune.block.ModBlocos;
import net.shune.fluid.ModFluidos;
import net.shune.item.ModItems;
import net.shune.particle.ModParticulas;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(CriogeniaKorpsMod.MOD_ID)
public class CriogeniaKorpsMod {
    public static final String MOD_ID = "criogeniakorps";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CriogeniaKorpsMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModCreativeModTabs.register(modEventBus);
        ModFluidos.register(modEventBus);
        ModBlocos.register(modEventBus);
        ModItems.register(modEventBus);
        ModParticulas.register(modEventBus);
    }
}