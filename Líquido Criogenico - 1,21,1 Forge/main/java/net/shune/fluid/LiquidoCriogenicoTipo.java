package net.shune.fluid;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;
import org.joml.Vector3f;

import java.util.function.Consumer;

public class LiquidoCriogenicoTipo extends FluidType {
    private static final ResourceLocation STILL_TEXTURE = new ResourceLocation("criogeniakorps", "block/criogenico_fonte");
    private static final ResourceLocation FLOW_TEXTURE = new ResourceLocation("criogeniakorps", "block/criogenico_correnteza");
    private static final ResourceLocation OVERLAY_TEXTURE = new ResourceLocation("criogeniakorps", "block/criogenico_sobreposicao");

    public LiquidoCriogenicoTipo(Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return STILL_TEXTURE;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return FLOW_TEXTURE;
            }

            @Override
            public ResourceLocation getOverlayTexture() {
                return OVERLAY_TEXTURE;
            }

            @Override
            public int getTintColor() {
                return 0x4000E5CC;
            }

            @Override
            public Vector3f modifyFogColor(net.minecraft.client.Camera camera, float partialTick,
                                           net.minecraft.client.multiplayer.ClientLevel level,
                                           int renderDistance, float darkenWorldAmount,
                                           Vector3f fluidFogColor) {
                return new Vector3f(0.0f, 0.9f, 1.0f);
            }
        });
    }
}