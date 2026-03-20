package com.treasurecompass;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Configurações do mod Treasure Compass.
 * Permite personalizar a aparência e comportamento da bússola.
 */
public class ModConfigHandler {
    
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    
    // Configurações de posição
    public static final ForgeConfigSpec.EnumValue<CompassPosition> COMPASS_POSITION;
    public static final ForgeConfigSpec.IntValue COMPASS_SIZE;
    public static final ForgeConfigSpec.IntValue COMPASS_PADDING;
    
    // Configurações de aparência
    public static final ForgeConfigSpec.BooleanValue SHOW_DISTANCE;
    public static final ForgeConfigSpec.BooleanValue SHOW_TITLE;
    public static final ForgeConfigSpec.BooleanValue SHOW_CARDINAL_MARKERS;
    
    // Configurações de comportamento
    public static final ForgeConfigSpec.BooleanValue POINT_TO_TREASURE_ICON;
    
    static {
        BUILDER.push("Treasure Compass Settings");
        
        BUILDER.comment("Posição da bússola na tela");
        COMPASS_POSITION = BUILDER.defineEnum("compassPosition", CompassPosition.TOP_RIGHT);
        
        BUILDER.comment("Tamanho da bússola em pixels (padrão: 64)");
        COMPASS_SIZE = BUILDER.defineInRange("compassSize", 64, 32, 128);
        
        BUILDER.comment("Espaçamento da borda da tela em pixels (padrão: 10)");
        COMPASS_PADDING = BUILDER.defineInRange("compassPadding", 10, 0, 50);
        
        BUILDER.comment("Mostrar distância até o mapa");
        SHOW_DISTANCE = BUILDER.define("showDistance", true);
        
        BUILDER.comment("Mostrar título 'Tesouro' acima da bússola");
        SHOW_TITLE = BUILDER.define("showTitle", true);
        
        BUILDER.comment("Mostrar marcadores cardeais (N, S, E, W)");
        SHOW_CARDINAL_MARKERS = BUILDER.define("showCardinalMarkers", true);
        
        BUILDER.comment("Se true, aponta para o ícone X do tesouro. Se false, aponta para o centro do mapa.");
        POINT_TO_TREASURE_ICON = BUILDER.define("pointToTreasureIcon", false);
        
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
    
    public enum CompassPosition {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        CENTER_LEFT,
        CENTER_RIGHT
    }
}
