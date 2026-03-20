package com.lootrnerf.util;

import com.lootrnerf.LootrNerf;
import com.lootrnerf.config.LootrNerfConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Utilitários para modificar o loot baseado nas regras do nerf
 */
public class LootModifierUtil {
    
    private static final Random RANDOM = new Random();
    
    /**
     * Processa o loot para um jogador que NÃO é o primeiro a abrir
     * Aplica as regras de redução configuradas
     * 
     * @param originalLoot Lista original de itens
     * @return Lista modificada (reduzida) de itens
     */
    public static List<ItemStack> processLootForOtherOpener(List<ItemStack> originalLoot) {
        // Verifica se o jogador tem sorte de receber algo
        int chanceRoll = RANDOM.nextInt(100);
        int otherChance = LootrNerfConfig.OTHER_OPENER_CHANCE.get();
        
        if (chanceRoll >= otherChance) {
            LootrNerf.LOGGER.debug("Jogador não teve sorte ({} >= {}), retornando loot vazio", chanceRoll, otherChance);
            return new ArrayList<>(); // Sem sorte, loot vazio
        }
        
        List<ItemStack> modifiedLoot = new ArrayList<>();
        
        // Filtra itens blacklistados
        List<ItemStack> filteredLoot = filterBlacklistedItems(originalLoot);
        
        if (filteredLoot.isEmpty()) {
            return modifiedLoot;
        }
        
        // Calcula quantos itens o jogador vai receber
        int multiplier = LootrNerfConfig.OTHER_OPENER_LOOT_MULTIPLIER.get();
        int minItems = LootrNerfConfig.MIN_ITEMS_FOR_OTHERS.get();
        int maxItems = LootrNerfConfig.MAX_ITEMS_FOR_OTHERS.get();
        
        int targetItems = Math.max(minItems, (filteredLoot.size() * multiplier) / 100);
        targetItems = Math.min(targetItems, maxItems);
        targetItems = Math.min(targetItems, filteredLoot.size());
        
        if (targetItems <= 0) {
            targetItems = Math.min(minItems, filteredLoot.size());
        }
        
        // Seleciona itens aleatoriamente
        List<ItemStack> shuffledLoot = new ArrayList<>(filteredLoot);
        java.util.Collections.shuffle(shuffledLoot, RANDOM);
        
        for (int i = 0; i < targetItems && i < shuffledLoot.size(); i++) {
            ItemStack stack = shuffledLoot.get(i).copy();
            
            // Reduz a quantidade do stack também
            if (stack.getCount() > 1) {
                int newCount = Math.max(1, (stack.getCount() * multiplier) / 100);
                stack.setCount(newCount);
            }
            
            modifiedLoot.add(stack);
        }
        
        LootrNerf.LOGGER.debug("Loot reduzido: {} -> {} itens", originalLoot.size(), modifiedLoot.size());
        return modifiedLoot;
    }
    
    /**
     * Filtra itens que estão na blacklist
     */
    private static List<ItemStack> filterBlacklistedItems(List<ItemStack> loot) {
        List<? extends String> blacklist = LootrNerfConfig.BLACKLISTED_ITEMS.get();
        boolean removeRare = LootrNerfConfig.REMOVE_RARE_ITEMS_FOR_OTHERS.get();
        
        List<ItemStack> filtered = new ArrayList<>();
        
        for (ItemStack stack : loot) {
            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
            if (itemId == null) continue;
            
            String itemIdStr = itemId.toString();
            
            // Verifica blacklist
            if (blacklist.contains(itemIdStr)) {
                LootrNerf.LOGGER.debug("Item {} removido (blacklist)", itemIdStr);
                continue;
            }
            
            // Verifica se deve remover itens raros/encantados
            if (removeRare) {
                if (isRareItem(stack)) {
                    LootrNerf.LOGGER.debug("Item {} removido (raro/encantado)", itemIdStr);
                    continue;
                }
            }
            
            filtered.add(stack);
        }
        
        return filtered;
    }
    
