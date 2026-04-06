package net.shune.purgatorio.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.shune.purgatorio.PurgatorioMod;
import net.shune.purgatorio.block.ModBlocks;

public class ModCreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PurgatorioMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> PURGATORIO_TAB = CREATIVE_MODE_TABS.register("purgatorio_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(Items.NETHER_STAR))
                    .title(Component.translatable("creativetab.purgatorio_tab"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.PURGATORIO_FOICE.get());
                        output.accept(ModItems.LANCA_ANGELICAL.get());
                        output.accept(ModItems.CAJADO_AMET.get());
                        output.accept(ModItems.CAJADO_LUCIFER.get());
                        output.accept(ModItems.CAJADO_SATA.get());
                        output.accept(ModBlocks.BLOCO_AR.get());
                        output.accept(ModBlocks.CAIXA_PANDORA.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}