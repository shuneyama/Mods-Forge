package net.shune.starles_mod.client;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

@OnlyIn(Dist.CLIENT)
public class VazioDimensionEffects extends DimensionSpecialEffects {

    public VazioDimensionEffects() {
        super(Float.NaN, true, SkyType.NONE, false, false);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float sunHeight) {
        return new Vec3(0.0, 0.0, 0.0);
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return false;
    }

    @Nullable
    @Override
    public float[] getSunriseColor(float timeOfDay, float partialTicks) {
        return null;
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick,
                             PoseStack poseStack, Camera camera, Matrix4f projectionMatrix,
                             boolean isFoggy, Runnable setupFog) {
        return false;
    }

    @Override
    public boolean renderClouds(ClientLevel level, int ticks, float partialTick,
                                PoseStack poseStack, double camX, double camY, double camZ,
                                Matrix4f projectionMatrix) {
        return true;
    }
}