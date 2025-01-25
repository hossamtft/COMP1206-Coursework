package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.network.Communicator;

import java.util.LinkedList;
import java.util.Queue;

/**
 * The MultiplayerGame class represents a multiplayer game instance. It extends the Game class and
 * includes functionality specific to multiplayer gameplay, such as handling pieces received from
 * the server and managing communication with the server.
 */
public class MultiplayerGame extends Game {
  private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);
  private final Communicator communicator;
  private Queue<GamePiece> pieceList = new LinkedList<>();

  /**
   * Create a new multiplayer game with the specified rows and columns. Creates a corresponding grid
   * model.
   *
   * @param cols number of columns
   * @param rows number of rows
   * @param communicator the communicator used to communicate with the server
   */
  public MultiplayerGame(int cols, int rows, Communicator communicator) {
    super(cols, rows);
    this.communicator = communicator;
    communicator.addListener(this::handleMsg);
    for (int i = 0; i < 10; i++) {
      this.communicator.send("PIECE");
    }
  }

  @Override
  /** Starts the game by initialising the game and starting the game loop. */
  public void start() {
    Platform.runLater(this::initialiseGame);
    startGameLoop();
  }

  @Override
  /** Initialises the game by spawning the first piece and setting up the next piece listener. */
  public void initialiseGame() {
    Platform.runLater(
        () -> {
          currentPiece = spawnPiece();
          followingPiece = spawnPiece();
          nextPieceListener();
        });
  }

  @Override
  /** Makes following piece current piece and spawns a new following piece */
  public void nextPiece() {
    logger.info("Spawning next Piece");
    currentPiece = followingPiece;
    followingPiece = spawnPiece();
    nextPieceListener();
    communicator.send("SCORES");
  }

  /**
   * Handles messages received from the server. If a message starts with "PIECE", a new piece is
   * created.
   *
   * @param message the message received from the server
   */
  public void handleMsg(String message) {
    if (message.startsWith("PIECE")) {
      newPiece(message);
    }
  }

  /**
   * Adds a new piece to the piece list based on the provided piece message.
   *
   * @param piece the message representing the new piece
   */
  private void newPiece(String piece) {
    piece = piece.replace("PIECE ", "");
    pieceList.add(GamePiece.createPiece(Integer.parseInt(piece)));
  }

  /**
   * Sends a request for a new piece to the server and returns/spawns the first piece in the list.
   *
   * @return the newly spawned piece
   */
  @Override
  public GamePiece spawnPiece() {
    communicator.send("PIECE");
    return pieceList.poll();
  }

  @Override
  /** sends a message to the server when a block is clicked */
  public boolean blockClicked(GameBlock gameBlock) {
    int x = gameBlock.getX();
    int y = gameBlock.getY();
    StringBuilder board = new StringBuilder();
    for (int xx = 0; xx < cols; xx++) {
      for (int yy = 0; yy < rows; yy++) {
        board.append(grid.get(xx, yy) + " ");
      }
    }
    communicator.send("BOARD " + board);
    communicator.send(board.toString());
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

  @Override
  /** Sends a message to the server when a line is cleared */
  public void score(int lines, int blocks) {
    super.score(lines, blocks);
    communicator.send("SCORE " + score.getValue());
  }

  /** Calls the next piece listener to notify the listeners that the next piece has been spawned. */
  public void nextPieceListener() {
    if (nextPieceListener != null) {
      nextPieceListener.nextPiece(currentPiece);
    }
  }
}
