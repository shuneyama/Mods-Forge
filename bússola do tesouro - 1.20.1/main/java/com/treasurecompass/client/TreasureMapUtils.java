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

/**
 * Utilitário para detectar mapas de tesouro e extrair informações de localização.
 * 
 * Existem DUAS posições diferentes:
 * 1. Centro da região do mapa - O ponto central da área 256x256 (ou outra escala) que o mapa mostra
 * 2. Posição do X/tesouro - Onde o baú está localizado
 * 
 * Mapas de tesouro (Buried Treasure) são tipicamente scale 1 = 256x256 blocos
 * Mapas de explorador (Mansion/Monument) são tipicamente scale 2 = 512x512 blocos
 */
public class TreasureMapUtils {
    
    /**
     * Verifica se o ItemStack é um mapa de tesouro/explorador.
     */
    public static boolean isTreasureMap(ItemStack stack, Level level) {
        if (stack.isEmpty() || stack.getItem() != Items.FILLED_MAP) {
            return false;
        }
        
        // Verifica pelo NBT do item
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Decorations", Tag.TAG_LIST)) {
            ListTag decorations = tag.getList("Decorations", Tag.TAG_COMPOUND);
            for (int i = 0; i < decorations.size(); i++) {
                CompoundTag decoration = decorations.getCompound(i);
                if (decoration.contains("type")) {
                    byte type = decoration.getByte("type");
                    // 26 = RED_X (tesouro), 8 = MANSION, 9 = MONUMENT
                    if (type == 26 || type == 8 || type == 9) {
                        return true;
                    }
                }
            }
        }
        
        // Verificação via MapItemSavedData
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
        
        // Verificação pelo nome
        String itemName = stack.getHoverName().getString().toLowerCase();
        return itemName.contains("treasure") || itemName.contains("tesouro") || 
               itemName.contains("buried") || itemName.contains("explorer") ||
               itemName.contains("explorador") || itemName.contains("mansion") ||
               itemName.contains("monument");
    }
    
    /**
     * Obtém a posição do CENTRO DA REGIÃO DO MAPA.
     * 
     * Como mapData.centerX/centerZ pode não estar sincronizado no cliente,
     * calculamos o centro baseado na posição do tesouro e na escala do mapa.
     * 
     * Mapas no Minecraft são alinhados a uma grade. Por exemplo, um mapa scale 1 (256x256)
     * tem seu centro em coordenadas específicas baseadas na grade mundial.
     */
    @Nullable
    public static BlockPos getMapCenterPosition(ItemStack stack, Level level) {
        if (stack.isEmpty() || stack.getItem() != Items.FILLED_MAP) {
            return null;
        }
        
        // Primeiro, tenta obter do MapItemSavedData (se estiver sincronizado)
        MapItemSavedData mapData = MapItem.getSavedData(stack, level);
        if (mapData != null && (mapData.centerX != 0 || mapData.centerZ != 0)) {
            BlockPos center = new BlockPos(mapData.centerX, 64, mapData.centerZ);
            return center;
        }
        
        // Se centerX/centerZ está zerado, calcula baseado na posição do tesouro
        BlockPos treasurePos = getTreasureIconPosition(stack, level);
        if (treasurePos != null) {
            // Determina a escala do mapa
            int scale = 1; // Default para mapas de tesouro (scale 1 = 256x256)
            if (mapData != null) {
                scale = mapData.scale;
            }
            
            // Calcula o centro da região do mapa baseado na posição do tesouro
            BlockPos mapCenter = calculateMapCenter(treasurePos.getX(), treasurePos.getZ(), scale);
            return mapCenter;
        }
        
        return null;
    }
    
    /**
     * Calcula o centro da região do mapa baseado em uma coordenada e escala.
     * 
     * Mapas no Minecraft são alinhados a uma grade mundial.
     * Para scale 0: grade de 128 blocos
     * Para scale 1: grade de 256 blocos  
     * Para scale 2: grade de 512 blocos
     * etc.
     * 
     * O centro do mapa é calculado encontrando em qual "célula" da grade
     * a coordenada está, e retornando o centro dessa célula.
     */
    private static BlockPos calculateMapCenter(int x, int z, int scale) {
        // Tamanho do mapa em blocos: 128 * 2^scale
        int mapSize = 128 * (1 << scale);
        
        // Mapas são alinhados com offset de 64 blocos
        // (o mapa começa em -64 relativamente à grade)
        int offset = 64;
        
        // Encontra o centro da célula do mapa onde esta coordenada está
        // Fórmula: floor((coord + offset) / mapSize) * mapSize + mapSize/2 - offset
        int centerX = (int) (Math.floor((double)(x + offset) / mapSize) * mapSize + mapSize / 2 - offset);
        int centerZ = (int) (Math.floor((double)(z + offset) / mapSize) * mapSize + mapSize / 2 - offset);
        
        return new BlockPos(centerX, 64, centerZ);
    }
    
    /**
     * Obtém a posição do ÍCONE X (onde o tesouro/baú está).
     * Esta é a posição para onde a bússola aponta quando pointToTreasureIcon = true.
     */
    @Nullable
    public static BlockPos getTreasureIconPosition(ItemStack stack, Level level) {
        if (stack.isEmpty() || stack.getItem() != Items.FILLED_MAP) {
            return null;
        }
        
        // Método 1: Tenta obter do NBT do item (coordenadas absolutas - mais confiável)
        BlockPos nbtPos = getPositionFromItemNBT(stack);
        if (nbtPos != null) {
            return nbtPos;
        }
        
        // Método 2: Tenta obter do MapItemSavedData
        BlockPos mapDataPos = getPositionFromMapDataDecorations(stack, level);
        if (mapDataPos != null) {
            return mapDataPos;
        }
        
        return null;
    }
    
    /**
     * Extrai as coordenadas do tesouro do NBT do ItemStack.
     * As decorações no NBT contêm "x" e "z" como coordenadas absolutas do mundo.
     */
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
            
            // Tipos relevantes:
            // 26 = RED_X (tesouro enterrado)
            // 8 = MANSION (mansão da floresta)
            // 9 = MONUMENT (monumento oceânico)
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
    
    /**
     * Extrai as coordenadas do tesouro das decorações do MapItemSavedData.
     */
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
                
                // Converte coordenadas relativas para absolutas
                int scale = 1 << mapData.scale;
                int worldX = mapData.centerX + (decoration.getX() * scale);
                int worldZ = mapData.centerZ + (decoration.getY() * scale);
                
                return new BlockPos(worldX, 64, worldZ);
            }
        }
        
        return null;
    }
}
