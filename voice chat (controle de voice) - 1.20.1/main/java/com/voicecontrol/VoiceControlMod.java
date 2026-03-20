package com.voicecontrol;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(VoiceControlMod.MOD_ID)
public class VoiceControlMod {
    public static final String MOD_ID = "voicecontrole";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public VoiceControlMod() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        VoiceControlCommands.register(event.getDispatcher());
    }
}