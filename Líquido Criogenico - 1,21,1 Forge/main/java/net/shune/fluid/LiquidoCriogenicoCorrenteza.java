package net.shune.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public class LiquidoCriogenicoCorrenteza extends ForgeFlowingFluid.Flowing {
    public LiquidoCriogenicoCorrenteza(Properties properties) {
        super(properties);
    }

    @Override
    public Vec3 getFlow(BlockGetter level, BlockPos pos, net.minecraft.world.level.material.FluidState state) {
        return super.getFlow(level, pos, state).scale(0.3);
    }
}