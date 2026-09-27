package com.mcphoneminesweeper.client;

import com.mcphoneminesweeper.client.board.Board;
import com.november.mcphone.api.client.ui.IPhonePage;
import com.november.mcphone.api.client.ui.PhoneCanvas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.sounds.SoundEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MinesweeperPage implements IPhonePage {
   private static final Logger LOGGER = LoggerFactory.getLogger("mcphone_minesweeper");
   private MinesweeperPage.View view = MinesweeperPage.View.GAME;
   private boolean menuOpen;
   private static boolean layoutLogged;
   private static int layoutFrame;
   private static final int[][] PRESETS = new int[][]{{9, 9, 10}, {16, 16, 40}, {30, 16, 99}};
   private static final int DIF_BEGINNER = 0;
   private static final int DIF_INTERMEDIATE = 1;
   private static final int DIF_EXPERT = 2;
   private static final int DIF_CUSTOM = 3;
   private int difficulty = 0;
   private int cols = PRESETS[0][0];
   private int rows = PRESETS[0][1];
   private int mines = PRESETS[0][2];
   private Board board;
   private MinesweeperPage.GameState state = MinesweeperPage.GameState.READY;
   private long startedAt;
   private int elapsedSeconds;
   private int toolMode;
   private int hoverCellX = -1;
   private int hoverCellY = -1;
   private boolean hoverSmiley;
   private boolean hoverFlagBtn;
   private boolean hoverQBtn;
   private boolean hoverCloseBtn;
   private boolean hoverGameMenu;
   private int hoverMenuItem = -1;
   private MinesweeperPage.Layout cur;
   // 版本号唯一来源：菜单版本项、日志、关于窗口全部引用此处，改一处即可全同步
   public static final String VERSION = "1.0.28";
   private final MinesweeperPage.CustomDialog custom = new MinesweeperPage.CustomDialog();
   private final MinesweeperPage.HeroDialog hero = new MinesweeperPage.HeroDialog();
   private final MinesweeperPage.AboutDialog about = new MinesweeperPage.AboutDialog();
   private static final int[] MENU_SCHEDULE = new int[]{0, -1, 1, 2, 3, 4, -1, 5, 6, -1, 7, -1, 8};
   private static final String[] MENU_KEYS = new String[]{
      "mcphone_minesweeper.menu.new",
      "mcphone_minesweeper.menu.beginner",
      "mcphone_minesweeper.menu.intermediate",
      "mcphone_minesweeper.menu.expert",
      "mcphone_minesweeper.menu.custom",
      "mcphone_minesweeper.menu.colors",
      "mcphone_minesweeper.menu.sound",
      "mcphone_minesweeper.menu.best",
      "mcphone_minesweeper.menu.version"
   };
   private static final int TITLE_H = 12;
   private static final int MENU_H = 13;
   private static final int COUNTER_H = 24;
   private static int winX;
   private static int winY;
   private static int winW;
   private static int winH;
   private static final int COLOR_BG = -4144960;
   private static final int COLOR_DARK = -8355712;
   private static final int COLOR_BLACK = -16777216;
   private static final int COLOR_WHITE = -1;
   private static final int COLOR_HOVER = -13538619;
   private static final int MENU_W = 86;
   private static final int ITEM_H = 10;
   private static final int SEP_H = 4;

   public MinesweeperPage() {
      this.newGame(this.cols, this.rows, this.mines);
   }

   private void newGame(int w, int h, int m) {
      this.cols = w;
      this.rows = h;
      this.mines = m;
      this.board = new Board(w, h, m, MsConfig.get().questionMarks);
      this.state = MinesweeperPage.GameState.READY;
      this.elapsedSeconds = 0;
      this.hoverCellX = this.hoverCellY = -1;
   }

   private void selectPreset(int idx) {
      this.difficulty = idx;
      int[] p = PRESETS[idx];
      this.newGame(p[0], p[1], p[2]);
   }

   private void applyCustom(int w, int h, int m) {
      this.difficulty = 3;
      this.newGame(w, h, m);
   }

   private void cellAction(int x, int y) {
      if (this.state != MinesweeperPage.GameState.WON && this.state != MinesweeperPage.GameState.LOST) {
         if (this.toolMode == 1) {
            this.board.toggleFlag(x, y);
            MsSounds.play((SoundEvent)MsSounds.CLICK.get());
         } else if (this.toolMode == 2) {
            this.board.toggleQuestion(x, y);
            MsSounds.play((SoundEvent)MsSounds.CLICK.get());
         } else {
            int before = this.board.revealedCount();
            if (this.board.revealedNumber(x, y) >= 0) {
               this.board.chord(x, y);
            } else {
               this.board.reveal(x, y);
            }

            int after = this.board.revealedCount();
            if (after != before || this.board.lost() || this.board.won()) {
               if (this.state == MinesweeperPage.GameState.READY) {
                  this.state = MinesweeperPage.GameState.PLAYING;
                  this.startedAt = System.currentTimeMillis();
                  this.elapsedSeconds = 0;
               }

               MsSounds.play((SoundEvent)MsSounds.CLICK.get());
            }

            this.afterAction();
         }
      }
   }

   private void afterAction() {
      if (this.board.won() && this.state != MinesweeperPage.GameState.WON) {
         this.state = MinesweeperPage.GameState.WON;
         MsSounds.play((SoundEvent)MsSounds.WIN.get());
         if (this.difficulty != 3) {
            MsConfig.get().recordBest(this.difficulty, this.elapsedSeconds);
         }
      } else if (this.board.lost() && this.state != MinesweeperPage.GameState.LOST) {
         this.state = MinesweeperPage.GameState.LOST;
         MsSounds.play((SoundEvent)MsSounds.LOSE.get());
      }
   }

   private void updateTimer() {
      if (this.state == MinesweeperPage.GameState.PLAYING) {
         int s = (int)((System.currentTimeMillis() - this.startedAt) / 1000L);
         this.elapsedSeconds = Math.min(999, s);
      }
   }

   private void menuAction(int item) {
      this.menuOpen = false;
      MsSounds.play((SoundEvent)MsSounds.CLICK.get());
      switch (item) {
         case 0:
            this.newGame(this.cols, this.rows, this.mines);
            break;
         case 1:
            this.selectPreset(0);
            break;
         case 2:
            this.selectPreset(1);
            break;
         case 3:
            this.selectPreset(2);
            break;
         case 4:
            this.custom.init(this.cols, this.rows, this.mines);
            this.view = MinesweeperPage.View.CUSTOM;
            break;
         case 5:
            MsConfig.get().colors = !MsConfig.get().colors;
            MsConfig.save();
            break;
         case 6:
            MsConfig.get().sound = !MsConfig.get().sound;
            MsConfig.save();
            break;
         case 7:
            this.view = MinesweeperPage.View.HERO;
            break;
         case 8:
            this.view = MinesweeperPage.View.ABOUT;
            break;
      }
   }

   private boolean menuItemChecked(int item) {
      MsConfig cfg = MsConfig.get();

      return switch (item) {
         case 1 -> this.difficulty == 0;
         case 2 -> this.difficulty == 1;
         case 3 -> this.difficulty == 2;
         case 4 -> this.difficulty == 3;
         case 5 -> cfg.colors;
         case 6 -> cfg.sound;
         default -> false;
      };
   }

   private int menuHeight() {
      // 9 个菜单项 × 10 + 4 条分隔线 × 4 = 106（版本项不顶出菜单下边）
      return 106;
   }

   private int menuItemAt(int dy) {
      int acc = 0;

      for (int s : MENU_SCHEDULE) {
         int h = s < 0 ? 4 : 10;
         if (dy >= acc && dy < acc + h) {
            return s;
         }

         acc += h;
      }

      return -1;
   }

   private MinesweeperPage.Layout layout(PhoneCanvas c) {
      int c0x = c.x();
      int c0y = c.y();
      int c0w = c.width();
      int c0h = c.height();
      // 窗口完全跟随画布：宽、高都不写死，画布多大窗口就多大，内容在其内自适应
      int cx = c0x;
      int cy = c0y;
      // 自适应布局（宽高都不写死）：格子按宽/高两个方向独立计算，棋盘尽量填满可用区域；
      // 拉伸比限幅 1.2 倍（视觉上仍近正方形，1.0.20 的 1.5 倍被用户否掉）。
      int menuBarY = cy + 12;
      // 计数行上沿：凸起线 [counterY-1=24,26] 紧贴旗子按钮下沿(24)，黑底顶 counterY+1=26
      int counterY = menuBarY + 13;
      int availW = c0w - 2;   // 左右凹槽内边各留 1px（竖屏初级保持最大 117px 满宽）
      // 可用高度 = 计数行下沿到画布底再留上下凹槽：全部用画布内相对坐标（counterY-c0y=25），
      // 避免 c0y≠0 时把绝对坐标混进相对尺寸减法导致可用高度算错
      int availH = c0h - (counterY - c0y) - 25 - 4;
      // 正方形格子：取宽高两个方向能同时放下的最大格，保证格子方正（用户明确要求不要扁格子）
      int cell = Math.max(3, Math.min(availW / this.cols, availH / this.rows));
      // 保证棋盘+左右凹槽(各2px) 不超出画布：手机窄画布(120px) 下初级棋盘 117px 会把凹槽挤到贴边，
      // 导致雷区看起来超出凹槽 1px。此约束随画布宽度自适应，任何设备上棋盘都居中且凹槽四边对称。
      int maxCellForPad = (c0w - 4) / this.cols;
      if (cell > maxCellForPad) {
         cell = Math.max(3, maxCellForPad);
      }
      int cellW = cell;
      int cellH = cell;

      int boardW = this.cols * cellW;
      int boardH = this.rows * cellH;
      winX = cx;
      winY = cy;
      winW = c0w;
      winH = c0h;

      int boardX = cx + (c0w - boardW) / 2;
      // 棋盘顶贴计数行下沿(50)下方 2px 凹槽上边框：原版紧凑风格。
      // 注意：counterY 已含 cy，这里绝不能再加 cy（曾导致棋盘整体下移、计数行到棋盘出现大空隙）
      int boardY = counterY + 25 + 2;
      int smileySize = 21;
      int smileyX = cx + c0w / 2 - smileySize / 2;
      // 计数行下沿上提：笑脸回退到与黑底顶部对齐（counterY+1，底 53）
      int smileyY = counterY + 1;
      int leftCX = (cx + 6 + smileyX - 7) / 2;
      int rightCX = (smileyX + smileySize + 7 + cx + c0w - 6) / 2;
      int minesX = leftCX - 14;   // 相对居中位再向中间 2px
      int timerX = rightCX - 18;  // 相对居中位再向中间 2px
      int btnSize = 12;
      int flagBtnX = cx + c0w - btnSize - 3;
      int qBtnX = flagBtnX - btnSize - 2;
      int btnY = menuBarY + (13 - btnSize) / 2;
      int closeX = cx + c0w - 19;
      int closeY = cy + 2;
      // 每 100 帧输出一次布局（验证版本是否生效：正方形格子自适应）
      if (layoutFrame++ % 100 == 0) {
         LOGGER.info(
            "[MinesweeperLayout v"
               + VERSION
               + "] canvas="
               + c0x
               + ","
               + c0y
               + " "
               + c0w
               + "x"
               + c0h
               + " cellW="
               + cellW
               + " cellH="
               + cellH
               + " board="
               + boardW
               + "x"
               + boardH
               + " boardY="
               + boardY
               + " counterY="
               + counterY
         );
      }
      return new MinesweeperPage.Layout(
         cx,
         cy,
         c0w,
         c0h,
         menuBarY,
         13,
         qBtnX,
         btnY,
         flagBtnX,
         btnY,
         btnSize,
         closeX,
         closeY,
         counterY,
         minesX,
         timerX,
         smileyX,
         smileyY,
         boardX,
         boardY,
         cellW,
         cellH,
         boardW,
         boardH
      );
   }

   private void computeHover(PhoneCanvas c, MinesweeperPage.Layout lay) {
      int mx = c.mouseX();
      int my = c.mouseY();
      this.hoverSmiley = mx >= lay.smileyX && mx < lay.smileyX + 21 && my >= lay.smileyY && my < lay.smileyY + 21;
      this.hoverFlagBtn = mx >= lay.flagBtnX && mx < lay.flagBtnX + lay.btnSize && my >= lay.flagBtnY && my < lay.flagBtnY + lay.btnSize;
      this.hoverQBtn = mx >= lay.qBtnX && mx < lay.qBtnX + lay.btnSize && my >= lay.qBtnY && my < lay.qBtnY + lay.btnSize;
      this.hoverCloseBtn = mx >= lay.closeX && mx < lay.closeX + 16 && my >= lay.closeY && my < lay.closeY + 11;
      this.hoverGameMenu = mx >= lay.cx + 2 && mx < lay.cx + 2 + 46 && my >= lay.menuBarY && my < lay.menuBarY + lay.menuBarH;
      this.hoverCellX = this.hoverCellY = -1;
      if (mx >= lay.boardX && mx < lay.boardX + lay.boardW && my >= lay.boardY && my < lay.boardY + lay.boardH) {
         int x = (mx - lay.boardX) / lay.cellW;
         int y = (my - lay.boardY) / lay.cellH;
         if (x < this.cols && y < this.rows) {
            this.hoverCellX = x;
            this.hoverCellY = y;
         }
      }

      this.hoverMenuItem = -1;
      if (this.menuOpen) {
         int dy = my - (lay.menuBarY + lay.menuBarH);
         if (mx >= lay.cx + 2 && mx < lay.cx + 2 + 86 && dy >= 0 && dy < this.menuHeight()) {
            this.hoverMenuItem = this.menuItemAt(dy);
         }
      }
   }

   public void render(PhoneCanvas c) {
      this.updateTimer();
      this.cur = this.layout(c);
      this.computeHover(c, this.cur);
      GuiGraphics g = c.graphics();
      g.fill(c.x(), c.y(), c.x() + c.width(), c.y() + c.height(), -4144960);
      switch (this.view) {
         case GAME:
            this.renderGame(c, g);
            break;
         case CUSTOM:
            this.renderGame(c, g);
            this.custom.render(c, g);
            break;
         case HERO:
            this.renderGame(c, g);
            this.hero.render(c, g);
            break;
         case ABOUT:
            this.renderGame(c, g);
            this.about.render(c, g);
      }
   }

   private void renderGame(PhoneCanvas c, GuiGraphics g) {
      boolean colors = MsConfig.get().colors;
      MinesweeperPage.Layout lay = this.cur;
      g.fill(lay.cx, lay.cy, lay.cx + lay.cw, lay.cy + lay.ch, -4144960);
      int bx1 = lay.cx;
      int by1 = lay.cy;
      int bx2 = lay.cx + lay.cw;
      int by2 = lay.cy + lay.ch;
      g.fill(bx1, by1, bx2, by1 + 1, -16777216);
      g.fill(bx1, by2 - 1, bx2, by2, -16777216);
      g.fill(bx1, by1, bx1 + 1, by2, -16777216);
      g.fill(bx2 - 1, by1, bx2, by2, -16777216);
      g.fill(bx1 + 1, by1 + 1, bx2 - 1, by1 + 2, -1);
      g.fill(bx1 + 1, by1 + 1, bx1 + 2, by2 - 2, -1);
      g.fill(bx2 - 2, by1 + 2, bx2 - 1, by2 - 1, -8355712);
      g.fill(bx1 + 1, by2 - 2, bx2 - 1, by2 - 1, -8355712);
      MsAtlas.drawTitlebar(g, lay.cx + 2, lay.cy + 2, lay.cw - 4, 12);
      g.drawCenteredString(c.font(), I18n.get("mcphone_minesweeper.app.minesweeper", new Object[0]), lay.cx + 2 + (lay.cw - 2 - 20) / 2, lay.cy + 3, -1);
      MsAtlas.drawCloseBtn(g, lay.closeX, lay.closeY, 11, this.hoverCloseBtn);
      g.fill(lay.cx + 2, lay.cy + 12, lay.cx + lay.cw - 2, lay.cy + 12 + 1, -2562824);
      drawRaisedLine(g, lay.cx + 2, lay.menuBarY - 1, lay.cx + lay.cw - 2);
      drawRaisedLine(g, lay.cx + 2, lay.counterY - 1, lay.cx + lay.cw - 2);
      // 计数行下沿分隔线：上提到笑脸下沿（counterY+22）后，按用户要求下移 1px（counterY+23），与雷区解绑
      drawRaisedLine(g, lay.cx + 2, lay.counterY + 23, lay.cx + lay.cw - 2);
      this.renderMenuBar(c, g, colors);
      int minesLeft = this.mines - this.board.flagCount();
      drawSunkenDigitArea(g, lay.minesX - 2, lay.counterY + 1, 36, 21);
      drawSunkenDigitArea(g, lay.timerX - 2, lay.counterY + 1, 36, 21);
      this.drawNumber(g, colors, lay.minesX, lay.counterY + 3, minesLeft);
      this.drawNumber(g, colors, lay.timerX, lay.counterY + 3, this.elapsedSeconds);
      drawRaisedRect(g, lay.smileyX - 1, lay.smileyY - 1, 23, 23);
      MsAtlas.drawFace(g, colors, lay.smileyX, lay.smileyY, 21, this.faceIndex());
      // 凹槽：严格包住棋盘（上下左右各 2px），棋盘在凹槽正中，上下对称、紧贴雷区
      int gx = Math.max(lay.cx, lay.boardX - 2);
      int gy = lay.boardY - 2;
      int gw = Math.min(lay.boardW + 4, lay.cx + lay.cw - gx);
      // 凹槽高度 = 棋盘高 + 上下各 2px（不再延伸到底部），保证凹槽上下边框与雷区对齐
      int gh = lay.boardH + 4;
      g.fill(gx, gy, gx + gw, gy + 1, -8355712);
      g.fill(gx, gy + gh - 1, gx + gw, gy + gh, -1);
      g.fill(gx, gy, gx + 1, gy + gh, -8355712);
      g.fill(gx + gw - 1, gy, gx + gw, gy + gh, -1);
      g.fill(gx + 1, gy + 1, gx + gw - 1, gy + 2, -16777216);
      g.fill(gx + 1, gy + gh - 2, gx + gw - 1, gy + gh - 1, -2039584);
      g.fill(gx + 1, gy + 1, gx + 2, gy + gh - 1, -16777216);
      g.fill(gx + gw - 2, gy + 1, gx + gw - 1, gy + gh - 1, -2039584);

      for (int y = 0; y < this.rows; y++) {
         for (int x = 0; x < this.cols; x++) {
            MsAtlas.drawBlock(g, colors, lay.boardX + x * lay.cellW, lay.boardY + y * lay.cellH, lay.cellW, lay.cellH, this.tileFor(x, y));
         }
      }

      if (this.menuOpen) {
         this.renderMenu(c, g);
      }
   }

   private static void drawRaisedLine(GuiGraphics g, int x1, int y, int x2) {
      g.fill(x1, y, x2, y + 1, -1);
      g.fill(x1, y + 1, x2, y + 2, -8355712);
   }

   private static void drawRaisedRect(GuiGraphics g, int x, int y, int w, int h) {
      g.fill(x, y, x + w, y + 1, -16777216);
      g.fill(x, y + h - 1, x + w, y + h, -16777216);
      g.fill(x, y, x + 1, y + h, -16777216);
      g.fill(x + w - 1, y, x + w, y + h, -16777216);
      g.fill(x + 1, y + 1, x + w - 1, y + 2, -1);
      g.fill(x + 1, y + 1, x + 2, y + h - 2, -1);
      g.fill(x + w - 2, y + 2, x + w - 1, y + h - 1, -8355712);
      g.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, -8355712);
   }

   private static void drawSunkenDigitArea(GuiGraphics g, int x, int y, int w, int h) {
      g.fill(x, y, x + w, y + h, -16777216);
      g.fill(x, y, x + w, y + 1, -8355712);
      g.fill(x, y, x + 1, y + h, -8355712);
      g.fill(x + w - 1, y + 1, x + w, y + h, -1);
      g.fill(x, y + h - 1, x + w, y + h, -1);
   }

   private void renderMenuBar(PhoneCanvas c, GuiGraphics g, boolean colors) {
      MinesweeperPage.Layout lay = this.cur;
      int my = lay.menuBarY;
      g.fill(lay.cx + 2, my, lay.cx + lay.cw - 2, my + lay.menuBarH, -4144960);
      boolean hl = this.menuOpen || this.hoverGameMenu;
      int tx = lay.cx + 3;
      if (hl) {
         g.fill(tx - 1, my, tx + 45, my + lay.menuBarH, -13538619);
         g.drawString(c.font(), I18n.get("mcphone_minesweeper.game", new Object[0]), tx + 1, my + 2, -1, false);
      } else {
         g.drawString(c.font(), I18n.get("mcphone_minesweeper.game", new Object[0]), tx + 1, my + 2, -16777216, false);
      }

      if (this.toolMode == 2) {
         MsAtlas.drawBlock(g, colors, lay.qBtnX, lay.qBtnY + 1, lay.btnSize, 15);
         MsAtlas.drawBlock(g, colors, lay.qBtnX, lay.qBtnY + 1, lay.btnSize, 2);
         drawSunkenEdge(g, lay.qBtnX, lay.qBtnY + 1, lay.btnSize);
      } else {
         MsAtlas.drawBlock(g, colors, lay.qBtnX, lay.qBtnY, lay.btnSize, 0);
         MsAtlas.drawBlock(g, colors, lay.qBtnX, lay.qBtnY, lay.btnSize, 2);
      }

      if (this.toolMode == 1) {
         MsAtlas.drawBlock(g, colors, lay.flagBtnX, lay.flagBtnY + 1, lay.btnSize, 15);
         MsAtlas.drawBlock(g, colors, lay.flagBtnX, lay.flagBtnY + 1, lay.btnSize, 1);
         drawSunkenEdge(g, lay.flagBtnX, lay.flagBtnY + 1, lay.btnSize);
      } else {
         MsAtlas.drawBlock(g, colors, lay.flagBtnX, lay.flagBtnY, lay.btnSize, 0);
         MsAtlas.drawBlock(g, colors, lay.flagBtnX, lay.flagBtnY, lay.btnSize, 1);
      }
   }

   private static void drawSunkenEdge(GuiGraphics g, int x, int y, int size) {
      g.fill(x + 1, y + 1, x + size - 1, y + 2, -8355712);
      g.fill(x + 1, y + size - 2, x + size - 1, y + size - 1, -1);
      g.fill(x + 1, y + 1, x + 2, y + size - 1, -8355712);
      g.fill(x + size - 2, y + 1, x + size - 1, y + size - 1, -1);
   }

   private void renderMenu(PhoneCanvas c, GuiGraphics g) {
      MinesweeperPage.Layout lay = this.cur;
      int mx = lay.cx + 2;
      int my = lay.menuBarY + lay.menuBarH;
      int mh = this.menuHeight();
      g.fill(mx, my, mx + 86, my + mh, -1);
      g.fill(mx, my, mx + 86, my + 1, -16777216);
      g.fill(mx, my + mh - 1, mx + 86, my + mh, -16777216);
      g.fill(mx, my, mx + 1, my + mh, -16777216);
      g.fill(mx + 86 - 1, my, mx + 86, my + mh, -16777216);
      int y = my;

      for (int s : MENU_SCHEDULE) {
         if (s < 0) {
            g.fill(mx + 2, y + 1, mx + 86 - 2, y + 2, -8355712);
            y += 4;
         } else {
            boolean hl = this.hoverMenuItem == s;
            if (hl) {
               g.fill(mx + 1, y, mx + 86 - 1, y + 10, -13538619);
            }

            if (this.menuItemChecked(s)) {
               drawCheck(g, mx + 4, y + 3, hl ? -1 : -16777216);
            }

            // 版本项直接显示全局版本号常量（唯一来源），不经过 lang，避免与其他版本号脱节
            String label = s == 8 ? "v" + VERSION : I18n.get(MENU_KEYS[s], new Object[0]);
            g.drawString(c.font(), label, mx + 12, y + 1, hl ? -1 : -16777216, false);
            y += 10;
         }
      }
   }

   private static void drawCheck(GuiGraphics g, int x, int y, int color) {
      g.fill(x + 2, y, x + 4, y + 1, color);
      g.fill(x + 1, y + 1, x + 5, y + 2, color);
      g.fill(x, y + 2, x + 6, y + 4, color);
      g.fill(x + 1, y + 4, x + 5, y + 5, color);
      g.fill(x + 2, y + 5, x + 4, y + 6, color);
   }

   private int faceIndex() {
      if (this.state == MinesweeperPage.GameState.WON) {
         return 1;
      } else if (this.state == MinesweeperPage.GameState.LOST) {
         return 2;
      } else if (this.hoverSmiley) {
         return 0;
      } else {
         return this.hoverCellX >= 0 && this.hoverCellY >= 0 && this.state != MinesweeperPage.GameState.WON && this.state != MinesweeperPage.GameState.LOST
            ? 3
            : 4;
      }
   }

   private int tileFor(int x, int y) {
      Board b = this.board;
      int st = b.state(x, y);
      switch (st) {
         case 0:
            if (b.lost() && b.isMine(x, y)) {
               return 5;
            } else {
               if (!b.lost() && this.hoverCellX == x && this.hoverCellY == y) {
                  return 15;
               }

               return 0;
            }
         case 1:
            if (b.lost() && !b.isMine(x, y)) {
               return 4;
            }

            return 1;
         case 2:
            if (!b.lost() && !b.won() && this.hoverCellX == x && this.hoverCellY == y) {
               return 6;
            }

            return 2;
         default:
            if (b.isMine(x, y)) {
               return b.explodedIndex() == y * this.cols + x ? 3 : 5;
            } else {
               int n = b.revealedNumber(x, y);
               return n <= 0 ? 15 : 15 - n;
            }
      }
   }

   private void drawNumber(GuiGraphics g, boolean colors, int x, int y, int value) {
      int d0;
      int d1;
      int d2;
      if (value < 0) {
         int v = -value;
         d0 = 0;   // 帧0 = 负号（图集帧序：0负号 1空 2..11=9..0）
         d1 = MsAtlas.digitIndex(v / 10 % 10);
         d2 = MsAtlas.digitIndex(v % 10);
      } else {
         d0 = MsAtlas.digitIndex(value / 100 % 10);
         d1 = MsAtlas.digitIndex(value / 10 % 10);
         d2 = MsAtlas.digitIndex(value % 10);
      }

      MsAtlas.drawDigit(g, colors, x, y, 10, 16, d0);
      MsAtlas.drawDigit(g, colors, x + 11, y, 10, 16, d1);
      MsAtlas.drawDigit(g, colors, x + 22, y, 10, 16, d2);
   }

   private void renderCustom(PhoneCanvas c, GuiGraphics g) {
      this.custom.render(c, g);
   }

   private void renderHero(PhoneCanvas c, GuiGraphics g) {
      this.hero.render(c, g);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      int mx = (int)Math.floor(mouseX);
      int my = (int)Math.floor(mouseY);
      MinesweeperPage.Layout lay = this.cur;
      if (lay == null) {
         return true;
      } else if (button == 1) {
         if (this.view == MinesweeperPage.View.GAME
            && !this.menuOpen
            && this.state != MinesweeperPage.GameState.WON
            && this.state != MinesweeperPage.GameState.LOST) {
            int[] cell = this.boardCellAt(mx, my, lay);
            if (cell != null) {
               this.board.cycleFlag(cell[0], cell[1]);
               MsSounds.play((SoundEvent)MsSounds.CLICK.get());
            }
         }

         return true;
      } else if (button != 0) {
         return true;
      } else {
         switch (this.view) {
            case CUSTOM:
               if (this.custom.closeClicked(mx, my)) {
                  this.view = MinesweeperPage.View.GAME;
                  return true;
               } else if (this.custom.okClicked(mx, my)) {
                  int w = MinesweeperPage.CustomDialog.clamp(parseNum(this.custom.values[1]), 2, 30);
                  int h = MinesweeperPage.CustomDialog.clamp(parseNum(this.custom.values[0]), 2, 24);
                  int m = MinesweeperPage.CustomDialog.clamp(parseNum(this.custom.values[2]), 1, w * h - 1);
                  this.applyCustom(w, h, m);
                  this.view = MinesweeperPage.View.GAME;
                  return true;
               } else {
                  if (this.custom.cancelClicked(mx, my)) {
                     this.view = MinesweeperPage.View.GAME;
                     return true;
                  }

                  this.custom.mouseClicked(mx, my);
                  return true;
               }
            case HERO:
               if (this.hero.closeClicked(mx, my)) {
                  this.view = MinesweeperPage.View.GAME;
                  return true;
               } else if (this.hero.okClicked(mx, my)) {
                  this.view = MinesweeperPage.View.GAME;
                  return true;
               } else {
                  if (this.hero.resetClicked(mx, my)) {
                     MsConfig.get().resetBests();
                     return true;
                  }

                  return true;
               }
            case ABOUT:
               if (this.about.closeClicked(mx, my)) {
                  this.view = MinesweeperPage.View.GAME;
                  return true;
               } else if (this.about.okClicked(mx, my)) {
                  this.view = MinesweeperPage.View.GAME;
                  return true;
               } else {
                  return true;
               }
            default:
               if (this.menuOpen) {
                  if (mx >= lay.cx + 2 && mx < lay.cx + 2 + 86 && my >= lay.menuBarY + lay.menuBarH && my < lay.menuBarY + lay.menuBarH + this.menuHeight()) {
                     int item = this.menuItemAt(my - (lay.menuBarY + lay.menuBarH));
                     if (item >= 0) {
                        this.menuAction(item);
                     }

                     return true;
                  } else {
                     this.menuOpen = false;
                     return true;
                  }
               } else if (mx >= lay.cx + 2 && mx < lay.cx + 2 + 46 && my >= lay.menuBarY && my < lay.menuBarY + lay.menuBarH) {
                  this.menuOpen = true;
                  MsSounds.play((SoundEvent)MsSounds.CLICK.get());
                  return true;
               } else if (mx >= lay.qBtnX && mx < lay.qBtnX + lay.btnSize && my >= lay.qBtnY && my < lay.qBtnY + lay.btnSize) {
                  this.toolMode = this.toolMode == 2 ? 0 : 2;
                  MsSounds.play((SoundEvent)MsSounds.CLICK.get());
                  return true;
               } else if (mx >= lay.flagBtnX && mx < lay.flagBtnX + lay.btnSize && my >= lay.flagBtnY && my < lay.flagBtnY + lay.btnSize) {
                  this.toolMode = this.toolMode == 1 ? 0 : 1;
                  MsSounds.play((SoundEvent)MsSounds.CLICK.get());
                  return true;
               } else if (mx >= lay.closeX && mx < lay.closeX + 16 && my >= lay.closeY && my < lay.closeY + 11) {
                  MsSounds.play((SoundEvent)MsSounds.CLICK.get());
                  this.closePage();
                  return true;
               } else if (mx >= lay.smileyX && mx < lay.smileyX + 21 && my >= lay.smileyY && my < lay.smileyY + 21) {
                  this.newGame(this.cols, this.rows, this.mines);
                  MsSounds.play((SoundEvent)MsSounds.CLICK.get());
                  return true;
               } else {
                  int[] cell = this.boardCellAt(mx, my, lay);
                  if (cell != null) {
                     this.cellAction(cell[0], cell[1]);
                     return true;
                  } else {
                     return true;
                  }
               }
         }
      }
   }

   private void closePage() {
      // X 按钮＝关闭手机。只走原版 Screen API（setScreen(null) 会触发 PhoneScreen 的 onClose 链，
      // 页面 onClose() 照常被调用），不引用 mcphone 的 core 内部类（wiki：仅 api 包对外）。
      Minecraft.getInstance().setScreen(null);
   }

   private int[] boardCellAt(int mx, int my, MinesweeperPage.Layout lay) {
      if (mx >= lay.boardX && mx < lay.boardX + lay.boardW && my >= lay.boardY && my < lay.boardY + lay.boardH) {
         int x = (mx - lay.boardX) / lay.cellW;
         int y = (my - lay.boardY) / lay.cellH;
         if (x >= 0 && x < this.cols && y >= 0 && y < this.rows) {
            return new int[]{x, y};
         }
      }

      return null;
   }

   private static int parseNum(String s) {
      try {
         return Integer.parseInt(s);
      } catch (NumberFormatException var2) {
         return 0;
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      return true;
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.view == MinesweeperPage.View.CUSTOM) {
         this.custom.keyPressed(keyCode);
         return true;
      } else {
         return false;
      }
   }

   public boolean charTyped(char codePoint, int modifiers) {
      if (this.view == MinesweeperPage.View.CUSTOM) {
         this.custom.charTyped(codePoint);
         return true;
      } else {
         return false;
      }
   }

   public boolean capturesKeyboard() {
      return this.view == MinesweeperPage.View.CUSTOM;
   }

   public boolean onBack() {
      if (this.view == MinesweeperPage.View.CUSTOM || this.view == MinesweeperPage.View.HERO) {
         this.view = MinesweeperPage.View.GAME;
         return true;
      } else if (this.menuOpen) {
         this.menuOpen = false;
         return true;
      } else {
         return false;
      }
   }

   public void onOpen() {
   }

   public void onClose() {
   }

   private static final class CustomDialog {
      private final String[] values = new String[3];
      private final int[] nums = new int[3];
      private int focused = -1;
      private int boxX;
      private int boxY;
      private int boxW;
      private int boxH;
      private int closeX;
      private int closeY;
      private boolean hoverClose;
      private int fieldY0;
      private int okX;
      private int okY;
      private int cancelX;
      private int cancelY;
      private static final int MIN = 2;
      private static final int W_MAX = 30;
      private static final int H_MAX = 24;

      private void init(int w, int h, int m) {
         this.nums[0] = h;
         this.nums[1] = w;
         this.nums[2] = m;

         for (int i = 0; i < 3; i++) {
            this.values[i] = Integer.toString(this.nums[i]);
         }

         this.focused = -1;
      }

      private int minOf(int field) {
         return field == 2 ? 1 : 2;
      }

      private int maxOf(int field) {
         if (field == 2) {
            int w = clamp(this.nums[1], 2, 30);
            int h = clamp(this.nums[0], 2, 24);
            return w * h - 1;
         } else {
            return field == 0 ? 24 : 30;
         }
      }

      private static int clamp(int v, int lo, int hi) {
         return Math.max(lo, Math.min(hi, v));
      }

      private void nudge(int field, int delta) {
         int v = clamp(this.nums[field] + delta, this.minOf(field), this.maxOf(field));
         this.nums[field] = v;
         this.values[field] = Integer.toString(v);
      }

      private void layout(PhoneCanvas c) {
         this.boxW = Math.min(MinesweeperPage.winW - 8, 106);
         this.boxH = 74;
         this.boxX = MinesweeperPage.winX + (MinesweeperPage.winW - this.boxW) / 2;
         this.boxY = MinesweeperPage.winY + (MinesweeperPage.winH - this.boxH) / 2 - 8;
         this.closeX = this.boxX + this.boxW - 18;
         this.closeY = this.boxY + 1;
         this.fieldY0 = this.boxY + 16;
         this.okX = this.boxX + this.boxW - 56;
         this.okY = this.boxY + this.boxH - 14;
         this.cancelX = this.boxX + this.boxW - 28;
         this.cancelY = this.okY;
      }

      private boolean inBox(int mx, int my) {
         return mx >= this.boxX && mx < this.boxX + this.boxW && my >= this.boxY && my < this.boxY + this.boxH;
      }

      private boolean okClicked(int mx, int my) {
         return this.inBox(mx, my) && mx >= this.okX && mx < this.okX + 26 && my >= this.okY && my < this.okY + 10;
      }

      private boolean cancelClicked(int mx, int my) {
         return this.inBox(mx, my) && mx >= this.cancelX && mx < this.cancelX + 26 && my >= this.cancelY && my < this.cancelY + 10;
      }

      private boolean closeClicked(int mx, int my) {
         return this.inBox(mx, my) && mx >= this.closeX && mx < this.closeX + 16 && my >= this.closeY && my < this.closeY + 11;
      }

      private boolean mouseClicked(int mx, int my) {
         if (!this.inBox(mx, my)) {
            return true;
         } else if (!this.okClicked(mx, my) && !this.cancelClicked(mx, my) && !this.closeClicked(mx, my)) {
            for (int i = 0; i < 3; i++) {
               int y = this.fieldY0 + i * 15;
               int minusX = this.boxX + 34;
               int minusY = y - 1;
               int plusX = this.boxX + 77;
               int plusY = y - 1;
               if (mx >= minusX && mx < minusX + 10 && my >= minusY && my < minusY + 10) {
                  this.nudge(i, -1);
                  return true;
               }

               if (mx >= plusX && mx < plusX + 10 && my >= plusY && my < plusY + 10) {
                  this.nudge(i, 1);
                  return true;
               }
            }

            int row = -1;

            for (int i = 0; i < 3; i++) {
               int yx = this.fieldY0 + i * 15;
               if (my >= yx - 2 && my < yx + 10) {
                  int fx = this.boxX + 46;
                  if (mx >= fx - 1 && mx < fx + 29) {
                     row = i;
                  }
               }
            }

            this.focused = row;
            return true;
         } else {
            return true;
         }
      }

      private void charTyped(char ch) {
         if (this.focused >= 0) {
            if (ch >= '0' && ch <= '9') {
               String v = this.values[this.focused];
               if (v.length() < 3) {
                  this.values[this.focused] = v + ch;
                  this.nums[this.focused] = Integer.parseInt(this.values[this.focused]);
               }
            }
         }
      }

      private void keyPressed(int keyCode) {
         if (this.focused >= 0) {
            if (keyCode == 259) {
               String v = this.values[this.focused];
               if (!v.isEmpty()) {
                  this.values[this.focused] = v.substring(0, v.length() - 1);
                  this.nums[this.focused] = this.values[this.focused].isEmpty() ? this.minOf(this.focused) : Integer.parseInt(this.values[this.focused]);
               }
            }
         }
      }

      private void render(PhoneCanvas c, GuiGraphics g) {
         this.layout(c);
         int mx = c.mouseX();
         int my = c.mouseY();
         this.hoverClose = mx >= this.closeX && mx < this.closeX + 16 && my >= this.closeY && my < this.closeY + 11;
         int cx = this.boxX;
         int cy = this.boxY;
         int cw = this.boxW;
         int chh = this.boxH;
         g.fill(cx, cy, cx + cw, cy + 1, -16777216);
         g.fill(cx, cy + chh - 1, cx + cw, cy + chh, -16777216);
         g.fill(cx, cy, cx + 1, cy + chh, -16777216);
         g.fill(cx + cw - 1, cy, cx + cw, cy + chh, -16777216);
         g.fill(cx + 1, cy + 1, cx + cw - 1, cy + 2, -1);
         g.fill(cx + 1, cy + 1, cx + 2, cy + chh - 2, -1);
         g.fill(cx + cw - 2, cy + 2, cx + cw - 1, cy + chh - 1, -8355712);
         g.fill(cx + 1, cy + chh - 2, cx + cw - 1, cy + chh - 1, -8355712);
         g.fill(cx + 2, cy + 12 + 1, cx + cw - 2, cy + chh - 2, -4144960);
         MsAtlas.drawTitlebar(g, cx + 2, cy + 2, cw - 4, 12);
         g.drawCenteredString(c.font(), I18n.get("mcphone_minesweeper.dialog.title", new Object[0]), cx + 2 + (cw - 2 - 20) / 2, cy + 3, -1);
         MsAtlas.drawCloseBtn(g, this.closeX, this.closeY, 11, this.hoverClose);
         g.fill(cx + 2, cy + 12, cx + cw - 2, cy + 12 + 1, -2562824);
         String[] labels = new String[]{
            I18n.get("mcphone_minesweeper.dialog.height", new Object[0]),
            I18n.get("mcphone_minesweeper.dialog.width", new Object[0]),
            I18n.get("mcphone_minesweeper.dialog.mines", new Object[0])
         };

         for (int i = 0; i < 3; i++) {
            int y = this.fieldY0 + i * 15;
            g.drawString(c.font(), labels[i], cx + 3, y, -16777216, false);
            int fx = cx + 46;
            g.fill(fx - 1, y - 1, fx + 29, y + 8, -1);
            g.fill(fx - 1, y - 1, fx + 29, y, -8355712);
            g.fill(fx - 1, y + 7, fx + 29, y + 8, -8355712);
            g.fill(fx - 1, y - 1, fx, y + 8, -8355712);
            g.fill(fx + 28, y - 1, fx + 29, y + 8, -8355712);
            String shown = this.values[i];
            g.drawString(c.font(), shown, fx + 2, y, this.focused == i ? -13538619 : -16777216, false);
            drawStepper(g, cx + 34, y - 1, "-", c);
            drawStepper(g, cx + 77, y - 1, "+", c);
         }

         drawButton(g, this.okX, this.okY, 26, I18n.get("mcphone_minesweeper.dialog.ok", new Object[0]), c);
         drawButton(g, this.cancelX, this.cancelY, 26, I18n.get("mcphone_minesweeper.dialog.cancel", new Object[0]), c);
      }

      private static void drawStepper(GuiGraphics g, int x, int y, String glyph, PhoneCanvas c) {
         g.fill(x, y, x + 10, y + 10, -4144960);
         g.fill(x, y, x + 10, y + 1, -1);
         g.fill(x, y, x + 1, y + 10, -1);
         g.fill(x, y + 9, x + 10, y + 10, -8355712);
         g.fill(x + 9, y, x + 10, y + 10, -8355712);
         g.drawString(c.font(), glyph, x + 3, y + 1, -16777216, false);
      }

      static void drawButton(GuiGraphics g, int x, int y, int w, String label, PhoneCanvas c) {
         g.fill(x, y, x + w, y + 10, -4144960);
         g.fill(x, y, x + w, y + 1, -1);
         g.fill(x, y, x + 1, y + 10, -1);
         g.fill(x, y + 9, x + w, y + 10, -8355712);
         g.fill(x + w - 1, y, x + w, y + 10, -8355712);
         int tw = c.font().width(label);
         g.drawString(c.font(), label, x + (w - tw) / 2, y + 1, -16777216, false);
      }
   }

   private static enum GameState {
      READY,
      PLAYING,
      WON,
      LOST;
   }

   private static final class HeroDialog {
      private int boxX;
      private int boxY;
      private int boxW;
      private int boxH;
      private int closeX;
      private int closeY;
      private boolean hoverClose;
      private int resetX;
      private int resetY;
      private int okX;
      private int okY;

      private void layout(PhoneCanvas c) {
         this.boxW = Math.min(MinesweeperPage.winW - 8, 106);
         this.boxH = 64;
         this.boxX = MinesweeperPage.winX + (MinesweeperPage.winW - this.boxW) / 2;
         this.boxY = MinesweeperPage.winY + (MinesweeperPage.winH - this.boxH) / 2 - 10;
         this.closeX = this.boxX + this.boxW - 18;
         this.closeY = this.boxY + 1;
         this.resetX = this.boxX + 4;
         this.resetY = this.boxY + this.boxH - 15;
         this.okX = this.boxX + this.boxW - 30;
         this.okY = this.resetY;
      }

      private boolean okClicked(int mx, int my) {
         return this.inBox(mx, my) && mx >= this.okX && mx < this.okX + 26 && my >= this.okY && my < this.okY + 10;
      }

      private boolean resetClicked(int mx, int my) {
         return this.inBox(mx, my) && mx >= this.resetX && mx < this.resetX + 60 && my >= this.resetY && my < this.resetY + 10;
      }

      private boolean closeClicked(int mx, int my) {
         return this.inBox(mx, my) && mx >= this.closeX && mx < this.closeX + 16 && my >= this.closeY && my < this.closeY + 11;
      }

      private boolean inBox(int mx, int my) {
         return mx >= this.boxX && mx < this.boxX + this.boxW && my >= this.boxY && my < this.boxY + this.boxH;
      }

      private void render(PhoneCanvas c, GuiGraphics g) {
         this.layout(c);
         int mx = c.mouseX();
         int my = c.mouseY();
         this.hoverClose = mx >= this.closeX && mx < this.closeX + 16 && my >= this.closeY && my < this.closeY + 11;
         int cx = this.boxX;
         int cy = this.boxY;
         int cw = this.boxW;
         int chh = this.boxH;
         g.fill(cx, cy, cx + cw, cy + 1, -16777216);
         g.fill(cx, cy + chh - 1, cx + cw, cy + chh, -16777216);
         g.fill(cx, cy, cx + 1, cy + chh, -16777216);
         g.fill(cx + cw - 1, cy, cx + cw, cy + chh, -16777216);
         g.fill(cx + 1, cy + 1, cx + cw - 1, cy + 2, -1);
         g.fill(cx + 1, cy + 1, cx + 2, cy + chh - 2, -1);
         g.fill(cx + cw - 2, cy + 2, cx + cw - 1, cy + chh - 1, -8355712);
         g.fill(cx + 1, cy + chh - 2, cx + cw - 1, cy + chh - 1, -8355712);
         g.fill(cx + 2, cy + 12 + 1, cx + cw - 2, cy + chh - 2, -4144960);
         MsAtlas.drawTitlebar(g, cx + 2, cy + 2, cw - 4, 12);
         g.drawCenteredString(c.font(), I18n.get("mcphone_minesweeper.hero.title", new Object[0]), cx + 2 + (cw - 2 - 20) / 2, cy + 3, -1);
         MsAtlas.drawCloseBtn(g, this.closeX, this.closeY, 11, this.hoverClose);
         g.fill(cx + 2, cy + 12, cx + cw - 2, cy + 12 + 1, -2562824);
         MsConfig cfg = MsConfig.get();
         String[] names = new String[]{
            I18n.get("mcphone_minesweeper.hero.beginner", new Object[0]),
            I18n.get("mcphone_minesweeper.hero.intermediate", new Object[0]),
            I18n.get("mcphone_minesweeper.hero.expert", new Object[0])
         };

         for (int i = 0; i < 3; i++) {
            int y = cy + 15 + i * 12;
            String time = I18n.get("mcphone_minesweeper.hero.seconds", new Object[]{cfg.bestOf(i)});
            String anon = I18n.get("mcphone_minesweeper.hero.anon", new Object[0]);
            g.drawString(c.font(), names[i], cx + 4, y, -16777216, false);
            g.drawString(c.font(), time, cx + 46, y, -16777216, false);
            g.drawString(c.font(), anon, cx + 84, y, -8355712, false);
         }

         MinesweeperPage.CustomDialog.drawButton(g, this.resetX, this.resetY, 60, I18n.get("mcphone_minesweeper.hero.reset", new Object[0]), c);
         MinesweeperPage.CustomDialog.drawButton(g, this.okX, this.okY, 26, I18n.get("mcphone_minesweeper.hero.ok", new Object[0]), c);
      }
   }

   // 关于窗口：仿自定义/英雄榜对话框模板，内容参考 Windows"关于"对话框
   private static final class AboutDialog {
      private int boxX;
      private int boxY;
      private int boxW;
      private int boxH;
      private int closeX;
      private int closeY;
      private boolean hoverClose;
      private int okX;
      private int okY;

      private void layout(PhoneCanvas c) {
         this.boxW = Math.min(MinesweeperPage.winW - 8, 106);
         this.boxH = 76;
         this.boxX = MinesweeperPage.winX + (MinesweeperPage.winW - this.boxW) / 2;
         this.boxY = MinesweeperPage.winY + (MinesweeperPage.winH - this.boxH) / 2 - 8;
         this.closeX = this.boxX + this.boxW - 18;
         this.closeY = this.boxY + 1;
         this.okX = this.boxX + this.boxW - 30;
         this.okY = this.boxY + this.boxH - 15;
      }

      private boolean okClicked(int mx, int my) {
         return this.inBox(mx, my) && mx >= this.okX && mx < this.okX + 26 && my >= this.okY && my < this.okY + 10;
      }

      private boolean closeClicked(int mx, int my) {
         return this.inBox(mx, my) && mx >= this.closeX && mx < this.closeX + 16 && my >= this.closeY && my < this.closeY + 11;
      }

      private boolean inBox(int mx, int my) {
         return mx >= this.boxX && mx < this.boxX + this.boxW && my >= this.boxY && my < this.boxY + this.boxH;
      }

      private void render(PhoneCanvas c, GuiGraphics g) {
         this.layout(c);
         int mx = c.mouseX();
         int my = c.mouseY();
         this.hoverClose = mx >= this.closeX && mx < this.closeX + 16 && my >= this.closeY && my < this.closeY + 11;
         int cx = this.boxX;
         int cy = this.boxY;
         int cw = this.boxW;
         int chh = this.boxH;
         g.fill(cx, cy, cx + cw, cy + 1, -16777216);
         g.fill(cx, cy + chh - 1, cx + cw, cy + chh, -16777216);
         g.fill(cx, cy, cx + 1, cy + chh, -16777216);
         g.fill(cx + cw - 1, cy, cx + cw, cy + chh, -16777216);
         g.fill(cx + 1, cy + 1, cx + cw - 1, cy + 2, -1);
         g.fill(cx + 1, cy + 1, cx + 2, cy + chh - 2, -1);
         g.fill(cx + cw - 2, cy + 2, cx + cw - 1, cy + chh - 1, -8355712);
         g.fill(cx + 1, cy + chh - 2, cx + cw - 1, cy + chh - 1, -8355712);
         g.fill(cx + 2, cy + 12 + 1, cx + cw - 2, cy + chh - 2, -4144960);
         MsAtlas.drawTitlebar(g, cx + 2, cy + 2, cw - 4, 12);
         g.drawCenteredString(c.font(), I18n.get("mcphone_minesweeper.about.title", new Object[0]), cx + 2 + (cw - 2 - 20) / 2, cy + 3, -1);
         MsAtlas.drawCloseBtn(g, this.closeX, this.closeY, 11, this.hoverClose);
         g.fill(cx + 2, cy + 12, cx + cw - 2, cy + 12 + 1, -2562824);
         // 图标居中
         g.blit(MsAtlas.APP_ICON, cx + cw / 2 - 10, cy + 15, 20, 20, 0.0F, 0.0F, 48, 48, 48, 48);
         // MC phone（大标题，居中）
         g.drawCenteredString(c.font(), I18n.get("mcphone_minesweeper.about.name", new Object[0]), cx + 2 + (cw - 2 - 20) / 2, cy + 36, -16777216);
         // Minecraft phone（副标题，居中）
         g.drawCenteredString(c.font(), I18n.get("mcphone_minesweeper.about.subname", new Object[0]), cx + 2 + (cw - 2 - 20) / 2, cy + 45, -8355712);
         // 版本号（用全局常量，与其他位置唯一来源）
         g.drawString(c.font(), "v" + MinesweeperPage.VERSION, cx + 4, cy + 55, -16777216, false);
         // 版权
         g.drawString(c.font(), I18n.get("mcphone_minesweeper.about.copyright", new Object[0]), cx + 4, cy + 64, -8355712, false);
         MinesweeperPage.CustomDialog.drawButton(g, this.okX, this.okY, 26, I18n.get("mcphone_minesweeper.hero.ok", new Object[0]), c);
      }
   }

   private static record Layout(
      int cx,
      int cy,
      int cw,
      int ch,
      int menuBarY,
      int menuBarH,
      int qBtnX,
      int qBtnY,
      int flagBtnX,
      int flagBtnY,
      int btnSize,
      int closeX,
      int closeY,
      int counterY,
      int minesX,
      int timerX,
      int smileyX,
      int smileyY,
      int boardX,
      int boardY,
      int cellW,
      int cellH,
      int boardW,
      int boardH
   ) {
   }

   private static enum View {
      GAME,
      CUSTOM,
      HERO,
      ABOUT;
   }
}
