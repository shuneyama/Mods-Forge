package com.voicecontrol;

import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import de.maxhenkel.voicechat.api.packets.MicrophonePacket;
import de.maxhenkel.voicechat.api.packets.StaticSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.UUID;

@ForgeVoicechatPlugin
public class VoiceBroadcastHandler implements VoicechatPlugin {

    private static VoicechatServerApi serverApi;

    @Override
    public String getPluginId() {
        return VoiceControlMod.MOD_ID + "_broadcast";
    }

    @Override
    public void initialize(VoicechatApi api) {
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted);
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket, 100);
    }

    private void onServerStarted(VoicechatServerStartedEvent event) {
        serverApi = event.getVoicechat();
    }

    private void onMicrophonePacket(MicrophonePacketEvent event) {
        if (serverApi == null) return;
        if (event.getSenderConnection() == null) return;
        if (event.getSenderConnection().getPlayer() == null) return;

        UUID senderUUID = event.getSenderConnection().getPlayer().getUuid();

        if (!MuteManager.isBroadcasting(senderUUID)) return;
        if (!MuteManager.canPlayerSpeak(senderUUID)) return;

        MicrophonePacket packet = event.getPacket();
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        for (ServerPlayer mcPlayer : server.getPlayerList().getPlayers()) {
            UUID receiverUUID = mcPlayer.getUUID();

            if (receiverUUID.equals(senderUUID)) continue;

            try {
                VoicechatConnection receiverConnection = serverApi.getConnectionOf(receiverUUID);
                if (receiverConnection == null) continue;

                UUID channelId = createChannelId(senderUUID, receiverUUID);

                StaticSoundPacket soundPacket = packet.staticSoundPacketBuilder()
                        .channelId(channelId)
                        .build();

                serverApi.sendStaticSoundPacketTo(receiverConnection, soundPacket);

            } catch (Exception e) {
                VoiceControlMod.LOGGER.error("Erro ao enviar broadcast para " + receiverUUID + ": " + e.getMessage());
            }
        }
    }

    private UUID createChannelId(UUID sender, UUID receiver) {
        long msb = sender.getMostSignificantBits() ^ receiver.getMostSignificantBits();
        long lsb = sender.getLeastSignificantBits() ^ receiver.getLeastSignificantBits();
        return new UUID(msb, lsb);
    }
}