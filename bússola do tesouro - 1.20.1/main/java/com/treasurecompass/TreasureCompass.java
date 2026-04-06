package com.treasurecompass;

import com.mojang.logging.LogUtils;
import com.treasurecompass.handler.CompassLinkHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(TreasureCompass.MODID)
public class TreasureCompass {
    public static final String MODID = "treasurecompass";
    private static final Logger LOGGER = LogUtils.getLogger();

    public TreasureCompass() {
        MinecraftForge.EVENT_BUS.register(new CompassLinkHandler());

        LOGGER.info("Treasure Compass Mod carregado!");
        LOGGER.info("Segure um mapa de tesouro em uma mao e uma bussola na outra, depois clique com botao direito!");
    }
}
