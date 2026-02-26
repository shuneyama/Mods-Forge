package net.shune;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.shune.item.ModItems;

public class ModCreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CriogeniaKorpsMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> CRIOGENIAKORPSS_TAB = CREATIVE_MODE_TABS.register("criogeniakorps_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.BALDE_CRIOGENICO.get()))
                    .title(Component.translatable("creativetab.criogeniakorps_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(ModItems.BALDE_CRIOGENICO.get());
                    })
                    .build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
