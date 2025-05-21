package dev.d9r.gameoflife.utility;

import java.util.Base64;

public class BinaryGridEncoder {
  /**
   * Encode a 2D boolean grid as a Base64 string.
   *
   * @param grid the grid to encode
   * @return a Base64 string representing the grid
   */
  public static String encode(boolean[][] grid) {
    int rows = grid.length;
    int cols = grid[0].length;

    int bytesNeeded = (rows * cols + 7) / 8;
    byte[] bytes = new byte[bytesNeeded];

    int bitIndex = 0;
    for (boolean[] booleans : grid) {
      for (int j = 0; j < cols; j++) {
        if (booleans[j]) {
          bytes[bitIndex / 8] |= (byte) (1 << (bitIndex % 8));
        }
        bitIndex++;
      }
    }

    return Base64.getEncoder().encodeToString(bytes);
  }
}