    /**
     * Verifica se um item é considerado "raro"
     * Funciona para itens vanilla E de outros mods!
     */
    private static boolean isRareItem(ItemStack stack) {
        // Itens encantados são raros (qualquer mod)
        if (stack.isEnchanted() || EnchantmentHelper.getEnchantments(stack).size() > 0) {
            return true;
        }
        
        // Verifica pela raridade do item (qualquer mod que defina raridade)
        switch (stack.getRarity()) {
            case EPIC:
            case RARE:
                return true;
            default:
                break;
        }
        
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId != null) {
            String path = itemId.getPath().toLowerCase();
            String namespace = itemId.getNamespace().toLowerCase();
            
            // =============================================
            // MATERIAIS VALIOSOS (funciona para qualquer mod)
            // =============================================
            
            // Tier alto universal
            if (path.contains("diamond") || 
                path.contains("netherite") ||
                path.contains("emerald") ||
                path.contains("ancient") ||      // ancient debris, ancient ingot, etc
                path.contains("void") ||         // void steel, void crystal, etc
                path.contains("ultimate") ||     // ultimate ingot, etc
                path.contains("awakened") ||     // awakened draconium, etc
                path.contains("chaotic") ||      // draconic evolution
                path.contains("draconic") ||     // draconic evolution
                path.contains("creative")) {     // creative items
                return true;
            }
            
            // =============================================
            // MEKANISM
            // =============================================
            if (namespace.equals("mekanism") || namespace.contains("mekanism")) {
                if (path.contains("refined_obsidian") ||
                    path.contains("refined_glowstone") ||
                    path.contains("hdpe") ||
                    path.contains("atomic") ||
                    path.contains("elite") ||
                    path.contains("ultimate") ||
                    path.contains("alloy") ||      // reinforced/atomic/infused alloy
                    path.contains("teleport") ||
                    path.contains("robit") ||
                    path.contains("mekasuit") ||
                    path.contains("meka_tool")) {
                    return true;
                }
            }
            
            // =============================================
            // CREATE
            // =============================================
            if (namespace.equals("create") || namespace.contains("create")) {
                if (path.contains("precision") ||   // precision mechanism
                    path.contains("blaze_cake") ||
                    path.contains("chromatic") ||
                    path.contains("shadow_steel") ||
                    path.contains("refined_radiance") ||
                    path.contains("wand_of") ||     // wand of symmetry
                    path.contains("extendo") ||
                    path.contains("potato_cannon") ||
                    path.contains("handheld_worldshaper")) {
                    return true;
                }
            }
            
            // =============================================
            // THERMAL SERIES
            // =============================================
            if (namespace.contains("thermal")) {
                if (path.contains("enderium") ||
                    path.contains("lumium") ||
                    path.contains("signalum") ||
                    path.contains("flux") ||        // flux items
                    path.contains("resonant")) {
                    return true;
                }
            }
            
            // =============================================
            // APPLIED ENERGISTICS 2
            // =============================================
            if (namespace.equals("ae2") || namespace.contains("appliede")) {
                if (path.contains("cell") ||        // storage cells
                    path.contains("singularity") ||
                    path.contains("quantum") ||
                    path.contains("spatial") ||
                    path.contains("fluix") ||
                    path.contains("certus") ||      // pure certus
                    path.contains("processor")) {   // processors
                    return true;
                }
            }
            
            // =============================================
            // BOTANIA
            // =============================================
            if (namespace.equals("botania")) {
                if (path.contains("terrasteel") ||
                    path.contains("elementium") ||
                    path.contains("gaia") ||
                    path.contains("relic") ||
                    path.contains("dice") ||
                    path.contains("ring_of") ||
                    path.contains("flight_tiara")) {
                    return true;
                }
            }
            
            // =============================================
            // ITENS ESPECIAIS GERAIS (qualquer mod)
            // =============================================
            if (path.contains("golden_apple") || 
                path.contains("enchanted_") ||      // enchanted anything
                path.contains("elytra") || 
                path.contains("totem") ||
                path.contains("trident") ||
                path.contains("nether_star") ||
                path.contains("heart_of") ||        // heart_of_the_sea, heart_of_diamond, etc
                path.contains("dragon") ||          // dragon egg, dragon breath, etc
                path.contains("wither") ||          // wither items
                path.contains("beacon") ||
                path.contains("conduit") ||
                path.contains("lodestone") ||
                path.contains("respawn_anchor") ||
                path.contains("upgrade_smithing") ||    // smithing templates
                path.contains("netherite_upgrade") ||
                path.contains("armor_trim") ||
                path.contains("music_disc") ||
                path.contains("_spawn_egg") ||      // spawn eggs
                path.contains("command_block") ||
                path.contains("structure_block") ||
                path.contains("jigsaw") ||
                path.contains("barrier") ||
                path.contains("debug")) {
                return true;
            }
            
            // =============================================
            // FERRAMENTAS/ARMADURAS DE TIER ALTO
            // =============================================
            // Detecta padrões comuns de nomenclatura de mods
            if ((path.contains("_sword") || 
                 path.contains("_pickaxe") || 
                 path.contains("_axe") || 
                 path.contains("_shovel") ||
                 path.contains("_hoe") ||
                 path.contains("_helmet") ||
                 path.contains("_chestplate") ||
                 path.contains("_leggings") ||
                 path.contains("_boots") ||
                 path.contains("_paxel") ||      // multi-tools
                 path.contains("_hammer") ||
                 path.contains("_excavator")) &&
                (path.contains("diamond") || 
                 path.contains("netherite") ||
                 path.contains("refined") ||
                 path.contains("reinforced") ||
                 path.contains("hardened") ||
                 path.contains("energized") ||
                 path.contains("flux") ||
                 path.contains("dark_steel") ||
                 path.contains("end_steel") ||
                 path.contains("stellar") ||
                 path.contains("vibrant"))) {
                return true;
            }
        }
        
        // Verifica valor de repair cost alto (indica item valioso/modificado)
        if (stack.getBaseRepairCost() > 10) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Verifica se uma loot table está na whitelist (não deve ser nerfada)
     */
    public static boolean isLootTableWhitelisted(ResourceLocation lootTable) {
        if (lootTable == null) return false;
        
        List<? extends String> whitelist = LootrNerfConfig.WHITELISTED_LOOT_TABLES.get();
        if (whitelist.isEmpty()) {
            return false; // Whitelist vazia = nerf em todos
        }
        
        return whitelist.contains(lootTable.toString());
    }
    
    /**
     * Calcula se o primeiro jogador deve receber loot completo
     * (baseado na chance configurada)
     */
    public static boolean shouldFirstOpenerGetFullLoot() {
        int chanceRoll = RANDOM.nextInt(100);
        int firstChance = LootrNerfConfig.FIRST_OPENER_CHANCE.get();
        return chanceRoll < firstChance;
    }
}
