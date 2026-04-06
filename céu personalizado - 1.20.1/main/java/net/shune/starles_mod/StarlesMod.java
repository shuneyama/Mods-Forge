package net.shune.starles_mod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.shune.starles_mod.block.ModBlocks;
import net.shune.starles_mod.event.ModEvents;

@Mod(StarlesMod.MODID)
public class StarlesMod {
    public static final String MODID = "starles";

    public static final ResourceKey<Level> VAZIO_LEVEL = ResourceKey.create(
            Registries.DIMENSION,
            new ResourceLocation(MODID, "vazio")
    );

    public static final ResourceKey<DimensionType> VAZIO_DIMENSION_TYPE = ResourceKey.create(
            Registries.DIMENSION_TYPE,
            new ResourceLocation(MODID, "vazio")
    );

    public StarlesMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(ModEvents.class);
    }
}
