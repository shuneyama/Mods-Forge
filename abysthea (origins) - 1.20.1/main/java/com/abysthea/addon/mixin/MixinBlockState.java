package com.abysthea.addon.mixin;

import com.abysthea.addon.AbystheaAddon;
import com.abysthea.addon.cliente.SlimePhaseCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class MixinBlockState {

    @Inject(method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("RETURN"), cancellable = true)
    private void abysthea_phasingColisao(BlockGetter world, BlockPos pos, CollisionContext context,
                                         CallbackInfoReturnable<VoxelShape> cir) {
        VoxelShape shape = cir.getReturnValue();
        if (shape.isEmpty()) return;
        if (!(context instanceof EntityCollisionContext entityCtx)) return;

        Entity entity = entityCtx.getEntity();
        if (!(entity instanceof Player jogador)) return;

        if (abysthea_estaFaseando(jogador)) {
            boolean acima = abysthea_estaAcima(jogador, shape, pos);
            if (acima) {
                if (jogador.isShiftKeyDown() && jogador.onGround()) {
                    cir.setReturnValue(Shapes.empty());
                }
            } else {
                cir.setReturnValue(Shapes.empty());
            }
        }
    }

    @Unique
    private boolean abysthea_estaFaseando(Player jogador) {
        if (jogador.level().isClientSide) {
            return SlimePhaseCache.estaFaseando(jogador.getUUID());
        }
        String tag = AbystheaAddon.MODID + ":slime_phasing";
        return jogador.getPersistentData().getBoolean(tag);
    }

    @Unique
    private boolean abysthea_estaAcima(Entity entity, VoxelShape shape, BlockPos pos) {
        double topoBloco = pos.getY() + shape.max(Direction.Axis.Y);
        double tolerancia = entity.onGround() ? 8.05 / 16.0 : 0.0015;
        return entity.getY() > topoBloco - tolerancia;
    }
}
