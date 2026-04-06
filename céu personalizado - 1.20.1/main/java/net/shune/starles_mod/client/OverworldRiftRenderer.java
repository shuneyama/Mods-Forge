package net.shune.starles_mod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class OverworldRiftRenderer {

    private static final ResourceLocation SPACE_RIFT2 =
            new ResourceLocation("starles", "textures/environment/space_rift2.png");

    private static final float SKY_RADIUS = 100.0f;

    private static OverworldRiftRenderer INSTANCE;

    public static OverworldRiftRenderer get() {
        if (INSTANCE == null) INSTANCE = new OverworldRiftRenderer();
        return INSTANCE;
    }

    private OverworldRiftRenderer() {}

    public void render(PoseStack poseStack, Matrix4f projectionMatrix, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        long dayTime = mc.level.getDayTime() % 24000;
        float timeAlpha;
        if (dayTime < 4500) {
            timeAlpha = 1f;
        } else if (dayTime < 5000) {
            timeAlpha = 1f - (float)(dayTime - 4500) / 500f;
        } else if (dayTime < 7000) {
            timeAlpha = 0f;
        } else if (dayTime < 7500) {
            timeAlpha = (float)(dayTime - 7000) / 500f;
        } else {
            timeAlpha = 1f;
        }

        BlockPos playerPos = mc.player.blockPosition();
        Vec3 skyColor = mc.level.getSkyColor(playerPos.getCenter(), partialTick);

        float r = (float) skyColor.x;
        float g = (float) skyColor.y;
        float b = (float) skyColor.z;

        float luminance = r * 0.299f + g * 0.587f + b * 0.114f;

        float celestialAngle = mc.level.getSunAngle(partialTick);
        float sunAtZenith = (float) Math.sin(celestialAngle * Math.PI * 2.0);
        float sunFade = 1f - Math.max(0f, sunAtZenith * sunAtZenith * sunAtZenith);

        float alpha = 1f - Math.min(1f, luminance * 2.5f);
        alpha = Math.max(0.2f, alpha) * sunFade * timeAlpha;

        float brighten = 0.5f;
        float tr = r + (1f - r) * brighten;
        float tg = g + (1f - g) * brighten;
        float tb = b + (1f - b) * brighten;

        float blend = 1f - Math.min(1f, luminance * 3f);
        tr = tr + blend * (1f - tr);
        tg = tg + blend * (1f - tg);
        tb = tb + blend * (1f - tb);

        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, SPACE_RIFT2);
        RenderSystem.setShaderColor(tr, tg, tb, alpha);

        poseStack.pushPose();

        poseStack.mulPose(Axis.XP.rotationDegrees(90f));
        poseStack.translate(0, 0, -(SKY_RADIUS - 5));

        float size = 35f;

        BufferBuilder buf = new BufferBuilder(256);
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buf.vertex(poseStack.last().pose(), -size, -size, 0f).uv(0f, 1f).endVertex();
        buf.vertex(poseStack.last().pose(),  size, -size, 0f).uv(1f, 1f).endVertex();
        buf.vertex(poseStack.last().pose(),  size,  size, 0f).uv(1f, 0f).endVertex();
        buf.vertex(poseStack.last().pose(), -size,  size, 0f).uv(0f, 0f).endVertex();
        BufferUploader.drawWithShader(buf.end());

        poseStack.popPose();

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }

    public void close() {
        INSTANCE = null;
    }
}