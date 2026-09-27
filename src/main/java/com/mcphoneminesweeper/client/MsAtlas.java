package com.mcphoneminesweeper.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public final class MsAtlas {
   public static final ResourceLocation BLOCKS = ResourceLocation.fromNamespaceAndPath("mcphone_minesweeper", "textures/minesweeper/blocks.png");
   public static final ResourceLocation BLOCKS_BW = ResourceLocation.fromNamespaceAndPath("mcphone_minesweeper", "textures/minesweeper/blocks_bw.png");
   public static final ResourceLocation DIGITS = ResourceLocation.fromNamespaceAndPath("mcphone_minesweeper", "textures/minesweeper/digits.png");
   public static final ResourceLocation DIGITS_BW = ResourceLocation.fromNamespaceAndPath("mcphone_minesweeper", "textures/minesweeper/digits_bw.png");
   public static final ResourceLocation FACES = ResourceLocation.fromNamespaceAndPath("mcphone_minesweeper", "textures/minesweeper/faces.png");
   public static final ResourceLocation FACES_BW = ResourceLocation.fromNamespaceAndPath("mcphone_minesweeper", "textures/minesweeper/faces_bw.png");
   public static final ResourceLocation TITLEBAR = ResourceLocation.fromNamespaceAndPath("mcphone_minesweeper", "textures/ui/titlebar.png");
   public static final ResourceLocation BTN_CLOSE = ResourceLocation.fromNamespaceAndPath("mcphone_minesweeper", "textures/ui/btn_close.png");
   public static final ResourceLocation BTN_CLOSE_PRESSED = ResourceLocation.fromNamespaceAndPath("mcphone_minesweeper", "textures/ui/btn_close_pressed.png");
   public static final ResourceLocation APP_ICON = ResourceLocation.fromNamespaceAndPath("mcphone_minesweeper", "textures/app/minesweeper.png");
   public static final int T_COVERED = 0;
   public static final int T_FLAG = 1;
   public static final int T_QUESTION = 2;
   public static final int T_MINE_RED = 3;
   public static final int T_MINE_X = 4;
   public static final int T_MINE = 5;
   public static final int T_QUESTION_PRESSED = 6;
   public static final int T_EMPTY = 15;
   public static final int D_MINUS = 1;
   public static final int D_BLANK = 0;
   public static final int F_NORMAL = 4;
   public static final int F_WIN = 1;
   public static final int F_DEAD = 2;
   public static final int F_OH = 3;
   public static final int F_PRESSED = 0;

   public static int digitIndex(int d) {
      return 11 - d % 10;
   }

   private MsAtlas() {
   }

   public static void drawBlock(GuiGraphics g, boolean colors, int x, int y, int size, int tile) {
      drawBlock(g, colors, x, y, size, size, tile);
   }

   public static void drawBlock(GuiGraphics g, boolean colors, int x, int y, int w, int h, int tile) {
      ResourceLocation tex = colors ? BLOCKS : BLOCKS_BW;
      g.blit(tex, x, y, w, h, 0.0F, (float)tile * 16.0F, 16, 16, 16, 256);
   }

   public static void drawDigit(GuiGraphics g, boolean colors, int x, int y, int w, int h, int tile) {
      ResourceLocation tex = colors ? DIGITS : DIGITS_BW;
      g.blit(tex, x, y, w, h, 0.0F, (float)tile * 23.0F, 13, 23, 13, 276);
   }

   public static void drawFace(GuiGraphics g, boolean colors, int x, int y, int size, int face) {
      ResourceLocation tex = colors ? FACES : FACES_BW;
      g.blit(tex, x, y, size, size, 0.0F, (float)face * 24.0F, 24, 24, 24, 120);
   }

   public static void drawTitlebar(GuiGraphics g, int x, int y, int w, int h) {
      g.blit(TITLEBAR, x, y, w, h, 0.0F, 0.0F, 128, 12, 128, 12);
   }

   public static void drawCloseBtn(GuiGraphics g, int x, int y, int h, boolean pressed) {
      ResourceLocation tex = pressed ? BTN_CLOSE_PRESSED : BTN_CLOSE;
      g.blit(tex, x, y, 16, h, 0.0F, 0.0F, 16, 12, 16, 12);
   }
}
