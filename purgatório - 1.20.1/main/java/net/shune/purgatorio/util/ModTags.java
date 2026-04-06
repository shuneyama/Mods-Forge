package net.shune.purgatorio.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.shune.purgatorio.PurgatorioMod;

public class ModTags {
    public static class Blocks {
        public static final TagKey<Block> INCORRECT_FOR_ADM_TOOL = tag("incorrect_for_adm_tool");
        public static final TagKey<Block> NEEDS_ADM_TOOL = tag("needs_adm_tool");

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(new ResourceLocation(PurgatorioMod.MOD_ID, name));
        }
    }

    public static class Items {
        private static TagKey<Item> tag(String name) {
            return ItemTags.create(new ResourceLocation(PurgatorioMod.MOD_ID, name));
        }
    }
}