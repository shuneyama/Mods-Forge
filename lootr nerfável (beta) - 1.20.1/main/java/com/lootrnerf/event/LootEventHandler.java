package com.lootrnerf.event;

import com.lootrnerf.LootrNerf;
import com.lootrnerf.config.LootrNerfConfig;
import com.lootrnerf.data.FirstOpenerTracker;
import com.lootrnerf.util.LootModifierUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

/**
 * Event handler que intercepta quando jogadores abrem containers
 * Funciona como alternativa/complemento ao mixin
 */
@Mod.EventBusSubscriber(modid = LootrNerf.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LootEventHandler {
    
    // Cache de inventários já processados nesta sessão (evita processar múltiplas vezes)
    private static final Set<String> processedThisSession = Collections.synchronizedSet(new HashSet<>());
    
    // Mapa temporário para rastrear qual player está abrindo qual posição
    private static final Map<UUID, BlockPos> pendingOpens = Collections.synchronizedMap(new HashMap<>());
    
    /**
     * Intercepta quando um jogador interage com um bloco (potencialmente um baú Lootr)
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!LootrNerfConfig.ENABLED.get()) return;
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        BlockPos pos = event.getPos();
        BlockEntity blockEntity = event.getLevel().getBlockEntity(pos);
        
        // Verifica se é um container do Lootr
        if (blockEntity != null && isLootrContainer(blockEntity)) {
            pendingOpens.put(player.getUUID(), pos);
            LootrNerf.LOGGER.debug("Player {} interagindo com container Lootr em {}", 
                player.getName().getString(), pos);
        }
    }

    /**
     * Intercepta quando um container é aberto
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        if (!LootrNerfConfig.ENABLED.get()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        // Obtém o menu (não o container diretamente)
        var menu = event.getContainer();

        // Tenta obter a posição do container pendente
        BlockPos pos = pendingOpens.remove(player.getUUID());

        if (pos == null) {
            // Tenta identificar posição via reflection do menu
            pos = tryGetPositionFromMenu(menu, player);
        }

        if (pos == null) {
            return;
        }

        // Obtém o container real do BlockEntity
        BlockEntity be = serverLevel.getBlockEntity(pos);
        if (be instanceof Container container) {
            processLootForPlayer(player, serverLevel, pos, container);
        }
    }

    /**
     * Tenta obter a posição do menu via reflection
     */
    private static BlockPos tryGetPositionFromMenu(Object menu, Player player) {
        try {
            // Tenta encontrar campo de posição no menu
            Class<?> clazz = menu.getClass();
            while (clazz != null && clazz != Object.class) {
                for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    Object value = field.get(menu);
                    if (value instanceof BlockPos bp) {
                        return bp;
                    }
                    if (value instanceof BlockEntity be) {
                        return be.getBlockPos();
                    }
                }
                clazz = clazz.getSuperclass();
            }
        } catch (Exception e) {
            LootrNerf.LOGGER.debug("Não foi possível obter posição do menu: ", e);
        }
        return null;
    }
    
    /**
     * Processa o loot baseado nas regras de primeiro jogador
     */
    private static void processLootForPlayer(ServerPlayer player, ServerLevel level, BlockPos pos, Container container) {
        ResourceKey<Level> dimension = level.dimension();
        String key = FirstOpenerTracker.getKey(dimension, pos);
        
        // Verifica se já processamos este container para este jogador nesta sessão
        String sessionKey = key + ":" + player.getUUID();
        if (processedThisSession.contains(sessionKey)) {
            return;
        }
        
        FirstOpenerTracker tracker = FirstOpenerTracker.get(level);
        UUID playerUUID = player.getUUID();
        
        boolean isFirstOpener = tracker.isFirstOpener(key, playerUUID);
        
        // Marca como processado
        processedThisSession.add(sessionKey);
        
        // Agenda limpeza do cache após um tempo
        scheduleSessionCleanup(sessionKey);
        
        if (isFirstOpener) {
            handleFirstOpenerContainer(player, container);
        } else {
            handleOtherOpenerContainer(player, container, tracker.getFirstOpener(key));
        }
    }
    
    /**
     * Lida com o primeiro jogador
     */
    private static void handleFirstOpenerContainer(ServerPlayer player, Container container) {
        if (LootrNerfConfig.SHOW_MESSAGES.get()) {
            player.sendSystemMessage(
                Component.literal("§a§l✦ PRIMEIRO DESCOBRIDOR! §r§aVocê encontrou este baú primeiro. Loot completo!")
            );
        }
        
        // Verifica chance do primeiro jogador
        if (!LootModifierUtil.shouldFirstOpenerGetFullLoot()) {
            // 10% de chance do primeiro jogador ter loot reduzido também
            if (LootrNerfConfig.SHOW_MESSAGES.get()) {
                player.sendSystemMessage(
                    Component.literal("§e⚠ Porém a sorte não estava completamente do seu lado...")
                );
            }
            reduceContainerPartially(container, 70); // Mantém 70%
        }
        
        LootrNerf.LOGGER.info("Primeiro jogador {} abriu container em seu mundo", player.getName().getString());
    }
    
    /**
     * Lida com outros jogadores
     */
    private static void handleOtherOpenerContainer(ServerPlayer player, Container container, UUID firstOpenerUUID) {
        if (LootrNerfConfig.SHOW_MESSAGES.get()) {
            player.sendSystemMessage(
                Component.literal("§c§l✗ BAÚ JÁ DESCOBERTO §r§cOutro explorador chegou primeiro. Loot severamente reduzido.")
            );
            
            int chance = LootrNerfConfig.OTHER_OPENER_CHANCE.get();
            player.sendSystemMessage(
                Component.literal("§7Você tem apenas " + chance + "% de chance de encontrar algo útil...")
            );
        }
        
        // Aplica redução severa
        applyLootReduction(container);
        
        LootrNerf.LOGGER.info("Jogador {} abriu container já descoberto", player.getName().getString());
    }
    
    /**
     * Aplica redução de loot para outros jogadores
     */
    private static void applyLootReduction(Container container) {
        // Coleta todos os itens atuais
        List<ItemStack> currentItems = new ArrayList<>();
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                currentItems.add(stack.copy());
            }
        }
        
        // Limpa o container
        container.clearContent();
        
        // Aplica as regras de redução
        List<ItemStack> reducedItems = LootModifierUtil.processLootForOtherOpener(currentItems);
        
        // Coloca os itens de volta
        for (int i = 0; i < reducedItems.size() && i < container.getContainerSize(); i++) {
            container.setItem(i, reducedItems.get(i));
        }
    }
    
    /**
     * Reduz parcialmente o container (para primeiro jogador azarado)
     */
    private static void reduceContainerPartially(Container container, int percentToKeep) {
        Random random = new Random();
        
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                if (random.nextInt(100) >= percentToKeep) {
                    container.setItem(i, ItemStack.EMPTY);
                } else if (stack.getCount() > 1) {
                    int newCount = Math.max(1, (stack.getCount() * percentToKeep) / 100);
                    stack.setCount(newCount);
                }
            }
        }
    }
    
    /**
     * Verifica se um BlockEntity é um container do Lootr
     */
    private static boolean isLootrContainer(BlockEntity blockEntity) {
        String className = blockEntity.getClass().getName();
        return className.contains("lootr") || className.contains("Lootr");
    }
    
    /**
     * Tenta obter a posição de um container
     */
    private static BlockPos tryGetContainerPosition(Container container, Player player) {
        // Se o container tem uma posição (BlockEntity)
        if (container instanceof BlockEntity be) {
            return be.getBlockPos();
        }
        
        // Tenta via reflection para containers do Lootr
        try {
            // Lootr usa posições customizadas
            Class<?> clazz = container.getClass();
            
            // Tenta getBlockPos
            try {
                java.lang.reflect.Method method = clazz.getMethod("getBlockPos");
                Object result = method.invoke(container);
                if (result instanceof BlockPos) {
                    return (BlockPos) result;
                }
            } catch (NoSuchMethodException ignored) {}
            
            // Tenta getPos
            try {
                java.lang.reflect.Method method = clazz.getMethod("getPos");
                Object result = method.invoke(container);
                if (result instanceof BlockPos) {
                    return (BlockPos) result;
                }
            } catch (NoSuchMethodException ignored) {}
            
        } catch (Exception e) {
            LootrNerf.LOGGER.debug("Não foi possível obter posição do container: ", e);
        }
        
        return null;
    }
    
    /**
     * Agenda limpeza do cache de sessão
     */
    private static void scheduleSessionCleanup(String sessionKey) {
        // Remove após 5 minutos para evitar vazamento de memória
        new Thread(() -> {
            try {
                Thread.sleep(300000); // 5 minutos
                processedThisSession.remove(sessionKey);
            } catch (InterruptedException ignored) {}
        }).start();
    }
    
    /**
     * Limpa todos os caches (chamado ao desligar o servidor)
     */
    public static void clearCaches() {
        processedThisSession.clear();
        pendingOpens.clear();
    }
}
