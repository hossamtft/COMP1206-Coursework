package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer
 * values arranged in a 2D arrow, with rows and columns.
 *
 * <p>Each value inside the Grid is an IntegerProperty can be bound to enable modification and
 * display of the contents of the grid.
 *
 * <p>The Grid contains functions related to modifying the model, for example, placing a piece
 * inside the grid.
 *
 * <p>The Grid should be linked to a GameBoard for it's display.
 */
public class Grid {

  private static final Logger logger = LogManager.getLogger(Grid.class);

  /** The number of columns in this grid */
  private final int cols;

  /** The number of rows in this grid */
  private final int rows;

  /** The grid is a 2D arrow with rows and columns of SimpleIntegerProperties. */
  private final SimpleIntegerProperty[][] grid;

  /**
   * Create a new Grid with the specified number of columns and rows and initialise them
   *
   * @param cols number of columns
   * @param rows number of rows
   */
  public Grid(int cols, int rows) {
    this.cols = cols;
    this.rows = rows;

    // Create the grid itself
    grid = new SimpleIntegerProperty[cols][rows];

    // Add a SimpleIntegerProperty to every block in the grid
    for (var y = 0; y < rows; y++) {
      for (var x = 0; x < cols; x++) {
        grid[x][y] = new SimpleIntegerProperty(0);
      }
    }
  }

  /**
   * Check if piece can be played on the grid
   *
   * @param gamePiece piece to be played
   * @param posX placement x
   * @param posY placement y
   * @return whether piece is playable on the grid or not
   */
  public boolean canPlayPiece(GamePiece gamePiece, int posX, int posY) {
    logger.info("Checking if we can be play the piece {} at {},{}", gamePiece, posX, posY);
    int topX = posX - 1;
    int topY = posY - 1;
    int[][] blocksOfPiece = gamePiece.getBlocks();
    for (var blockX = 0; blockX < blocksOfPiece.length; blockX++) {
      for (var blockY = 0; blockY < blocksOfPiece.length; blockY++) {
        var blockValue = blocksOfPiece[blockX][blockY];
        if (blockValue > 0) {
          var gridValue = get(topX + blockX, topY + blockY);
          if (gridValue != 0) {
            logger.info("Unable to place piece conflict at {},{}", posX + blockX, posY + blockY);
            return false;
          }
        }
      }
    }
    return true;
  }

  /**
   * Play gamePiece on the grid at pos x and pos y
   *
   * @param gamePiece
   * @param posX
   * @param posY
   */
  public void playPiece(GamePiece gamePiece, int posX, int posY) {
    logger.info("Playing the piece {} at {},{}", gamePiece, posX, posY);
    int topX = posX - 1;
    int topY = posY - 1;

    int value = gamePiece.getValue();
    int[][] blocksOfPiece = gamePiece.getBlocks();
    // return if piece can not be played
    if (!canPlayPiece(gamePiece, posX, posY)) return;
    for (var blockX = 0; blockX < blocksOfPiece.length; blockX++) {
      for (var blockY = 0; blockY < blocksOfPiece.length; blockY++) {
        var blockValue = blocksOfPiece[blockX][blockY];
        if (blockValue > 0) {
          set(topX + blockX, topY + blockY, value);
        }
      }
    }
  }

  /**
   * Get the Integer property contained inside the grid at a given row and column index. Can be used
   * for binding.
   *
   * @param x column
   * @param y row
   * @return the IntegerProperty at the given x and y in this grid
   */
  public IntegerProperty getGridProperty(int x, int y) {
    return grid[x][y];
  }

  /**
   * Update the value at the given x and y index within the grid
   *
   * @param x column
   * @param y row
   * @param value the new value
   */
  public void set(int x, int y, int value) {
    grid[x][y].set(value);
  }

  /**
   * Get the value represented at the given x and y index within the grid
   *
   * @param x column
   * @param y row
   * @return the value
   */
  public int get(int x, int y) {
    try {
      // Get the value held in the property at the x and y index provided
      return grid[x][y].get();
    } catch (ArrayIndexOutOfBoundsException e) {
      // No such index
      return -1;
    }
  }

  /**
   * Get the number of columns in this game
   *
   * @return number of columns
   */
  public int getCols() {
    return cols;
  }

  /**
   * Get the number of rows in this game
   *
   * @return number of rows
   */
  public int getRows() {
    return rows;
  }

  /** Clear the grid by setting all values to 0 */
  public void clear() {
    for (int x = 0; x < cols; x++) {
      for (int y = 0; y < rows; y++) {
        set(x, y, 0);
      }
    }
  }
}
