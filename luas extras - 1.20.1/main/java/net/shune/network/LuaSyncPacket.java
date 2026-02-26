package net.shune.network;

import net.shune.LuasMod.TipoLua;
import net.shune.client.LuaClientRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class LuaSyncPacket {
    
    private final TipoLua tipoLua;
    
    public LuaSyncPacket(TipoLua tipoLua) {
        this.tipoLua = tipoLua;
    }
    
    public static void encode(LuaSyncPacket packet, FriendlyByteBuf buf) {
        buf.writeEnum(packet.tipoLua);
    }
    
    public static LuaSyncPacket decode(FriendlyByteBuf buf) {
        return new LuaSyncPacket(buf.readEnum(TipoLua.class));
    }
    
    public static void handle(LuaSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                LuaClientRenderer.luaCliente = packet.tipoLua;
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
