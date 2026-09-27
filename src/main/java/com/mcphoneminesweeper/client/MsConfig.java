package com.mcphoneminesweeper.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;

public final class MsConfig {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   public boolean questionMarks = true;
   public boolean colors = true;
   public boolean sound = true;
   public int customWidth = 30;
   public int customHeight = 16;
   public int customMines = 99;
   public int beginnerBest = 999;
   public int intermediateBest = 999;
   public int expertBest = 999;
   private static MsConfig instance = new MsConfig();

   private MsConfig() {
   }

   public static MsConfig get() {
      return instance;
   }

   public static void load() {
      Path path = path();
      if (!Files.exists(path)) {
         save();
      } else {
         try (Reader r = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            MsConfig loaded = (MsConfig)GSON.fromJson(r, MsConfig.class);
            if (loaded != null) {
               instance = loaded;
            }
         } catch (RuntimeException | IOException var6) {
            LOGGER.warn("[MCphone扫雷] 读取配置失败，使用默认值：{}", var6.toString());
         }
      }
   }

   public static void save() {
      Path path = path();

      try {
         Files.createDirectories(path.getParent());

         try (Writer w = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            GSON.toJson(instance, w);
         }
      } catch (IOException var6) {
         LOGGER.warn("[MCphone扫雷] 保存配置失败：{}", var6.toString());
      }
   }

   private static Path path() {
      return Minecraft.getInstance().gameDirectory.toPath().resolve("config/mcphone_minesweeper.json");
   }

   public void recordBest(int difficulty, int seconds) {
      if (difficulty >= 0 && difficulty <= 2) {
         int[] bests = new int[]{this.beginnerBest, this.intermediateBest, this.expertBest};
         if (seconds < bests[difficulty]) {
            switch (difficulty) {
               case 0:
                  this.beginnerBest = seconds;
                  break;
               case 1:
                  this.intermediateBest = seconds;
                  break;
               case 2:
                  this.expertBest = seconds;
            }

            save();
         }
      }
   }

   public int bestOf(int difficulty) {
      return switch (difficulty) {
         case 0 -> this.beginnerBest;
         case 1 -> this.intermediateBest;
         default -> this.expertBest;
      };
   }

   public void resetBests() {
      this.beginnerBest = 999;
      this.intermediateBest = 999;
      this.expertBest = 999;
      save();
   }
}
