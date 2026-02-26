package net.shune.network;

import net.shune.LuasMod;
import net.shune.LuasMod.TipoLua;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class LuaNetworkHandler {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(LuasMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        INSTANCE.registerMessage(
                packetId++,
                LuaSyncPacket.class,
                LuaSyncPacket::encode,
                LuaSyncPacket::decode,
                LuaSyncPacket::handle
        );
    }

    public static void syncToAll(TipoLua lua) {
        INSTANCE.send(
                PacketDistributor.ALL.noArg(),
                new LuaSyncPacket(lua)
        );
    }

    public static void syncToPlayer(ServerPlayer player, TipoLua lua) {
        INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> player),
                new LuaSyncPacket(lua)
        );
    }
}