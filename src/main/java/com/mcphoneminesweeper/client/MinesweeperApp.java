package com.mcphoneminesweeper.client;

import com.mcphoneminesweeper.MCphoneMinesweeper;
import com.november.mcphone.api.client.app.IPhoneApp;
import com.november.mcphone.api.client.ui.IPhonePage;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class MinesweeperApp implements IPhoneApp {
   private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("mcphone_minesweeper", "minesweeper");
   private MinesweeperPage page;

   public ResourceLocation getId() {
      return ID;
   }

   public Component getDisplayName() {
      return Component.translatable("mcphone_minesweeper.app.minesweeper");
   }

   public ResourceLocation getIconTexture() {
      return ResourceLocation.fromNamespaceAndPath("mcphone_minesweeper", "textures/app/minesweeper.png");
   }

   public IPhonePage openPage() {
      if (this.page == null) {
         this.page = new MinesweeperPage();
      }

      return this.page;
   }

   public boolean isPreinstalled() {
      // 不预装：首次发现进应用商店，配合 IAppPriceProvider（1 TNT）实现"购买后上主屏"（mcphone 原版付费 App 逻辑）
      return false;
   }

   public void onPress() {
   }

   public String getVersion() {
      return MCphoneMinesweeper.version();
   }

   public String getAuthor() {
      return "ytbetafish";
   }

   public String getDescription() {
      String key = "mcphone_minesweeper.app.minesweeper.desc";
      return I18n.exists(key) ? I18n.get(key, new Object[0]) : "";
   }
}
