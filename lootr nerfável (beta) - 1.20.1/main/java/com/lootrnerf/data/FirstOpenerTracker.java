package com.lootrnerf.data;

import com.lootrnerf.LootrNerf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Rastreia quem foi o primeiro jogador a abrir cada baú Lootr
 * Persiste os dados no mundo do servidor
 */
public class FirstOpenerTracker extends SavedData {
    
    private static final String DATA_NAME = "lootrnerf_first_openers";
    
    // Mapa: "dimension:x,y,z" -> UUID do primeiro jogador
    private final Map<String, UUID> firstOpeners = new HashMap<>();
    
    // Mapa: "dimension:x,y,z" -> timestamp de quando foi aberto
    private final Map<String, Long> openTimestamps = new HashMap<>();
    
    public FirstOpenerTracker() {
        super();
    }
    
    public static FirstOpenerTracker load(CompoundTag tag) {
        FirstOpenerTracker tracker = new FirstOpenerTracker();
        
        if (tag.contains("first_openers", Tag.TAG_LIST)) {
            ListTag list = tag.getList("first_openers", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                String key = entry.getString("key");
                UUID uuid = entry.getUUID("uuid");
                long timestamp = entry.getLong("timestamp");
                
                tracker.firstOpeners.put(key, uuid);
                tracker.openTimestamps.put(key, timestamp);
            }
        }
        
        return tracker;
    }
    
    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        
        for (Map.Entry<String, UUID> entry : firstOpeners.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("key", entry.getKey());
            entryTag.putUUID("uuid", entry.getValue());
            entryTag.putLong("timestamp", openTimestamps.getOrDefault(entry.getKey(), 0L));
            list.add(entryTag);
        }
        
        tag.put("first_openers", list);
        return tag;
    }
    
    /**
     * Gera a chave única para um baú baseado na posição e dimensão
     */
    public static String getKey(ResourceKey<Level> dimension, BlockPos pos) {
        return dimension.location().toString() + ":" + pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }
    
    /**
     * Gera a chave única para um baú usando UUID do container Lootr
     */
    public static String getKeyFromUUID(UUID containerId) {
        return "uuid:" + containerId.toString();
    }
    
    /**
     * Verifica se um jogador é o primeiro a abrir o baú
     * Se ninguém abriu ainda, registra esse jogador como primeiro
     * 
     * @return true se é o primeiro jogador (ou se foi recém-registrado como primeiro)
     */
    public boolean isFirstOpener(String key, UUID playerUUID) {
        if (!firstOpeners.containsKey(key)) {
            // Ninguém abriu ainda - este é o primeiro!
            firstOpeners.put(key, playerUUID);
            openTimestamps.put(key, System.currentTimeMillis());
            setDirty();
            LootrNerf.LOGGER.debug("Primeiro jogador a abrir {}: {}", key, playerUUID);
            return true;
        }
        
        // Verifica se é o mesmo jogador que abriu primeiro
        return firstOpeners.get(key).equals(playerUUID);
    }
    
    /**
     * Obtém o UUID do primeiro jogador a abrir
     */
    @Nullable
    public UUID getFirstOpener(String key) {
        return firstOpeners.get(key);
    }
    
    /**
     * Verifica se o baú já foi aberto por alguém
     */
    public boolean hasBeenOpened(String key) {
        return firstOpeners.containsKey(key);
    }
    
    /**
     * Obtém o timestamp de quando o baú foi aberto pela primeira vez
     */
    public long getOpenTimestamp(String key) {
        return openTimestamps.getOrDefault(key, 0L);
    }
    
    /**
     * Remove o registro de um baú (útil para reset/admin)
     */
    public void clearOpener(String key) {
        firstOpeners.remove(key);
        openTimestamps.remove(key);
        setDirty();
    }
    
    /**
     * Obtém ou cria o tracker para o servidor
     */
    public static FirstOpenerTracker get(ServerLevel level) {
        DimensionDataStorage storage = level.getServer().overworld().getDataStorage();
        return storage.computeIfAbsent(
                FirstOpenerTracker::load,
                FirstOpenerTracker::new,
                DATA_NAME
        );
    }
    
    /**
     * Obtém estatísticas do tracker
     */
    public int getTrackedContainerCount() {
        return firstOpeners.size();
    }
}
