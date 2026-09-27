package com.mcphoneminesweeper;

import com.november.mcphone.api.cost.IAppPriceProvider;
import com.november.mcphone.api.cost.ICost;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

public final class MsPriceProvider implements IAppPriceProvider {
   public Map<ResourceLocation, ICost> prices() {
      return Map.of(ResourceLocation.fromNamespaceAndPath("mcphone_minesweeper", "minesweeper"), ICost.of(Items.TNT, 1));
   }
}
