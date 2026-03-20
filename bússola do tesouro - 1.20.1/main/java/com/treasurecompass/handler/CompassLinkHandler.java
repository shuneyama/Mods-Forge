package com.treasurecompass.handler;

import com.mojang.logging.LogUtils;
import com.treasurecompass.client.TreasureMapUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;

public class CompassLinkHandler {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    
    @SubscribeEvent
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        Level level = event.getLevel();

        if (level.isClientSide()) {
            return;
        }
        
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        ItemStack mapStack = null;
        ItemStack compassStack = null;
        InteractionHand compassHand = null;
        
        if (isTreasureMap(mainHand, level) && isCompass(offHand)) {
            mapStack = mainHand;
            compassStack = offHand;
            compassHand = InteractionHand.OFF_HAND;
        } else if (isTreasureMap(offHand, level) && isCompass(mainHand)) {
            mapStack = offHand;
            compassStack = mainHand;
            compassHand = InteractionHand.MAIN_HAND;
        }

        if (mapStack == null || compassStack == null) {
            return;
        }

        BlockPos mapCenter = TreasureMapUtils.getMapCenterPosition(mapStack, level);
        
        if (mapCenter == null) {
            player.displayClientMessage(
                Component.literal("§cNão foi possível ler as coordenadas do mapa!"),
                true
            );
            return;
        }

        linkCompassToPosition(compassStack, mapCenter, level);

        if (compassHand == InteractionHand.MAIN_HAND) {
            player.setItemInHand(InteractionHand.MAIN_HAND, compassStack);
        } else {
            player.setItemInHand(InteractionHand.OFF_HAND, compassStack);
        }

        double distance = calculateDistance(player.getX(), player.getZ(), mapCenter.getX(), mapCenter.getZ());
        int distanceRounded = (int) Math.round(distance);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 1.0F, 1.0F);
        
        player.displayClientMessage(
            Component.literal("§aBússola vinculada ao mapa do tesouro atual, sua distância média ao centro da área é de §e" +
                    distanceRounded + " blocos"), 
            true
        );
        
        LOGGER.info("Compass linked to map center: {} for player {}", mapCenter, player.getName().getString());

        event.setCanceled(true);
    }

    private boolean isTreasureMap(ItemStack stack, Level level) {
        return TreasureMapUtils.isTreasureMap(stack, level);
    }

    private boolean isCompass(ItemStack stack) {
        return stack.getItem() == Items.COMPASS;
    }

    private void linkCompassToPosition(ItemStack compass, BlockPos target, Level level) {
        CompoundTag tag = compass.getOrCreateTag();

        CompoundTag lodestonePos = new CompoundTag();
        lodestonePos.putInt("X", target.getX());
        lodestonePos.putInt("Y", target.getY());
        lodestonePos.putInt("Z", target.getZ());

        tag.put("LodestonePos", lodestonePos);
        tag.putString("LodestoneDimension", level.dimension().location().toString());
        tag.putBoolean("LodestoneTracked", false);

        tag.putBoolean("TreasureCompass", true);
        tag.putInt("TreasureTargetX", target.getX());
        tag.putInt("TreasureTargetZ", target.getZ());
    }
    
    private double calculateDistance(double x1, double z1, int x2, int z2) {
        double dx = x2 - x1;
        double dz = z2 - z1;
        return Math.sqrt(dx * dx + dz * dz);
    }
}
