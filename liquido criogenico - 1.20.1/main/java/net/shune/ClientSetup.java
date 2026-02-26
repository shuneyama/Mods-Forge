package net.shune;

import net.shune.fluid.ModFluidos;
import net.shune.particle.ModParticulas;
import net.shune.particle.ParticulaNeve;
import net.shune.particle.ParticulaVaporCriogenico;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = CriogeniaKorpsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(ModFluidos.LIQUIDO_CRIOGENICO_FONTE.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluidos.LIQUIDO_CRIOGENICO_CORRENTEZA.get(), RenderType.translucent());
    }

    @SubscribeEvent
    public static void onRegistrarParticulas(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticulas.NEVE_CRIOGENICA.get(), ParticulaNeve.Provider::new);
        event.registerSpriteSet(ModParticulas.VAPOR_CRIOGENICO.get(), ParticulaVaporCriogenico.Provider::new);
    }
}