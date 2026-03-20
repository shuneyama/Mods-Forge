package com.lootrnerf.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;

/**
 * Configurações do LootrNerf
 */
public class LootrNerfConfig {
    
    public static final ForgeConfigSpec SERVER_SPEC;
    public static final ServerConfig SERVER;
    
    // Configurações acessíveis
    public static ForgeConfigSpec.IntValue FIRST_OPENER_CHANCE;
    public static ForgeConfigSpec.IntValue OTHER_OPENER_CHANCE;
    public static ForgeConfigSpec.IntValue OTHER_OPENER_LOOT_MULTIPLIER;
    public static ForgeConfigSpec.IntValue MIN_ITEMS_FOR_OTHERS;
    public static ForgeConfigSpec.IntValue MAX_ITEMS_FOR_OTHERS;
    public static ForgeConfigSpec.BooleanValue REMOVE_RARE_ITEMS_FOR_OTHERS;
    public static ForgeConfigSpec.BooleanValue SHOW_MESSAGES;
    public static ForgeConfigSpec.BooleanValue ENABLED;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_ITEMS;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> WHITELISTED_LOOT_TABLES;
    
    static {
        Pair<ServerConfig, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
        SERVER_SPEC = pair.getRight();
        SERVER = pair.getLeft();
    }
    
    public static class ServerConfig {
        
        public ServerConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("===========================================")
                   .comment("  LootrNerf - Configurações de Balanceamento")
                   .comment("===========================================")
                   .push("geral");
            
            ENABLED = builder
                    .comment("Habilita/desabilita o sistema de nerf do Lootr")
                    .define("enabled", true);
            
            SHOW_MESSAGES = builder
                    .comment("Mostrar mensagens para os jogadores sobre o status do loot")
                    .define("show_messages", true);
            
            builder.pop().push("chances");
            
            FIRST_OPENER_CHANCE = builder
                    .comment("Chance (%) do primeiro jogador receber loot completo")
                    .comment("Padrão: 90 = 90% de chance de loot normal")
                    .defineInRange("first_opener_chance", 90, 0, 100);
            
            OTHER_OPENER_CHANCE = builder
                    .comment("Chance (%) dos outros jogadores receberem algum loot")
                    .comment("Padrão: 10 = apenas 10% de chance de receber algo")
                    .defineInRange("other_opener_chance", 10, 0, 100);
            
            builder.pop().push("loot_reduction");
            
            OTHER_OPENER_LOOT_MULTIPLIER = builder
                    .comment("Multiplicador de quantidade de itens para outros jogadores (%)")
                    .comment("Padrão: 25 = outros jogadores recebem apenas 25% da quantidade normal")
                    .defineInRange("other_opener_loot_multiplier", 25, 1, 100);
            
            MIN_ITEMS_FOR_OTHERS = builder
                    .comment("Mínimo de itens que outros jogadores podem receber (se tiverem sorte)")
                    .defineInRange("min_items_for_others", 1, 0, 27);
            
            MAX_ITEMS_FOR_OTHERS = builder
                    .comment("Máximo de itens que outros jogadores podem receber")
                    .defineInRange("max_items_for_others", 5, 1, 27);
            
            REMOVE_RARE_ITEMS_FOR_OTHERS = builder
                    .comment("Remover itens raros (diamante, netherite, enchanted, etc) do loot dos outros jogadores")
                    .define("remove_rare_items_for_others", true);
            
            builder.pop().push("filtros");
            
            BLACKLISTED_ITEMS = builder
                    .comment("Lista de itens que NUNCA aparecem para outros jogadores")
                    .comment("Formato: modid:item_name")
                    .comment("Suporta itens de QUALQUER MOD!")
                    .defineList("blacklisted_items", Arrays.asList(
                            // ========== VANILLA ==========
                            "minecraft:diamond",
                            "minecraft:diamond_block",
                            "minecraft:diamond_sword",
                            "minecraft:diamond_pickaxe",
                            "minecraft:diamond_axe",
                            "minecraft:diamond_shovel",
                            "minecraft:diamond_hoe",
                            "minecraft:diamond_helmet",
                            "minecraft:diamond_chestplate",
                            "minecraft:diamond_leggings",
                            "minecraft:diamond_boots",
                            "minecraft:netherite_ingot",
                            "minecraft:netherite_block",
                            "minecraft:netherite_scrap",
                            "minecraft:ancient_debris",
                            "minecraft:netherite_sword",
                            "minecraft:netherite_pickaxe",
                            "minecraft:netherite_axe",
                            "minecraft:netherite_shovel",
                            "minecraft:netherite_hoe",
                            "minecraft:netherite_helmet",
                            "minecraft:netherite_chestplate",
                            "minecraft:netherite_leggings",
                            "minecraft:netherite_boots",
                            "minecraft:enchanted_golden_apple",
                            "minecraft:elytra",
                            "minecraft:totem_of_undying",
                            "minecraft:trident",
                            "minecraft:heart_of_the_sea",
                            "minecraft:nether_star",
                            "minecraft:dragon_egg",
                            "minecraft:dragon_breath",
                            "minecraft:beacon",
                            "minecraft:conduit",
                            "minecraft:netherite_upgrade_smithing_template",
                            
                            // ========== MEKANISM ==========
                            "mekanism:refined_obsidian_ingot",
                            "mekanism:refined_glowstone_ingot",
                            "mekanism:atomic_alloy",
                            "mekanism:hdpe_sheet",
                            "mekanism:mekasuit_helmet",
                            "mekanism:mekasuit_bodyarmor",
                            "mekanism:mekasuit_pants",
                            "mekanism:mekasuit_boots",
                            "mekanism:meka_tool",
                            "mekanism:teleportation_core",
                            "mekanism:robit",
                            
                            // ========== CREATE ==========
                            "create:precision_mechanism",
                            "create:chromatic_compound",
                            "create:shadow_steel",
                            "create:refined_radiance",
                            "create:blaze_cake",
                            "create:wand_of_symmetry",
                            "create:extendo_grip",
                            "create:potato_cannon",
                            "create:handheld_worldshaper",
                            
                            // ========== THERMAL ==========
                            "thermal:enderium_ingot",
                            "thermal:lumium_ingot",
                            "thermal:signalum_ingot",
                            
                            // ========== AE2 ==========
                            "ae2:singularity",
                            "ae2:quantum_entangled_singularity",
                            
                            // ========== BOTANIA ==========
                            "botania:terrasteel_ingot",
                            "botania:gaia_ingot",
                            "botania:dice_of_fate",
                            
                            // ========== ALEX'S CAVES (você tem instalado) ==========
                            "alexscaves:pure_darkness",
                            "alexscaves:primordial_gem",
                            "alexscaves:extinction_spear"
                    ), obj -> obj instanceof String);
            
            WHITELISTED_LOOT_TABLES = builder
                    .comment("Lista de loot tables onde o nerf NÃO se aplica (deixe vazio para aplicar em todos)")
                    .comment("Formato: modid:loot_table_path")
                    .defineList("whitelisted_loot_tables", Arrays.asList(), obj -> obj instanceof String);
            
            builder.pop();
        }
    }
}
