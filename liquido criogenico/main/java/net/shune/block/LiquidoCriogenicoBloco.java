package net.shune.block;

import net.shune.fluid.ModFluidos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

public class LiquidoCriogenicoBloco extends LiquidBlock {
    public LiquidoCriogenicoBloco() {
        super(ModFluidos.LIQUIDO_CRIOGENICO_FONTE,
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_CYAN)
                        .noCollission()
                        .strength(100.0f)
                        .noLootTable()
                        .liquid());
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return true;
    }
}