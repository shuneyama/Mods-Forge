package net.shune.fluid;

import net.shune.CriogeniaKorpsMod;
import net.shune.block.ModBlocos;
import net.shune.item.ModItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModFluidos {
    public static final DeferredRegister<net.minecraft.world.level.material.Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, CriogeniaKorpsMod.MOD_ID);

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, CriogeniaKorpsMod.MOD_ID);

    public static final RegistryObject<FluidType> LIQUIDO_CRIOGENICO_TIPO = FLUID_TYPES.register("liquido_criogenico",
            () -> new LiquidoCriogenicoTipo(FluidType.Properties.create()
                    .canSwim(true)
                    .canDrown(false)
                    .canExtinguish(true)
                    .canConvertToSource(true)
                    .supportsBoating(true)
                    .sound(net.minecraftforge.common.SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(net.minecraftforge.common.SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .density(800)
                    .viscosity(3000)
                    .temperature(100)));

    public static final RegistryObject<LiquidoCriogenicoFonte> LIQUIDO_CRIOGENICO_FONTE = FLUIDS.register("liquido_criogenico_fonte",
            () -> new LiquidoCriogenicoFonte(fluidProperties()));

    public static final RegistryObject<LiquidoCriogenicoCorrenteza> LIQUIDO_CRIOGENICO_CORRENTEZA = FLUIDS.register("liquido_criogenico_correnteza",
            () -> new LiquidoCriogenicoCorrenteza(fluidProperties()));

    private static ForgeFlowingFluid.Properties fluidProperties() {
        return new ForgeFlowingFluid.Properties(LIQUIDO_CRIOGENICO_TIPO, LIQUIDO_CRIOGENICO_FONTE, LIQUIDO_CRIOGENICO_CORRENTEZA)
                .block(ModBlocos.LIQUIDO_CRIOGENICO_BLOCO_FLUIDO)
                .bucket(ModItems.BALDE_CRIOGENICO)
                .levelDecreasePerBlock(1)
                .slopeFindDistance(2)
                .explosionResistance(100f);
    }

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
        FLUIDS.register(eventBus);
    }
}