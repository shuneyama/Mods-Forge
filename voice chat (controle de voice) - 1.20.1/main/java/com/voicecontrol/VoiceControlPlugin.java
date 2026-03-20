package com.voicecontrol;

import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;

import java.util.UUID;

@ForgeVoicechatPlugin
public class VoiceControlPlugin implements VoicechatPlugin {

    public static VoicechatServerApi serverApi;

    @Override
    public String getPluginId() {
        return VoiceControlMod.MOD_ID;
    }

    @Override
    public void initialize(VoicechatApi api) {
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted);
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket);
    }

    private void onServerStarted(VoicechatServerStartedEvent event) {
        serverApi = event.getVoicechat();
    }

    private void onMicrophonePacket(MicrophonePacketEvent event) {
        if (event.getSenderConnection() == null) return;
        if (event.getSenderConnection().getPlayer() == null) return;

        UUID senderUUID = event.getSenderConnection().getPlayer().getUuid();

        if (!MuteManager.canPlayerSpeak(senderUUID)) {
            event.cancel();
        }
    }
}