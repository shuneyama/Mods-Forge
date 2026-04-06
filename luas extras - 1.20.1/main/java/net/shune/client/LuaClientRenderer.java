package net.shune.client;

import net.shune.LuasMod;
import net.shune.LuasMod.TipoLua;
import net.shune.network.LuaSyncPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = LuasMod.MODID, value = Dist.CLIENT)
public class LuaClientRenderer {

    public static TipoLua luaCliente = TipoLua.NORMAL;

    public static float[] getCorLua() {
        return switch (luaCliente) {
            case DEUSA -> new float[]{1.0f, 0.9f, 0.3f};
            case SORTE -> new float[]{0.3f, 0.5f, 1.0f};
            case VERMELHA -> new float[]{1.0f, 0.2f, 0.2f};
            case SUPER_VERMELHA -> new float[]{0.8f, 0.0f, 0.0f};
            default -> new float[]{1.0f, 1.0f, 1.0f};
        };
    }

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        
        if (level == null) return;
        if (luaCliente == TipoLua.NORMAL) return;

        long tempo = level.getDayTime() % 24000;
        if (tempo < 13000 || tempo > 23000) return;

        float intensidade = calcularIntensidadeNoite(tempo);

        float[] corFog = switch (luaCliente) {
            case DEUSA -> new float[]{0.15f, 0.12f, 0.02f};
            case SORTE -> new float[]{0.02f, 0.05f, 0.15f};
            case VERMELHA -> new float[]{0.15f, 0.02f, 0.02f};
            case SUPER_VERMELHA -> new float[]{0.2f, 0.0f, 0.0f};
            default -> new float[]{0.0f, 0.0f, 0.0f};
        };

        float r = lerp((float) event.getRed(), corFog[0], intensidade * 0.6f);
        float g = lerp((float) event.getGreen(), corFog[1], intensidade * 0.6f);
        float b = lerp((float) event.getBlue(), corFog[2], intensidade * 0.6f);
        
        event.setRed(r);
        event.setGreen(g);
        event.setBlue(b);
    }

    private static float calcularIntensidadeNoite(long tempo) {
        if (tempo >= 13000 && tempo <= 14000) {
            return (tempo - 13000) / 1000.0f;
        }
        else if (tempo >= 22000 && tempo <= 23000) {
            return 1.0f - ((tempo - 22000) / 1000.0f);
        }
        else if (tempo > 14000 && tempo < 22000) {
            return 1.0f;
        }
        return 0.0f;
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
