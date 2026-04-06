package com.treasurecompass.client;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import javax.annotation.Nullable;

public class TreasureMapUtils {

    public static boolean isTreasureMap(ItemStack stack, Level level) {
        if (stack.isEmpty() || stack.getItem() != Items.FILLED_MAP) {
            return false;
        }

        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Decorations", Tag.TAG_LIST)) {
            ListTag decorations = tag.getList("Decorations", Tag.TAG_COMPOUND);
            for (int i = 0; i < decorations.size(); i++) {
                CompoundTag decoration = decorations.getCompound(i);
                if (decoration.contains("type")) {
                    byte type = decoration.getByte("type");
                    if (type == 26 || type == 8 || type == 9) {
                        return true;
                    }
                }
            }
        }

        MapItemSavedData mapData = MapItem.getSavedData(stack, level);
        if (mapData != null) {
            for (MapDecoration decoration : mapData.getDecorations()) {
                MapDecoration.Type type = decoration.getType();
                if (type == MapDecoration.Type.RED_X || 
                    type == MapDecoration.Type.MANSION || 
                    type == MapDecoration.Type.MONUMENT) {
                    return true;
                }
            }
        }

        String itemName = stack.getHoverName().getString().toLowerCase();
        return itemName.contains("treasure") || itemName.contains("tesouro") || 
               itemName.contains("buried") || itemName.contains("explorer") ||
               itemName.contains("explorador") || itemName.contains("mansion") ||
               itemName.contains("monument");
    }

    @Nullable
    public static BlockPos getMapCenterPosition(ItemStack stack, Level level) {
        if (stack.isEmpty() || stack.getItem() != Items.FILLED_MAP) {
            return null;
        }

        MapItemSavedData mapData = MapItem.getSavedData(stack, level);
        if (mapData != null && (mapData.centerX != 0 || mapData.centerZ != 0)) {
            BlockPos center = new BlockPos(mapData.centerX, 64, mapData.centerZ);
            return center;
        }

        BlockPos treasurePos = getTreasureIconPosition(stack, level);
        if (treasurePos != null) {
            int scale = 1;
            if (mapData != null) {
                scale = mapData.scale;
            }

            BlockPos mapCenter = calculateMapCenter(treasurePos.getX(), treasurePos.getZ(), scale);
            return mapCenter;
        }
        
        return null;
    }

    private static BlockPos calculateMapCenter(int x, int z, int scale) {
        int mapSize = 128 * (1 << scale);

        int offset = 64;

        int centerX = (int) (Math.floor((double)(x + offset) / mapSize) * mapSize + mapSize / 2 - offset);
        int centerZ = (int) (Math.floor((double)(z + offset) / mapSize) * mapSize + mapSize / 2 - offset);
        
        return new BlockPos(centerX, 64, centerZ);
    }

    @Nullable
    public static BlockPos getTreasureIconPosition(ItemStack stack, Level level) {
        if (stack.isEmpty() || stack.getItem() != Items.FILLED_MAP) {
            return null;
        }

        BlockPos nbtPos = getPositionFromItemNBT(stack);
        if (nbtPos != null) {
            return nbtPos;
        }

        BlockPos mapDataPos = getPositionFromMapDataDecorations(stack, level);
        if (mapDataPos != null) {
            return mapDataPos;
        }
        
        return null;
    }

    @Nullable
    private static BlockPos getPositionFromItemNBT(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("Decorations", Tag.TAG_LIST)) {
            return null;
        }
        
        ListTag decorations = tag.getList("Decorations", Tag.TAG_COMPOUND);
        
        for (int i = 0; i < decorations.size(); i++) {
            CompoundTag decoration = decorations.getCompound(i);
            
            if (!decoration.contains("type")) {
                continue;
            }
            
            byte type = decoration.getByte("type");

            if (type == 26 || type == 8 || type == 9) {
                if (decoration.contains("x") && decoration.contains("z")) {
                    double x = decoration.getDouble("x");
                    double z = decoration.getDouble("z");
                    return new BlockPos((int) x, 64, (int) z);
                }
            }
        }
        
        return null;
    }

    @Nullable
    private static BlockPos getPositionFromMapDataDecorations(ItemStack stack, Level level) {
        MapItemSavedData mapData = MapItem.getSavedData(stack, level);
        if (mapData == null) {
            return null;
        }
        
        for (MapDecoration decoration : mapData.getDecorations()) {
            MapDecoration.Type type = decoration.getType();
            
            if (type == MapDecoration.Type.RED_X || 
                type == MapDecoration.Type.MANSION || 
                type == MapDecoration.Type.MONUMENT) {

                int scale = 1 << mapData.scale;
                int worldX = mapData.centerX + (decoration.getX() * scale);
                int worldZ = mapData.centerZ + (decoration.getY() * scale);
                
                return new BlockPos(worldX, 64, worldZ);
            }
        }
        
        return null;
    }
}
