package uk.ac.soton.comp1206.scene;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/** The main menu of the game. Provides a gateway to the rest of the game. */
public class MenuScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(MenuScene.class);

  /**
   * Create a new menu scene
   *
   * @param gameWindow the Game Window this will be displayed in
   */
  public MenuScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Menu Scene");
  }

  /** Build the menu layout */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var menuPane = new StackPane();
    menuPane.setMaxWidth(gameWindow.getWidth());
    menuPane.setMaxHeight(gameWindow.getHeight());
    menuPane.getStyleClass().add("menu-background");
    root.getChildren().add(menuPane);

    var mainPane = new BorderPane();
    menuPane.getChildren().add(mainPane);

    ImageView menuImage =
        new ImageView(getClass().getResource("/images/tetrECS.png").toExternalForm());
    menuImage.setFitWidth(gameWindow.getWidth() / 2);
    menuImage.setPreserveRatio(true);
    BorderPane.setAlignment(menuImage, Pos.TOP_CENTER);
    mainPane.setTop(menuImage);
    var translate = new TranslateTransition(Duration.seconds(2), menuImage);
    translate.setCycleCount(-1);
    translate.setByY(50);
    translate.setAutoReverse(true);
    translate.play();

    var playButton = new Text("Play");
    var instructionButton = new Text("Instructions");
    playButton.getStyleClass().add("menuItem");
    instructionButton.getStyleClass().add("menuItem");

    var multiplayer = new Text("Multiplayer Mode");
    multiplayer.getStyleClass().add("menuItem");
    multiplayer.setOnMouseClicked(this::openLobby);

    var exitButton = new Text("Exit Game");
    exitButton.getStyleClass().add("menuItem");
    exitButton.setOnMouseClicked(this::exitGame);

    var buttonBox = new VBox(playButton, multiplayer, instructionButton, exitButton);
    buttonBox.setAlignment(Pos.CENTER);
    buttonBox.setSpacing(10);
    buttonBox.getStyleClass().add("menu");
    mainPane.setCenter(buttonBox);

    playButton.setOnMouseClicked(this::startGame);
    instructionButton.setOnMouseClicked(this::startInstruction);

    Multimedia.playBackgroundMusic(getClass().getResource("/music/game_start.wav").getPath());
  }

  /** Initialise the menu */
  @Override
  public void initialise() {}

  /**
   * Handle when the Start Game button is pressed
   *
   * @param event event
   */
  private void startGame(MouseEvent event) {
    Multimedia.stopBackgroundMusic();
    gameWindow.startChallenge();
  }

  private void startInstruction(MouseEvent event) {
    Multimedia.stopBackgroundMusic();
    gameWindow.loadScene(new InstructionScene(gameWindow));
  }

  private void openLobby(MouseEvent event) {
    Multimedia.stopBackgroundMusic();
    gameWindow.loadScene(new LobbyScene(gameWindow));
  }

  /**
   * Handle when the Exit button is pressed
   *
   * @param mouseEvent mouseEvent
   */
  private void exitGame(MouseEvent mouseEvent) {
    logger.info("Game has been exited");
    gameWindow.getStage().close();
  }
}
