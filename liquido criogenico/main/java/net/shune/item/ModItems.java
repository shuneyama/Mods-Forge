package net.shune.item;

import net.shune.CriogeniaKorpsMod;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<net.minecraft.world.item.Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CriogeniaKorpsMod.MOD_ID);

    public static final RegistryObject<BaldeCriogenicoItem> BALDE_CRIOGENICO =
            ITEMS.register("balde_criogenico", BaldeCriogenicoItem::new);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}