package uk.ac.soton.comp1206.game;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.GameOverListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;

import static javafx.application.Platform.runLater;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to
 * manipulate the game state and to handle actions made by the player should take place inside this
 * class.
 */
public class Game {
  /** Logger to log whats happening */
  private static final Logger logger = LogManager.getLogger(Game.class);

  /** NextPieceListener to be notified when the next piece is generated */
  public NextPieceListener nextPieceListener = null;

  /** LineClearedListener to be notified when a line is cleared */
  public LineClearedListener lineClearedListener;

  /** Random number generator */
  public Random random = new Random();

  /** Number of rows */
  protected final int rows;

  /** Number of columns */
  protected final int cols;

  /** The grid model linked to the game */
  protected final Grid grid;

  /** The current piece to be played in the game */
  public GamePiece currentPiece;

  /** The Following Piece to be played in the game */
  public GamePiece followingPiece;

  /** The current score of the game. */
  public IntegerProperty score = new SimpleIntegerProperty(0);

  /** The current level of the game. */
  public IntegerProperty level = new SimpleIntegerProperty(0);

  /** The number of lives remaining in the game. */
  public IntegerProperty lives = new SimpleIntegerProperty(3);

  /** The current score multiplier of the game. */
  public IntegerProperty multiplier = new SimpleIntegerProperty(1);

  /** The executor for the game loop. */
  public final ScheduledExecutorService executor;

  /**
   * The timer for the game loop. This timer is used to schedule the game loop to run at a fixed
   * interval.
   */
  public ScheduledFuture<?> nextLoop;

  /** the NextPieceListener to be notified when the next piece is generated. */
  public GameLoopListener loopListener;

  /** The GameOverListener to be notified when the game is over. */
  public GameOverListener endGameListener;

  /**
   * Create a new game with the specified rows and columns. Creates a corresponding grid model.
   *
   * @param cols number of columns
   * @param rows number of rows
   */
  public Game(int cols, int rows) {
    this.cols = cols;
    this.rows = rows;

    // Create a new grid model to represent the game state
    this.grid = new Grid(cols, rows);

    executor = Executors.newSingleThreadScheduledExecutor();
  }

  /** Start the game */
  public void start() {
    logger.info("Starting game");
    initialiseGame();
    startGameLoop();
  }
  /** Initialise a new game and set up anything that needs to be done at the start */
  public void initialiseGame() {
    logger.info("Initialising game");
    followingPiece = spawnPiece();
    nextPiece();
  }

  /**
   * Handle what should happen when a particular block is clicked
   *
   * @param gameBlock the block that was clicked
   */
  public boolean blockClicked(GameBlock gameBlock) {
    // Get the position of this block
    int x = gameBlock.getX();
    int y = gameBlock.getY();
    if (grid.canPlayPiece(currentPiece, x, y)) {
      grid.playPiece(currentPiece, x, y);
      afterPiece();
      nextPiece();
      Multimedia.playAudio(getClass().getResource("/sounds/place.wav").getPath());
      return true;
    } else {
      Multimedia.playAudio(getClass().getResource("/sounds/fail.wav").getPath());
      return false;
    }
  }

