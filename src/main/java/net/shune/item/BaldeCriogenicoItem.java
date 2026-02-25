package net.shune.item;

import net.shune.fluid.ModFluidos;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class BaldeCriogenicoItem extends BucketItem {
    public BaldeCriogenicoItem() {
        super(ModFluidos.LIQUIDO_CRIOGENICO_FONTE, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1));
    }
}