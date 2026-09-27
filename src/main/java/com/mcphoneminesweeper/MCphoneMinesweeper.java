package com.mcphoneminesweeper;

import com.mcphoneminesweeper.client.MsConfig;
import com.mcphoneminesweeper.client.MsSounds;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(
   value = "mcphone_minesweeper",
   dist = {Dist.CLIENT}
)
public final class MCphoneMinesweeper {
   public static final String MODID = "mcphone_minesweeper";
   public static final Logger LOGGER = LogUtils.getLogger();
   private static String version = "0.0.0";

   public MCphoneMinesweeper(ModContainer container) {
      version = container.getModInfo().getVersion().toString();
      MsSounds.registerBus(container.getEventBus());
      MsConfig.load();
      LOGGER.info("[MCphone扫雷] v{} 已加载，App 会由 MCphone 的 SPI 自行发现", version);
   }

   public static String version() {
      return version;
   }
}
