package net.shune.starles_mod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class VazioSkyRenderer {

    private static final ResourceLocation[] GALAXIES = {
            new ResourceLocation("starles", "textures/environment/galaxy_1.png"),
            new ResourceLocation("starles", "textures/environment/galaxy_2.png"),
            new ResourceLocation("starles", "textures/environment/galaxy_3.png")
    };

    private static final ResourceLocation[] SKY_OBJECTS = {
            new ResourceLocation("starles", "textures/environment/sky_2.png"),
            new ResourceLocation("starles", "textures/environment/sky_3.png"),
            new ResourceLocation("starles", "textures/environment/sky_4.png"),
            new ResourceLocation("starles", "textures/environment/sky_5.png"),
            new ResourceLocation("starles", "textures/environment/sky_6.png"),
            new ResourceLocation("starles", "textures/environment/sky_7.png"),
            new ResourceLocation("starles", "textures/environment/sky_8.png")
    };

    private static final ResourceLocation SUN_TEXTURE = new ResourceLocation("minecraft", "textures/environment/sun.png");

    private static final ResourceLocation SPACE_RIFT = new ResourceLocation("starles", "textures/environment/space_rift.png");

    private static final float SKY_RADIUS = 100.0f;

    private VertexBuffer starBuffer;
    private boolean starsReady = false;

    private final List<SkyObjectData> skyObjects = new ArrayList<>();
    private boolean skyObjectsReady = false;

    private static VazioSkyRenderer INSTANCE;
    public static VazioSkyRenderer get() {
        if (INSTANCE == null) INSTANCE = new VazioSkyRenderer();
        return INSTANCE;
    }
    private VazioSkyRenderer() {}

    public void render(PoseStack poseStack, Matrix4f projectionMatrix, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        ensureStars();
        ensureSkyObjects();

        float skyRotation = mc.level.getTimeOfDay(partialTick) * 360.0f * 0.1f;

        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        renderStars(poseStack, projectionMatrix, skyRotation);
        renderSpaceRift(poseStack, projectionMatrix, skyRotation);
        renderSun(poseStack, projectionMatrix);
        renderSkyObjects(poseStack, projectionMatrix, skyRotation);

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }

    private void renderSun(PoseStack poseStack, Matrix4f projectionMatrix) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, SUN_TEXTURE);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(180f));
        poseStack.mulPose(Axis.XP.rotationDegrees(35f));
        poseStack.translate(0, 0, -SKY_RADIUS + 5);

        float size = 20f;

        BufferBuilder buf = new BufferBuilder(256);
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buf.vertex(poseStack.last().pose(), -size, -size, 0f).uv(0f, 1f).endVertex();
        buf.vertex(poseStack.last().pose(),  size, -size, 0f).uv(1f, 1f).endVertex();
        buf.vertex(poseStack.last().pose(),  size,  size, 0f).uv(1f, 0f).endVertex();
        buf.vertex(poseStack.last().pose(), -size,  size, 0f).uv(0f, 0f).endVertex();
        BufferUploader.drawWithShader(buf.end());

        poseStack.popPose();
    }

    private void ensureStars() {
        if (starsReady) return;
        starsReady = true;

        if (starBuffer != null) starBuffer.close();
        starBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);

        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        RandomSource rng = RandomSource.create(10842L);
        for (int i = 0; i < 2500; i++) {
            double d0 = rng.nextFloat() * 2f - 1f;
            double d1 = rng.nextFloat() * 2f - 1f;
            double d2 = rng.nextFloat() * 2f - 1f;
            double d3 = 0.15f + rng.nextFloat() * 0.15f;
            double d4 = d0 * d0 + d1 * d1 + d2 * d2;
            if (d4 >= 1.0 || d4 <= 0.01) continue;
            d4 = 1.0 / Math.sqrt(d4);
            d0 *= d4; d1 *= d4; d2 *= d4;
            double d5 = d0 * 100, d6 = d1 * 100, d7 = d2 * 100;
            double d8 = Math.atan2(d0, d2), d9 = Math.sin(d8), d10 = Math.cos(d8);
            double d11 = Math.atan2(Math.sqrt(d0 * d0 + d2 * d2), d1);
            double d12 = Math.sin(d11), d13 = Math.cos(d11);
            double d14 = rng.nextDouble() * Math.PI * 2, d15 = Math.sin(d14), d16 = Math.cos(d14);
            int r, g, b;
            float cc = rng.nextFloat();
            if      (cc < 0.40f) { r = 200 + rng.nextInt(55); g = 200 + rng.nextInt(55); b = 255; }
            else if (cc < 0.70f) { r = 180 + rng.nextInt(75); g = 150 + rng.nextInt(75); b = 255; }
            else if (cc < 0.85f) { r = 255; g = 255; b = 200 + rng.nextInt(55); }
            else                 { r = 255; g = 255; b = 255; }
            int alpha;
            float ac = rng.nextFloat();
            if      (ac < 0.20f) alpha = 30  + rng.nextInt(30);
            else if (ac < 0.50f) alpha = 60  + rng.nextInt(50);
            else if (ac < 0.80f) alpha = 110 + rng.nextInt(50);
            else                 alpha = 180 + rng.nextInt(75);
            for (int j = 0; j < 4; j++) {
                double d18 = ((j & 2) - 1) * d3, d19 = ((j + 1 & 2) - 1) * d3;
                double d21 = d18 * d16 - d19 * d15, d22 = d19 * d16 + d18 * d15;
                double d23 = d21 * d12, d24 = -d21 * d13;
                double d25 = d24 * d9 - d22 * d10, d26 = d22 * d9 + d24 * d10;
                buf.vertex((float)(d5+d25),(float)(d6+d23),(float)(d7+d26)).color(r,g,b,alpha).endVertex();
            }
        }
        starBuffer.bind();
        starBuffer.upload(buf.end());
        VertexBuffer.unbind();
    }

    private void renderStars(PoseStack poseStack, Matrix4f projectionMatrix, float skyRotation) {
        if (starBuffer == null) return;
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-90f));
        poseStack.mulPose(Axis.XP.rotationDegrees(skyRotation));
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        starBuffer.bind();
        starBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, GameRenderer.getPositionColorShader());
        VertexBuffer.unbind();
        poseStack.popPose();
    }

    private void renderSpaceRift(PoseStack poseStack, Matrix4f projectionMatrix, float skyRotation) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, SPACE_RIFT);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-90f));
        poseStack.mulPose(Axis.XP.rotationDegrees(skyRotation));

        poseStack.mulPose(Axis.XP.rotationDegrees(75f));
        poseStack.translate(0, 0, -SKY_RADIUS + 5);

        poseStack.mulPose(Axis.ZP.rotationDegrees(25f));

        float size = 35f;

        BufferBuilder buf = new BufferBuilder(256);
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buf.vertex(poseStack.last().pose(), -size, -size, 0f).uv(0f, 1f).endVertex();
        buf.vertex(poseStack.last().pose(),  size, -size, 0f).uv(1f, 1f).endVertex();
        buf.vertex(poseStack.last().pose(),  size,  size, 0f).uv(1f, 0f).endVertex();
        buf.vertex(poseStack.last().pose(), -size,  size, 0f).uv(0f, 0f).endVertex();
        BufferUploader.drawWithShader(buf.end());

        poseStack.popPose();
    }

    private void ensureSkyObjects() {
        if (skyObjectsReady) return;
        skyObjectsReady = true;

        RandomSource rng = RandomSource.create(99999L);

        int lastGalaxyIndex = -1;
        int lastSkyIndex = -1;

        for (int i = 0; i < 5; i++) {
            double theta, phi, x, y, z;
            int textureIndex;

            int attempts = 0;
            do {
                theta = (i / 5.0) * Math.PI * 2.0 + (rng.nextDouble() - 0.5) * 0.5;
                phi = (rng.nextDouble() - 0.3) * Math.PI * 0.35;

                x = Math.cos(phi) * Math.cos(theta);
                y = Math.sin(phi);
                z = Math.cos(phi) * Math.sin(theta);

                do {
                    textureIndex = rng.nextInt(GALAXIES.length);
                } while (textureIndex == lastGalaxyIndex && GALAXIES.length > 1);

                attempts++;
            } while (isTooCloseToAny(x, y, z, 0.5) && attempts < 30);

            lastGalaxyIndex = textureIndex;

            float size = 10f + rng.nextFloat() * 12f;
            float tiltX = (rng.nextFloat() - 0.5f) * 30f;
            float tiltY = rng.nextFloat() * 360f;

            skyObjects.add(new SkyObjectData(x, y, z, size, tiltX, tiltY, GALAXIES[textureIndex]));
        }

        for (int i = 0; i < 1; i++) {
            double theta, phi, x, y, z;
            int textureIndex;

            int attempts = 0;
            do {
                theta = Math.PI + rng.nextDouble() * Math.PI;
                phi = 0.2 + rng.nextDouble() * 0.25;

                x = Math.cos(phi) * Math.cos(theta);
                y = Math.sin(phi);
                z = Math.cos(phi) * Math.sin(theta);

                do {
                    textureIndex = rng.nextInt(GALAXIES.length);
                } while (textureIndex == lastGalaxyIndex && GALAXIES.length > 1);

                attempts++;
            } while (isTooCloseToAny(x, y, z, 0.5) && attempts < 30);

            lastGalaxyIndex = textureIndex;

            float size = 8f + rng.nextFloat() * 10f;
            float tiltX = (rng.nextFloat() - 0.5f) * 30f;
            float tiltY = rng.nextFloat() * 360f;

            skyObjects.add(new SkyObjectData(x, y, z, size, tiltX, tiltY, GALAXIES[textureIndex]));
        }

        for (int i = 0; i < 14; i++) {
            double theta, phi, x, y, z;
            int textureIndex;

            int attempts = 0;
            do {
                theta = rng.nextDouble() * Math.PI * 2.0;
                phi = rng.nextDouble() * Math.PI * 0.35;

                x = Math.cos(phi) * Math.cos(theta);
                y = Math.sin(phi);
                z = Math.cos(phi) * Math.sin(theta);

                do {
                    textureIndex = rng.nextInt(SKY_OBJECTS.length);
                } while (textureIndex == lastSkyIndex && SKY_OBJECTS.length > 1);

                attempts++;
            } while (isTooCloseToAny(x, y, z, 0.3) && attempts < 30);

            lastSkyIndex = textureIndex;

            float size = 2f + rng.nextFloat() * 3f;
            float tiltX = (rng.nextFloat() - 0.5f) * 20f;
            float tiltY = rng.nextFloat() * 360f;

            skyObjects.add(new SkyObjectData(x, y, z, size, tiltX, tiltY, SKY_OBJECTS[textureIndex]));
        }

        for (int i = 0; i < 1; i++) {
            double theta, phi, x, y, z;
            int textureIndex;

            int attempts = 0;
            do {
                theta = Math.PI + rng.nextDouble() * Math.PI;
                phi = 0.25 + rng.nextDouble() * 0.2;

                x = Math.cos(phi) * Math.cos(theta);
                y = Math.sin(phi);
                z = Math.cos(phi) * Math.sin(theta);

                do {
                    textureIndex = rng.nextInt(SKY_OBJECTS.length);
                } while (textureIndex == lastSkyIndex && SKY_OBJECTS.length > 1);

                attempts++;
            } while (isTooCloseToAny(x, y, z, 0.3) && attempts < 30);

            lastSkyIndex = textureIndex;

            float size = 1.5f + rng.nextFloat() * 2.5f;
            float tiltX = (rng.nextFloat() - 0.5f) * 20f;
            float tiltY = rng.nextFloat() * 360f;

            skyObjects.add(new SkyObjectData(x, y, z, size, tiltX, tiltY, SKY_OBJECTS[textureIndex]));
        }
    }

    private boolean isTooCloseToAny(double x, double y, double z, double minDistance) {
        for (SkyObjectData obj : skyObjects) {
            double dx = obj.x - x;
            double dy = obj.y - y;
            double dz = obj.z - z;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (dist < minDistance) {
                return true;
            }
        }
        return false;
    }

    private void renderSkyObjects(PoseStack poseStack, Matrix4f projectionMatrix, float skyRotation) {
        BufferBuilder buf = new BufferBuilder(256);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        for (SkyObjectData obj : skyObjects) {
            RenderSystem.setShaderTexture(0, obj.texture);

            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(-90f));
            poseStack.mulPose(Axis.XP.rotationDegrees(skyRotation));

            float yaw = (float) Math.toDegrees(Math.atan2(obj.x, obj.z));
            float pitch = (float) Math.toDegrees(Math.asin(Math.max(-1.0, Math.min(1.0, obj.y))));

            poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
            poseStack.translate(0, 0, -SKY_RADIUS);

            poseStack.mulPose(Axis.XP.rotationDegrees(obj.tiltX));
            poseStack.mulPose(Axis.ZP.rotationDegrees(obj.tiltY));

            float s = obj.size;
            buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            buf.vertex(poseStack.last().pose(), -s, -s, 0f).uv(0f, 1f).endVertex();
            buf.vertex(poseStack.last().pose(),  s, -s, 0f).uv(1f, 1f).endVertex();
            buf.vertex(poseStack.last().pose(),  s,  s, 0f).uv(1f, 0f).endVertex();
            buf.vertex(poseStack.last().pose(), -s,  s, 0f).uv(0f, 0f).endVertex();

            BufferUploader.drawWithShader(buf.end());

            poseStack.popPose();
        }

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public void close() {
        if (starBuffer != null) { starBuffer.close(); starBuffer = null; }
        starsReady = false; skyObjectsReady = false; skyObjects.clear(); INSTANCE = null;
    }

    private static final class SkyObjectData {
        final double x, y, z;
        final float size, tiltX, tiltY;
        final ResourceLocation texture;
        SkyObjectData(double x, double y, double z, float size, float tiltX, float tiltY, ResourceLocation texture) {
            this.x = x; this.y = y; this.z = z;
            this.size = size; this.tiltX = tiltX; this.tiltY = tiltY; this.texture = texture;
        }
    }
}