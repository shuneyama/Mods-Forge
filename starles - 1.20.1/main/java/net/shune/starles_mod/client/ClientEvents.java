package net.shune.starles_mod.client;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shune.starles_mod.StarlesMod;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid = StarlesMod.MODID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onRenderSky(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (mc.level.dimension() != StarlesMod.VAZIO_LEVEL) return;

        RenderSystem.clearColor(0f, 0f, 0f, 1f);
        RenderSystem.clear(org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT, Minecraft.ON_OSX);

        PoseStack poseStack       = event.getPoseStack();
        Matrix4f  projectionMatrix = event.getProjectionMatrix();
        float     partialTick     = event.getPartialTick();

        VazioSkyRenderer.get().render(poseStack, projectionMatrix, partialTick);
    }

    @SubscribeEvent
    public static void onRenderOverworldRift(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (mc.level.dimension() != Level.OVERWORLD) return;

        PoseStack poseStack        = event.getPoseStack();
        Matrix4f  projectionMatrix = event.getProjectionMatrix();
        float     partialTick      = event.getPartialTick();

        OverworldRiftRenderer.get().render(poseStack, projectionMatrix, partialTick);
    }

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.level.dimension() == StarlesMod.VAZIO_LEVEL) {
            event.setRed(0f);
            event.setGreen(0f);
            event.setBlue(0f);
        }
    }

    @SubscribeEvent
    public static void onFogDensity(ViewportEvent.RenderFog event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.level.dimension() == StarlesMod.VAZIO_LEVEL) {
            event.setNearPlaneDistance(20f);
            event.setFarPlaneDistance(100f);
            event.setFogShape(FogShape.SPHERE);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (mc.level.dimension() != StarlesMod.VAZIO_LEVEL) return;
        if (mc.isPaused()) return;

        RandomSource random = mc.level.random;
        if (random.nextFloat() < 0.15f) {
            double px = mc.player.getX() + (random.nextDouble() - 0.5) * 64;
            double py = mc.player.getY() + (random.nextDouble() - 0.5) * 32;
            double pz = mc.player.getZ() + (random.nextDouble() - 0.5) * 64;

            if (random.nextBoolean()) {
                mc.level.addParticle(ParticleTypes.END_ROD,
                        px, py, pz,
                        (random.nextDouble() - 0.5) * 0.01,
                        (random.nextDouble() - 0.5) * 0.01,
                        (random.nextDouble() - 0.5) * 0.01);
            } else {
                mc.level.addParticle(ParticleTypes.WITCH,
                        px, py, pz,
                        (random.nextDouble() - 0.5) * 0.01,
                        (random.nextDouble() - 0.5) * 0.01,
                        (random.nextDouble() - 0.5) * 0.01);
            }
        }
    }

    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening event) {
        if (event.getScreen() instanceof BackupConfirmScreen) return;
        if (event.getScreen() instanceof net.minecraft.client.gui.screens.ConfirmScreen confirmScreen) {
            String title = confirmScreen.getTitle().getString().toLowerCase();
            if (title.contains("experimental")) {
                Minecraft.getInstance().setScreen(null);
                try {
                    var field = net.minecraft.client.gui.screens.ConfirmScreen.class.getDeclaredField("callback");
                    field.setAccessible(true);
                    var callback = (it.unimi.dsi.fastutil.booleans.BooleanConsumer) field.get(confirmScreen);
                    callback.accept(true);
                } catch (Exception ignored) {}
            }
        }
    }
}