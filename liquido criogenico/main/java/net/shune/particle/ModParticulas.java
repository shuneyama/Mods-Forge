package net.shune.particle;

import net.shune.CriogeniaKorpsMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModParticulas {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, CriogeniaKorpsMod.MOD_ID);

    public static final RegistryObject<SimpleParticleType> NEVE_CRIOGENICA =
            PARTICLE_TYPES.register("neve_criogenica", () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> VAPOR_CRIOGENICO =
            PARTICLE_TYPES.register("vapor_criogenico", () -> new SimpleParticleType(false));

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }
}