package net.shune.purgatorio.block;

import java.util.function.Supplier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.BarrierBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.shune.purgatorio.PurgatorioMod;
import net.shune.purgatorio.item.ModItems;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, PurgatorioMod.MOD_ID);

    public static final RegistryObject<Block> BLOCO_AR = registerBlock("bloco_ar",
            () -> new BarrierBlock(BlockBehaviour.Properties.of()
                    .noLootTable()
                    .noOcclusion()
                    .noCollission()
                    .strength(-1.0F, 3600000.8F)
                    .pushReaction(PushReaction.BLOCK)));

    public static final RegistryObject<Block> CAIXA_PANDORA = registerBlock("caixa_pandora",
            () -> new CaixaPandoraBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .instrument(NoteBlockInstrument.BANJO)
                    .strength(-1.0F, 3600000.0F)
                    .sound(SoundType.WOOD)
                    .noCollission()));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}