package com.abysthea.addon.mixin;

import com.abysthea.addon.AbystheaAddon;
import com.abysthea.addon.cliente.SlimePhaseCache;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @Inject(method = "checkInsideBlocks", at = @At("HEAD"), cancellable = true)
    private void abysthea_ignorarColisaoBloco(CallbackInfo ci) {
        if (abysthea_estaFaseando()) {
            ci.cancel();
        }
    }

    @Inject(method = "isInWall", at = @At("HEAD"), cancellable = true)
    private void abysthea_semSufocamento(CallbackInfoReturnable<Boolean> cir) {
        if (abysthea_estaFaseando()) {
            cir.setReturnValue(false);
        }
    }

    @Unique
    private boolean abysthea_estaFaseando() {
        if (!((Object) this instanceof Player jogador)) return false;
        if (jogador.level().isClientSide) {
            return SlimePhaseCache.estaFaseando(jogador.getUUID());
        }
        String tag = AbystheaAddon.MODID + ":slime_phasing";
        return jogador.getPersistentData().getBoolean(tag);
    }
}
