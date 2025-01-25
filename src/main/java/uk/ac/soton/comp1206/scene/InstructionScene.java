package uk.ac.soton.comp1206.scene;

import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.game.PieceBoard;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.Objects;

public class InstructionScene extends BaseScene {
  private static final Logger logger = LogManager.getLogger(MenuScene.class);

  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public InstructionScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Instructions Scene");
  }

  /** Initialise this scene. Called after creation */
  public void initialise() {
    logger.info("Initialising Instructions Scene");
    root.getScene()
        .setOnKeyPressed(
            event -> {
              if (event.getCode() == KeyCode.ESCAPE) {
                gameWindow.loadScene(new MenuScene(gameWindow));
              }
            });
  }

  /** Build the layout of the scene */
  public void build() {
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());
    root.getStyleClass().add("menu-background");

    VBox instructions = new VBox();
    generateImage(instructions);
    instructions.setAlignment(Pos.CENTER_RIGHT);
    var pane = new GridPane();
    pane.getStyleClass().add("instructions-grid");
    pane.setHgap(10);
    pane.setVgap(10);
    pane.setAlignment(Pos.CENTER_LEFT);
    root.getChildren().add(pane);
    for (int i = 0; i < 15; i++) {
      Grid newGrid = new Grid(3, 3);
      newGrid.playPiece(GamePiece.createPiece(i), 1, 1);
      PieceBoard pieceBoard = new PieceBoard(newGrid, 100, 100);
      pane.add(pieceBoard, i % 3, i / 3);
    }

    root.getChildren().add(instructions);
  }

  /**
   * Displays instructions using an image file located at the specified file path.
   *
   * @param vBox UI vbox to add to
   */
  private void generateImage(VBox vBox) {
    ImageView instructionImage =
        new ImageView(
            Objects.requireNonNull(getClass().getResource("/images/Instructions.png"))
                .toExternalForm());
    instructionImage.setFitWidth((double) gameWindow.getWidth() / 1.75);
    instructionImage.setPreserveRatio(true);
    vBox.getChildren().add(instructionImage);
  }
}
