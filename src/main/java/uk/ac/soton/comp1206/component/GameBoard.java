package uk.ac.soton.comp1206.component;

import java.util.Set;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.BlockClickedListener;
import uk.ac.soton.comp1206.event.RightClickListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.Grid;

/**
 * A GameBoard is a visual component to represent the visual GameBoard. It extends a GridPane to
 * hold a grid of GameBlocks.
 *
 * <p>The GameBoard can hold an internal grid of it's own, for example, for displaying an upcoming
 * block. It also be linked to an external grid, for the main game board.
 *
 * <p>The GameBoard is only a visual representation and should not contain game logic or model logic
 * in it, which should take place in the Grid.
 */
public class GameBoard extends GridPane {

  private static final Logger logger = LogManager.getLogger(GameBoard.class);

  /** Number of columns in the board */
  private final int cols;

  /** Number of rows in the board */
  private final int rows;

  /** The visual width of the board - has to be specified due to being a Canvas */
  private final double width;

  /** The visual height of the board - has to be specified due to being a Canvas */
  private final double height;

  /** The grid this GameBoard represents */
  final Grid grid;

  /** The blocks inside the grid */
  GameBlock[][] blocks;

  /** The listener to call when a specific block is clicked */
  private BlockClickedListener blockClickedListener;

  private RightClickListener rightClickListener;
  private Game game;
  private GameBlock hoveredBlock;
  private int hoverX = -1;
  private int hoverY = -1;
  GameBlock mouse;
  GameBlock keyboard;

  /**
   * Create a new GameBoard, based off a given grid, with a visual width and height.
   *
   * @param grid linked grid
   * @param width the visual width
   * @param height the visual height
   */
  public GameBoard(Grid grid, double width, double height) {
    this.cols = grid.getCols();
    this.rows = grid.getRows();
    this.width = width;
    this.height = height;
    this.grid = grid;

    // Build the GameBoard
    build();
  }

  /**
   * Create a new GameBoard with it's own internal grid, specifying the number of columns and rows,
   * along with the visual width and height.
   *
   * @param cols number of columns for internal grid
   * @param rows number of rows for internal grid
   * @param width the visual width
   * @param height the visual height
   */
  public GameBoard(int cols, int rows, double width, double height) {
    this.cols = cols;
    this.rows = rows;
    this.width = width;
    this.height = height;
    this.grid = new Grid(cols, rows);

    // Build the GameBoard
    build();
  }

  /**
   * Set the game object related to the game board.
   *
   * @param game the game to manage
   */
  public void setGame(Game game) {
    this.game = game;
  }

  /**
   * Right click handler setter.
   *
   * @param handler the handler to set
   */
  public void setOnRightClick(RightClickListener handler) {
    rightClickListener = handler;
  }

  /**
   * Get a specific block from the GameBoard, specified by it's row and column
   *
   * @param x column
   * @param y row
   * @return game block at the given column and row
   */
  public GameBlock getBlock(int x, int y) {
    return blocks[x][y];
  }

  /** Build the GameBoard by creating a block at every x and y column and row */
  protected void build() {
    logger.info("Building grid: {} x {}", cols, rows);

    setMaxWidth(width);
    setMaxHeight(height);

    setGridLinesVisible(true);

    blocks = new GameBlock[cols][rows];

    for (var y = 0; y < rows; y++) {
      for (var x = 0; x < cols; x++) {
        createBlock(x, y);
      }
    }
  }

  public int getCols() {
    return cols;
  }

  public int getRows() {
    return rows;
  }

  /**
   * Create a block at the given x and y position in the GameBoard
   *
   * @param x column
   * @param y row
   */
  protected GameBlock createBlock(int x, int y) {
    var blockWidth = width / cols;
    var blockHeight = height / rows;

    // Create a new GameBlock UI component
    GameBlock block = new GameBlock(this, x, y, blockWidth, blockHeight);

    // Add to the GridPane
    add(block, x, y);

    // Add to our block directory
    blocks[x][y] = block;

    // Link the GameBlock component to the corresponding value in the Grid
    block.bind(grid.getGridProperty(x, y));

    // Add a mouse click handler to the block to trigger GameBoard blockClicked method
    block.setOnMouseClicked(
        (e) -> {
          if (e.getButton() == MouseButton.PRIMARY) {
            this.blockClicked(block);
          } else {
            this.rightClicked();
          }
        });
    block.setOnMouseEntered(
        (e) -> {
          if (keyboard != null) {
            keyboard.setHovered(false);
          }
          mouse = block;
          block.setHovered(true);
        });
    block.setOnMouseExited((e) -> block.setHovered(false));
    return block;
  }

  /**
   * Set the listener to handle an event when a block is clicked
   *
   * @param listener listener to add
   */
  public void setOnBlockClick(BlockClickedListener listener) {
    this.blockClickedListener = listener;
  }

  /**
   * Triggered when a block is clicked. Call the attached listener.
   *
   * @param block block clicked on
   */
  private void blockClicked(GameBlock block) {
    logger.info("Block clicked: {}", block);
    if (blockClickedListener != null) {
      blockClickedListener.blockClicked(block);
    }
  }

  private void rightClicked() {
    logger.info("GameBoard was right clicked");
    if (rightClickListener != null) {
      rightClickListener.rightClicked();
    }
  }

  public void fadeOut(Set<GameBlockCoordinate> cords) {
    for (GameBlockCoordinate cord : cords) {
      getBlock(cord.getX(), cord.getY()).fadeOut();
    }
  }

  public Grid getGrid() {
    return this.grid;
  }

  public GameBlock[][] getBlocks() {
    return blocks;
  }

  /**
   * Clears the currently hovered block by setting its hovered state to false. Resets the hover
   * coordinates to (-1, -1) to indicate that no block is currently hovered. This method should be
   * called when the mouse pointer is no longer hovering over any block.
   */
  public void clearHoveredBlock() {
    if (hoverX >= 0 && hoverY >= 0) {
      // clearing blocks
      blocks[hoverX][hoverY].setHovered(false);
      hoverX = -1;
      hoverY = -1;
    }
  }

  /**
   * Updates the hover state to the specified coordinates (x, y) by clearing the previously hovered
   * block and setting the new block at the specified coordinates to be hovered.
   *
   * @param x the x-coordinate of the block to hover
   * @param y the y-coordinate of the block to hover
   */
  public void updateHover(int x, int y) {
    if (mouse != null) {
      mouse.setHovered(false);
    }

    keyboard = blocks[x][y];
    clearHoveredBlock();
    hoverX = x;
    hoverY = y;

    blocks[hoverX][hoverY].setHovered(true);
  }

  public int getHoverX() {
    return hoverX;
  }

  public int getHoverY() {
    return hoverY;
  }
}