  /**
   * Get the grid model inside this game representing the game state of the board
   *
   * @return game grid model
   */
  public Grid getGrid() {
    return grid;
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

  public void nextPiece() {
    currentPiece = followingPiece;
    followingPiece = spawnPiece();
    logger.info("The current peice is : {}", currentPiece);
    logger.info("The next peice is : {}", followingPiece);
    if (nextPieceListener != null) {
      nextPieceListener.nextPiece(currentPiece);
    }
  }

  /**
   * Creates a game piece
   *
   * @return returns piece created
   */
  public GamePiece spawnPiece() {
    var maxPieces = GamePiece.PIECES;
    var randomPiece = random.nextInt(maxPieces);
    logger.info("Picking random pieces: {}", randomPiece);
    GamePiece piece = GamePiece.createPiece(randomPiece);
    return piece;
  }

  /**
   * Handle actions to be performed after playing a piece. This method clears any full vertical or
   * horizontal lines.
   */
  public void afterPiece() {
    Set<Integer> rowsToClear = new HashSet<>();
    Set<Integer> columnsToClear = new HashSet<>();
    Set<GameBlockCoordinate> blocksToClear = new HashSet<>();

    // Search for horizontal lines
    for (int y = 0; y < rows; y++) {
      boolean fullRow = true;
      for (int x = 0; x < cols; x++) {
        if (grid.get(x, y) == 0) {
          fullRow = false;
          break;
        }
      }
      if (fullRow) {
        logger.info("Found full row at index: {}", y);
        rowsToClear.add(y);
        // Add blocks to clear set
        for (int x = 0; x < cols; x++) {
          blocksToClear.add(new GameBlockCoordinate(x, y));
        }
      }
    }

    // Search for vertical lines
    for (int x = 0; x < cols; x++) {
      boolean fullColumn = true;
      for (int y = 0; y < rows; y++) {
        if (grid.get(x, y) == 0) {
          fullColumn = false;
          break;
        }
      }
      if (fullColumn) {
        logger.info("Found full column at index: {}", x);
        columnsToClear.add(x);
        // Add blocks to clear set
        for (int y = 0; y < rows; y++) {
          blocksToClear.add(new GameBlockCoordinate(x, y));
        }
      }
    }
    fireLineCleared(blocksToClear);

    // Clear full rows
    for (int row : rowsToClear) {
      clearRow(row);
    }

    // Clear full columns
    for (int col : columnsToClear) {
      clearColumn(col);
    }
    // Calculate the number of lines cleared
    int numberOfLinesCleared = Math.max(rowsToClear.size(), columnsToClear.size());

    // Calculate the number of blocks cleared
    int numberOfBlocksCleared = blocksToClear.size();

    // Call the score method to calculate and update the score
    score(numberOfLinesCleared, numberOfBlocksCleared);
  }

  /**
   * Calculate the score based on the number of lines cleared and the number of blocks cleared.
   *
   * @param numberOfLines the number of lines cleared
   * @param numberOfBlocks the number of blocks cleared
   */
  public void score(int numberOfLines, int numberOfBlocks) {
    // Calculate score only if at least one line is cleared
    if (numberOfLines > 0) {
      logger.info("Line Cleared");
      // Calculate score based on the formula
      int scoreToAdd = numberOfLines * numberOfBlocks * 10 * multiplier.get();

      // Add the calculated score to the current score
      setScore(score.get() + scoreToAdd);
      logger.info("Score is now: {}", getScore());
      // Update the multiplier
      setMultiplier(multiplier.get() + 1);
      logger.info("Multiplier is now {}", getMultiplier());

    } else {
      logger.info("No line Cleared");
      // Reset the multiplier if no lines are cleared
      setMultiplier(1);
    }
  }

  /**
   * Clear the specified row by removing all blocks and shifting blocks above down.
   *
   * @param row the row to clear
   */
  private void clearRow(int row) {
    for (int col = 0; col < cols; col++) {
      grid.set(col, row, 0); // Clear block
    }
  }

  /**
   * Clear the specified column by removing all blocks and shifting blocks to the left.
   *
   * @param col the column to clear
   */
  private void clearColumn(int col) {
    for (int row = 0; row < rows; row++) {
      grid.set(col, row, 0); // Clear block
    }
  }

  /**
   * Retrieves value of the current games score.
   *
   * @return the score of the current game.
   */
  public int getScore() {
    return this.score.get();
  }

  /**
   * Retrieves the score property.
   *
   * @return The current score property.
   */
  public IntegerProperty getScoreProperty() {
    return score;
  }

  /**
   * Sets the score of the game.
   *
   * @param score The new score value.
   */
  public void setScore(int score) {
    this.score.set(score);
    updateLevel();
  }

  public void updateLevel() {
    setLevel((int) Math.floor(getScore() / 1000));
  }

  /**
   * Retrieves the current level of the game.
   *
   * @return The current level.
   */
  public int getLevel() {
    return this.lives.get();
  }

  /**
   * Retrieves the current level property.
   *
   * @return The current level property.
   */
  public IntegerProperty getLevelProperty() {
    return level;
  }

  /**
   * Sets the level of the game.
   *
   * @param level The new level value.
   */
  public void setLevel(int level) {
    this.level.set(level);
  }

  /**
   * Retrieves the number of lives remaining in the game.
   *
   * @return The number of lives.
   */
  public int getLives() {
    return this.lives.get();
  }

  /**
   * Retrieves the lives property.
   *
   * @return lives property.
   */
  public IntegerProperty getLivesProperty() {
    return lives;
  }

  /**
   * Sets the number of lives remaining in the game.
   *
   * @param lives The new number of lives.
   */
  public void setLives(int lives) {
    this.lives.set(lives);
  }

  /**
   * Retrieves the current score multiplier of the game.
   *
   * @return The current multiplier.
   */
  public int getMultiplier() {
    return this.multiplier.get();
  }

  /**
   * Retrieves the multiplier property
   *
   * @return The multiplier property.
   */
  public IntegerProperty getMultiplierProperty() {
    return multiplier;
  }

  /**
   * Sets the score multiplier of the game.
   *
   * @param multiplier The new multiplier value.
   */
  public void setMultiplier(int multiplier) {
    this.multiplier.set(multiplier);
  }

  /**
   * Set the NextPieceListener to be notified when the next piece is generated.
   *
   * @param listener the listener to set
   */
  public void setNextPieceListener(NextPieceListener listener) {
    this.nextPieceListener = listener;
  }

  /** Rotate the current piece clockwise. */
  public void rotateCurrentPiece() {
    if (currentPiece != null) {
      currentPiece.rotate();
    }
  }

  /** Move the current piece left. */
  public GamePiece getCurrentPiece() {
    return this.currentPiece;
  }

  /**
   * Get the following piece.
   *
   * @return the following piece
   */
  public GamePiece getFollowingPiece() {
    return this.followingPiece;
  }

  /** Swap the current piece with the following piece. */
  public void swapCurrentPiece() {
    if (currentPiece != null && followingPiece != null) {
      GamePiece tempPiece = currentPiece;
      currentPiece = followingPiece;
      followingPiece = tempPiece;
    }
  }

  /**
   * Set the LineClearedListener to be notified when lines are cleared.
   *
   * @param listener the LineClearedListener to set
   */
  public void setLineClearedListener(LineClearedListener listener) {
    this.lineClearedListener = listener;
  }

  /**
   * Notify the LineClearedListener that a line has been cleared.
   *
   * @param clearedBlocks the set of blocks that have been cleared
   */
  private void fireLineCleared(Set<GameBlockCoordinate> clearedBlocks) {
    if (lineClearedListener != null) {
      lineClearedListener.LineCleared(clearedBlocks);
    }
  }

  /** Decrease the amount of lives and trigger the game over method when it is bellow 0. */
  private void loseLive() {
    logger.info("lives now: {}", lives.get());
    if (lives.get() > 0) {
      lives.set(lives.get() - 1);
    } else {

    }
  }

  /**
   * Set the GameOverListener to be notified when the game is over.
   *
   * @param listener the GameOverListener to set
   */
  public void setEndGameListener(GameOverListener listener) {
    this.endGameListener = listener;
  }

  public void resetMultiplier() {
    if (getMultiplier() > 1) {
      logger.info("Multiplier reset to 1");
      setMultiplier(1);
    }
  }

  /**
   * Get the delay for the game loop timer.
   *
   * @return the delay in milliseconds
   */
  public int getTimerDelay() {
    int delay = 12000 - 500 * getLevel();
    return Math.max(2500, delay);
  }

  /**
   * Set the GameLoopListener to receive game loop events.
   *
   * @param listener the listener to set
   */
  public void setOnGameLoop(GameLoopListener listener) {
    this.loopListener = listener;
  }

  /** Starting the game loop. */
  public void startGameLoop() {
    nextLoop = executor.schedule(this::gameLoop, getTimerDelay(), TimeUnit.MILLISECONDS);
    if (loopListener != null) {
      loopListener.gameLoop(getTimerDelay());
    }
  }

  /** Restarting the game loop. */
  public void restartGameLoop() {
    nextLoop.cancel(false);
    startGameLoop();
  }

  /** Finishing a game loop cycle. */
  public void gameLoop() {
    // Reset the multiplier
    if (getMultiplier() > 1) {
      logger.info("Multiplier reset to 1");
      setMultiplier(1);
    }

    // Decrease the live count
    decreaseLives();

    // Generate the next piece
    nextPiece();

    // Generate the timer for the next game loop
    int nextTimer = getTimerDelay();

    if (loopListener != null) {
      loopListener.gameLoop(nextTimer);
    }

    // Cancel the game loop timers
    nextLoop.cancel(false);
    // Set the game loop timers
    nextLoop = executor.schedule(this::gameLoop, nextTimer, TimeUnit.MILLISECONDS);
  }

  /** Decrease the amount of lives and trigger the game over method when it is bellow 0. */
  private void decreaseLives() {
    if (lives.get() > 0) {
      lives.set(lives.get() - 1);
    } else {
      gameOver();
    }
  }

  /** Ending the game and stopping the game loop. */
  public void endGame() {
    nextLoop.cancel(false);
    executor.shutdown();
  }

  /** Stopping the game when it is over. */
  private void gameOver() {
    logger.info("Game over!");
    executor.shutdown();
    if (endGameListener != null) {
      runLater(() -> endGameListener.gameOverNow());
    }
  }
}
