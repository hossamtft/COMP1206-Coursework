package uk.ac.soton.comp1206.scene;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Set;
import java.util.Stack;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.game.*;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.ScoreList;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(MenuScene.class);
  protected Game game;
  public Grid currentPieceGrid;
  public PieceBoard currentPieceBoard;
  public Grid nextPieceGrid;
  public PieceBoard nextPieceBoard;
  public VBox sideBar;
  public GameBoard board;

  /** Timer UI */
  public StackPane timerStack;

  public Rectangle timer;
  public StackPane challengePane;
  public BorderPane mainPane;
  public GridPane topBar;

  /**
   * Create a new Single Player challenge scene
   *
   * @param gameWindow the Game Window
   */
  public ChallengeScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Challenge Scene");
  }

  /** Build the Challenge window */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    setupGame();

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    challengePane = new StackPane();
    challengePane.setMaxWidth(gameWindow.getWidth());
    challengePane.setMaxHeight(gameWindow.getHeight());
    challengePane.getStyleClass().add("challenge-background");
    root.getChildren().add(challengePane);

    mainPane = new BorderPane();
    challengePane.getChildren().add(mainPane);

    board = new GameBoard(game.getGrid(), gameWindow.getWidth() / 2, gameWindow.getWidth() / 2);
    board.setGame(game);
    mainPane.setCenter(board);

    board.setOnRightClick(this::rotateBlock);
    game.setLineClearedListener(this::fadeClearedBlocks);
    game.setNextPieceListener(this::nextPiece);
    game.setOnGameLoop(this::gameLoop);
    game.setEndGameListener(this::gameOver);

    setUpTimer();

    VBox scoreBox = new VBox();
    Text scoreHeader = new Text("Score");
    scoreHeader.getStyleClass().add("heading");
    Text scoreField = new Text();
    scoreField.textProperty().bind(game.getScoreProperty().asString());
    scoreField.getStyleClass().add("stats");
    scoreBox.setAlignment(Pos.CENTER);
    scoreBox.setPrefWidth(150);
    scoreBox.getChildren().add(scoreHeader);
    scoreBox.getChildren().add(scoreField);

    VBox levelBox = new VBox();
    Text levelHeader = new Text("Level");
    levelHeader.getStyleClass().add("heading");
    Text levelField = new Text();
    levelField.textProperty().bind(game.getLevelProperty().asString());
    levelField.getStyleClass().add("stats");
    levelBox.setAlignment(Pos.CENTER);
    levelBox.setPrefWidth(150);
    levelBox.getChildren().add(levelHeader);
    levelBox.getChildren().add(levelField);

    VBox liveBox = new VBox();
    Text liveHeader = new Text("Lives");
    liveHeader.getStyleClass().add("heading");
    Text liveField = new Text();
    liveField.textProperty().bind(game.getLivesProperty().asString());
    liveField.getStyleClass().add("stats");
    liveBox.setAlignment(Pos.CENTER);
    liveBox.setPrefWidth(150);
    liveBox.getChildren().add(liveHeader);
    liveBox.getChildren().add(liveField);

    VBox multBox = new VBox();
    Text multHeader = new Text("Multiplier");
    multHeader.getStyleClass().add("heading");
    Text multField = new Text();
    multField.textProperty().bind(game.getMultiplierProperty().asString());
    multField.getStyleClass().add("stats");
    multBox.setAlignment(Pos.CENTER);
    multBox.setPrefWidth(150);
    multBox.getChildren().add(multHeader);
    multBox.getChildren().add(multField);

    VBox highScoreBox = new VBox();
    Text highScoreHeader = new Text("High Score");
    highScoreHeader.getStyleClass().add("heading");
    Text highScoreField = new Text();
    highScoreField.textProperty().bind(getHighScore());
    highScoreField.getStyleClass().add("stats");
    highScoreBox.setAlignment(Pos.CENTER);
    highScoreBox.setPrefWidth(150);
    highScoreBox.getChildren().add(highScoreHeader);
    highScoreBox.getChildren().add(highScoreField);

    topBar = new GridPane();
    topBar.add(scoreBox, 0, 0);
    topBar.add(levelBox, 1, 0);
    topBar.add(multBox, 2, 0);
    topBar.add(liveBox, 3, 0);
    topBar.add(highScoreBox, 4, 0);

    mainPane.setTop(topBar);

    currentPieceGrid = new Grid(3, 3);
    currentPieceBoard = new PieceBoard(currentPieceGrid, 200, 200);
    currentPieceBoard.setOnBlockClick(this::rotateBlock);

    nextPieceGrid = new Grid(3, 3);
    nextPieceBoard = new PieceBoard(nextPieceGrid, 150, 150);
    nextPieceBoard.setOnBlockClick(this::swapCurrentBlocks);

    sideBar = new VBox(10, currentPieceBoard, nextPieceBoard);
    sideBar.setAlignment(Pos.CENTER);
    mainPane.setRight(sideBar);

    Multimedia.playBackgroundMusic(getClass().getResource("/music/game.wav").getPath());

    board.setOnBlockClick(this::blockClicked);
  }

  /**
   * Swap the current piece with the next piece
   *
   * @param gameBlock the Game Block that was clicked
   */
  private void swapCurrentBlocks(GameBlock gameBlock) {
    game.swapCurrentPiece();
    currentPieceBoard.setPiece(game.getCurrentPiece());
    nextPieceBoard.setPiece(game.getFollowingPiece());
  }

  /**
   * Fade out the cleared blocks
   *
   * @param gameBlockCoordinates the coordinates of the blocks that were cleared
   */
  private void fadeClearedBlocks(Set<GameBlockCoordinate> gameBlockCoordinates) {
    board.fadeOut(gameBlockCoordinates);
  }

  /**
   * Rotate the current piece
   *
   * @param gameBlock the Game Block that was clicked
   */
  private void rotateBlock(GameBlock gameBlock) {
    game.rotateCurrentPiece();
    currentPieceBoard.setPiece(game.getCurrentPiece());
  }

  /**
   * Handle when a block is clicked
   *
   * @param gameBlock the Game Block that was clocked
   */
  private void blockClicked(GameBlock gameBlock) {
    if (game.blockClicked(gameBlock)) {
      logger.info("Placed {}", gameBlock);
      Multimedia.playAudio(getClass().getResource("/sounds/place.wav").getPath());
      game.restartGameLoop();
    } else {
      logger.info("Unable to place {}", gameBlock);
      Multimedia.playAudio(getClass().getResource("/sounds/fail.wav").getPath());
    }
  }

  /** Set up the game object and model */
  public void setupGame() {
    logger.info("Starting a new challenge");
    // Start new game
    game = new Game(5, 5);
  }

  /** Initialise the scene and start the game */
  public void initialise() {
    logger.info("Initialising Challenge");
    game.start();
    root.getScene().addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPress);
    board.requestFocus();
  }

  /** End the game */
  private void endGame() {
    logger.info("Ending game");
    game.endGame();
    Multimedia.stopBackgroundMusic();
  }

  /**
   * Called when the game provides a new piece.
   *
   * @param nextPiece the new piece provided by the game
   */
  public void nextPiece(GamePiece nextPiece) {
    currentPieceBoard.setPiece(nextPiece);
    currentPieceBoard.middleDot();
    nextPieceBoard.setPiece(game.getFollowingPiece());
  }

  /** Rotate the current piece */
  public void rotateBlock() {
    game.rotateCurrentPiece();
    Multimedia.playAudio(getClass().getResource("/sounds/rotate.wav").getPath());
    currentPieceBoard.setPiece(game.getCurrentPiece());
  }

  /**
   * Rotate the current piece
   *
   * @param i the number of times to rotate the piece
   */
  public void rotates(int i) {
    for (int wow = 0; wow < i; wow++) {
      game.rotateCurrentPiece();
      Multimedia.playAudio(getClass().getResource("/sounds/rotate.wav").getPath());
    }
    currentPieceBoard.setPiece(game.getCurrentPiece());
  }

  /** Called when the game is over Spawns the score scene */
  private void gameOver() {
    gameWindow.startScoreScene(this.game);
    Multimedia.stopBackgroundMusic();
  }

  /**
   * Handle key presses for the game
   *
   * @param keyEvent the key event that was pressed
   */
  public void handleKeyPress(KeyEvent keyEvent) {
    var keyCode = keyEvent.getCode();
    logger.info("Keycode: {}", keyCode);
    int xKey = 0;
    int yKey = 0;
    if (!(scene.getFocusOwner() instanceof javafx.scene.control.TextField)) {
      if (board.getHoverX() == -1 && board.getHoverY() == -1) {
        board.updateHover(0, 0);
      }

      xKey = board.getHoverX();
      yKey = board.getHoverY();

      if (keyCode.equals(KeyCode.ENTER) || keyCode.equals(KeyCode.X)) {
        blockClicked(board.getBlock(xKey, yKey));
      }
      switch (keyCode) {
        case LEFT, A -> board.updateHover(Math.max((xKey - 1), 0), yKey);
        case UP, W -> board.updateHover(xKey, Math.max(yKey - 1, 0));
        case RIGHT, D -> board.updateHover(Math.min((xKey + 1), board.getCols() - 1), yKey);
        case DOWN, S -> board.updateHover(xKey, Math.min(yKey + 1, board.getRows() - 1));
      }
      // Swap pieces
      if (keyCode.equals(KeyCode.SPACE) || keyCode.equals(KeyCode.R)) {
        logger.info("Swap");
        swapCurrentBlocks();
        logger.info("Swapblock done");
      }

      if (keyCode.equals(KeyCode.Q)
          || keyCode.equals(KeyCode.Z)
          || keyCode.equals(KeyCode.OPEN_BRACKET)) {
        rotates(3);
      }

      if (keyCode.equals(KeyCode.E)
          || keyCode.equals(KeyCode.C)
          || keyCode.equals(KeyCode.CLOSE_BRACKET)) {
        rotateBlock();
      }
      if (keyCode.equals(KeyCode.V)) {
        game.gameLoop();
      }
      if (keyCode.equals(KeyCode.ESCAPE)) {
        endGame();
        gameWindow.startMenu();
      }
      logger.info("Key press done: ");
    }

    board.requestFocus();
  }

  /** Swap the current piece with the next piece */
  private void swapCurrentBlocks() {
    game.swapCurrentPiece();
    currentPieceBoard.setPiece(game.getCurrentPiece());
    nextPieceBoard.setPiece(game.getFollowingPiece());
  }

  /**
   * Manage the game loop bar animation.
   *
   * @param nextLoop the length of the game loop
   */
  protected void gameLoop(int nextLoop) {
    var greenKeyFrame =
        new KeyFrame(Duration.ZERO, new KeyValue(timer.fillProperty(), Color.GREEN));

    var startingLengthKeyFrame =
        new KeyFrame(Duration.ZERO, new KeyValue(timer.widthProperty(), timerStack.getWidth()));

    var yellowKeyFrame =
        new KeyFrame(
            new Duration((double) nextLoop * 0.5),
            new KeyValue(timer.fillProperty(), Color.YELLOW));

    var redKeyFrame =
        new KeyFrame(
            new Duration((double) nextLoop * 0.75), new KeyValue(timer.fillProperty(), Color.RED));

    var endingLengthKeyFrame =
        new KeyFrame(new Duration(nextLoop), new KeyValue(timer.widthProperty(), 0));

    Timeline timeline =
        new Timeline(
            greenKeyFrame,
            startingLengthKeyFrame,
            yellowKeyFrame,
            redKeyFrame,
            endingLengthKeyFrame);

    timeline.play();
  }

  /**
   * Get the high score from the scores file
   *
   * @return the high score
   */
  public SimpleStringProperty getHighScore() {
    ArrayList<Pair<String, Integer>> newHighScore = ScoresScene.loadScores();
    SimpleStringProperty highScore =
        new SimpleStringProperty(newHighScore.get(0).getValue().toString());
    return highScore;
  }

  /**
   * Set up the timer UI
   *
   * @return the StackPane containing the timer
   */
  public StackPane setUpTimer() {
    timerStack = new StackPane();
    mainPane.setBottom(timerStack);
    timer = new Rectangle();
    timer.setHeight(25);
    BorderPane.setMargin(timerStack, new Insets(5, 5, 5, 5));
    timerStack.getChildren().add(timer);
    StackPane.setAlignment(timer, Pos.CENTER);
    return timerStack;
  }
}
