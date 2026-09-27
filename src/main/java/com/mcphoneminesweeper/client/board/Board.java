package com.mcphoneminesweeper.client.board;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class Board {
   public static final int STATE_COVERED = 0;
   public static final int STATE_FLAG = 1;
   public static final int STATE_QUESTION = 2;
   public static final int STATE_REVEALED = 3;
   private final int width;
   private final int height;
   private final int mineCount;
   private boolean questionMarksAllowed;
   private boolean[] mines;
   private int[] numbers;
   private byte[] states;
   private boolean minesPlaced;
   private boolean lost;
   private boolean won;
   private int flagCount;
   private int revealedCount;
   private int explodedIndex = -1;
   private final Random random = new Random();

   public Board(int width, int height, int mineCount, boolean questionMarksAllowed) {
      if (width < 2 || height < 2) {
         throw new IllegalArgumentException("board too small: " + width + "x" + height);
      } else if (mineCount >= 1 && mineCount < width * height) {
         this.width = width;
         this.height = height;
         this.mineCount = mineCount;
         this.questionMarksAllowed = questionMarksAllowed;
         int cells = width * height;
         this.mines = new boolean[cells];
         this.numbers = new int[cells];
         this.states = new byte[cells];
      } else {
         throw new IllegalArgumentException("bad mine count: " + mineCount);
      }
   }

   public int width() {
      return this.width;
   }

   public int height() {
      return this.height;
   }

   public int mineCount() {
      return this.mineCount;
   }

   public int flagCount() {
      return this.flagCount;
   }

   public int revealedCount() {
      return this.revealedCount;
   }

   public boolean minesPlaced() {
      return this.minesPlaced;
   }

   public boolean lost() {
      return this.lost;
   }

   public boolean won() {
      return this.won;
   }

   public boolean questionMarksAllowed() {
      return this.questionMarksAllowed;
   }

   public void setQuestionMarksAllowed(boolean allowed) {
      this.questionMarksAllowed = allowed;
   }

   public int explodedIndex() {
      return this.explodedIndex;
   }

   private int idx(int x, int y) {
      return y * this.width + x;
   }

   public boolean inBounds(int x, int y) {
      return x >= 0 && x < this.width && y >= 0 && y < this.height;
   }

   public int state(int x, int y) {
      return this.states[this.idx(x, y)];
   }

   public boolean isMine(int x, int y) {
      return this.minesPlaced && this.mines[this.idx(x, y)];
   }

   public int revealedNumber(int x, int y) {
      int i = this.idx(x, y);
      return this.states[i] != 3 ? -1 : this.numbers[i];
   }

   private void placeMines(int sx, int sy) {
      int cells = this.width * this.height;
      List<Integer> forbidden = new ArrayList<>();

      for (int dy = -1; dy <= 1; dy++) {
         for (int dx = -1; dx <= 1; dx++) {
            int x = sx + dx;
            int y = sy + dy;
            if (this.inBounds(x, y)) {
               forbidden.add(this.idx(x, y));
            }
         }
      }

      if (cells - forbidden.size() < this.mineCount) {
         forbidden.clear();
         forbidden.add(this.idx(sx, sy));
      }

      boolean[] forbiddenSet = new boolean[cells];

      for (int i : forbidden) {
         forbiddenSet[i] = true;
      }

      int placed = 0;

      while (placed < this.mineCount) {
         int i = this.random.nextInt(cells);
         if (!this.mines[i] && !forbiddenSet[i]) {
            this.mines[i] = true;
            placed++;
         }
      }

      for (int y = 0; y < this.height; y++) {
         for (int x = 0; x < this.width; x++) {
            int i = this.idx(x, y);
            this.numbers[i] = this.mines[i] ? -1 : this.countNeighborMines(x, y);
         }
      }

      this.minesPlaced = true;
   }

   private int countNeighborMines(int x, int y) {
      int n = 0;

      for (int dy = -1; dy <= 1; dy++) {
         for (int dx = -1; dx <= 1; dx++) {
            if (dx != 0 || dy != 0) {
               int nx = x + dx;
               int ny = y + dy;
               if (this.inBounds(nx, ny) && this.mines[this.idx(nx, ny)]) {
                  n++;
               }
            }
         }
      }

      return n;
   }

   public int reveal(int x, int y) {
      if (this.inBounds(x, y) && !this.lost && !this.won) {
         if (!this.minesPlaced) {
            this.placeMines(x, y);
         }

         int i = this.idx(x, y);
         byte st = this.states[i];
         if (st == 1) {
            return 0;
         } else if (st == 3) {
            return 0;
         } else if (this.mines[i]) {
            this.explodedIndex = i;
            this.lost = true;
            this.states[i] = 3;
            return 1;
         } else {
            return this.floodReveal(x, y);
         }
      } else {
         return 0;
      }
   }

   public int chord(int x, int y) {
      if (this.inBounds(x, y) && !this.lost && !this.won) {
         int i = this.idx(x, y);
         if (this.states[i] == 3 && this.numbers[i] > 0) {
            int flagNeighbors = 0;

            for (int dy = -1; dy <= 1; dy++) {
               for (int dx = -1; dx <= 1; dx++) {
                  if (dx != 0 || dy != 0) {
                     int nx = x + dx;
                     int ny = y + dy;
                     if (this.inBounds(nx, ny) && this.states[this.idx(nx, ny)] == 1) {
                        flagNeighbors++;
                     }
                  }
               }
            }

            if (flagNeighbors != this.numbers[i]) {
               return 0;
            } else {
               int revealed = 0;

               for (int dy = -1; dy <= 1; dy++) {
                  for (int dxx = -1; dxx <= 1; dxx++) {
                     if (dxx != 0 || dy != 0) {
                        int nx = x + dxx;
                        int ny = y + dy;
                        if (this.inBounds(nx, ny)) {
                           int ni = this.idx(nx, ny);
                           byte nst = this.states[ni];
                           if (nst == 0 || nst == 2) {
                              if (this.mines[ni]) {
                                 this.explodedIndex = ni;
                                 this.lost = true;
                                 this.states[ni] = 3;
                                 revealed++;
                              } else {
                                 revealed += this.floodReveal(nx, ny);
                              }
                           }
                        }
                     }
                  }
               }

               return revealed;
            }
         } else {
            return 0;
         }
      } else {
         return 0;
      }
   }

   private int floodReveal(int x, int y) {
      int revealed = 0;
      List<int[]> stack = new ArrayList<>();
      stack.add(new int[]{x, y});

      while (!stack.isEmpty()) {
         int[] p = stack.remove(stack.size() - 1);
         int px = p[0];
         int py = p[1];
         if (this.inBounds(px, py)) {
            int i = this.idx(px, py);
            if (this.states[i] != 3 && this.states[i] != 1 && !this.mines[i]) {
               this.states[i] = 3;
               revealed++;
               if (this.numbers[i] == 0) {
                  for (int dy = -1; dy <= 1; dy++) {
                     for (int dx = -1; dx <= 1; dx++) {
                        if (dx != 0 || dy != 0) {
                           stack.add(new int[]{px + dx, py + dy});
                        }
                     }
                  }
               }
            }
         }
      }

      this.revealedCount += revealed;
      this.checkWin();
      return revealed;
   }

   public void cycleFlag(int x, int y) {
      if (this.inBounds(x, y) && !this.lost && !this.won) {
         int i = this.idx(x, y);
         switch (this.states[i]) {
            case 0:
               this.states[i] = 1;
               this.flagCount++;
               break;
            case 1:
               this.flagCount--;
               if (this.questionMarksAllowed) {
                  this.states[i] = 2;
               } else {
                  this.states[i] = 0;
               }
               break;
            case 2:
               this.states[i] = 0;
         }
      }
   }

   public void toggleFlag(int x, int y) {
      if (this.inBounds(x, y) && !this.lost && !this.won) {
         int i = this.idx(x, y);
         switch (this.states[i]) {
            case 0:
            case 2:
               this.states[i] = 1;
               this.flagCount++;
               break;
            case 1:
               this.states[i] = 0;
               this.flagCount--;
         }
      }
   }

   public void toggleQuestion(int x, int y) {
      if (this.inBounds(x, y) && !this.lost && !this.won) {
         int i = this.idx(x, y);
         switch (this.states[i]) {
            case 0:
               this.states[i] = 2;
               break;
            case 1:
               this.states[i] = 2;
               this.flagCount--;
               break;
            case 2:
               this.states[i] = 0;
         }
      }
   }

   public boolean isFlag(int x, int y) {
      return this.inBounds(x, y) && this.states[this.idx(x, y)] == 1;
   }

   public boolean isQuestion(int x, int y) {
      return this.inBounds(x, y) && this.states[this.idx(x, y)] == 2;
   }

   public boolean isRevealed(int x, int y) {
      return this.inBounds(x, y) && this.states[this.idx(x, y)] == 3;
   }

   private void checkWin() {
      if (this.revealedCount == this.width * this.height - this.mineCount) {
         this.won = true;

         for (int i = 0; i < this.states.length; i++) {
            if (this.mines[i] && this.states[i] == 0) {
               this.states[i] = 1;
               this.flagCount++;
            }
         }
      }
   }
}
