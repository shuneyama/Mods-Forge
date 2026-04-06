package com.abysthea.addon.rede;

import com.abysthea.addon.AbystheaAddon;
import com.abysthea.addon.cliente.SlimePhaseCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.UUID;
import java.util.function.Supplier;

public class RedeAbysthea {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(AbystheaAddon.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void registrar() {
        CHANNEL.registerMessage(packetId++, PacoteSyncSlimePhase.class,
                PacoteSyncSlimePhase::encode,
                PacoteSyncSlimePhase::decode,
                PacoteSyncSlimePhase::handle);
    }

    public static void enviarSlimePhase(ServerPlayer jogador, boolean ativo) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> jogador),
                new PacoteSyncSlimePhase(jogador.getUUID(), ativo));
    }

    public static class PacoteSyncSlimePhase {
        private final UUID uuid;
        private final boolean ativo;

        public PacoteSyncSlimePhase(UUID uuid, boolean ativo) {
            this.uuid = uuid;
            this.ativo = ativo;
        }

        public static void encode(PacoteSyncSlimePhase msg, FriendlyByteBuf buf) {
            buf.writeUUID(msg.uuid);
            buf.writeBoolean(msg.ativo);
        }

        public static PacoteSyncSlimePhase decode(FriendlyByteBuf buf) {
            return new PacoteSyncSlimePhase(buf.readUUID(), buf.readBoolean());
        }

        public static void handle(PacoteSyncSlimePhase msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                SlimePhaseCache.definir(msg.uuid, msg.ativo);
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
