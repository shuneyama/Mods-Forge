package net.shune.block;

import net.shune.CriogeniaKorpsMod;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocos {
    public static final DeferredRegister<net.minecraft.world.level.block.Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, CriogeniaKorpsMod.MOD_ID);

    public static final RegistryObject<LiquidoCriogenicoBloco> LIQUIDO_CRIOGENICO_BLOCO_FLUIDO =
            BLOCKS.register("liquido_criogenico", LiquidoCriogenicoBloco::new);

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}