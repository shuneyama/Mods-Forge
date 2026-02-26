package net.shune.mixin;

import net.shune.client.LuaClientRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LuaSkyMixin {

    @Inject(
        method = "renderSky",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V",
            ordinal = 1
        )
    )
    private void luasmod_beforeMoonRender(PoseStack poseStack, Matrix4f projectionMatrix, 
                                          float partialTick, Camera camera,
                                          boolean isFoggy, Runnable setupFog, 
                                          CallbackInfo ci) {
        float[] cor = LuaClientRenderer.getCorLua();
        RenderSystem.setShaderColor(cor[0], cor[1], cor[2], 1.0f);
    }

    @Inject(
        method = "renderSky",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/BufferUploader;drawWithShader(Lcom/mojang/blaze3d/vertex/BufferBuilder$RenderedBuffer;)V",
            ordinal = 1
        )
    )
    private void luasmod_afterMoonRender(PoseStack poseStack, Matrix4f projectionMatrix,
                                         float partialTick, Camera camera,
                                         boolean isFoggy, Runnable setupFog,
                                         CallbackInfo ci) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
