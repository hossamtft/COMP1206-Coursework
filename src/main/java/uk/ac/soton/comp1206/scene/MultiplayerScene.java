package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.Multimedia;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.Leaderboard;

import java.util.ArrayList;
import java.util.List;

/**
 * The MultiplayerScene class provides a scene for multiplayer gameplay. It handles the display of
 * the game, communication between players, and updating the leaderboard.
 */
public class MultiplayerScene extends ChallengeScene {
  Logger logger = LogManager.getLogger(MultiplayerScene.class);
  private Communicator communicator;
  protected VBox vBox;
  private StringProperty name = new SimpleStringProperty("");
  protected ArrayList<Pair<String, Integer>> remotePlayerScores = new ArrayList<>();
  protected ObservableList<Pair<String, Integer>> remotePlayerScoresList =
      FXCollections.observableArrayList(remotePlayerScores);
  Leaderboard leaderBoard;
  private VBox chat;
  private TextField chatInput;
  private TextArea chatHistory;
  private int msgListenerCounter = 0;

  /**
   * Create a new MultiplayerScene.
   *
   * @param gameWindow the Game Window
   */
  public MultiplayerScene(GameWindow gameWindow) {
    super(gameWindow);
    this.communicator = gameWindow.getCommunicator();
  }

  /** Build the scene, including the game components and chat interface. */
  @Override
  public void build() {
    super.build();

    vBox = new VBox();
    vBox.setMaxHeight(80);
    mainPane.setBottom(vBox);

    remotePlayerScoresList = FXCollections.observableArrayList(remotePlayerScores);
    SimpleListProperty<Pair<String, Integer>> deadPlayerScoresListWrapper =
        new SimpleListProperty<>(remotePlayerScoresList);
    leaderBoard = new Leaderboard();
    Text leaderTitle = new Text("LeaderBoard");
    leaderTitle.setTextAlignment(TextAlignment.CENTER);
    leaderTitle.getStyleClass().add("heading");
    leaderBoard.getStyleClass().add("leaderboard");
    leaderBoard.getScoresProperty().bind(deadPlayerScoresListWrapper);
    leaderBoard.getName().bind(name);
    leaderBoard.setAlignment(Pos.CENTER);
    VBox leaderboard = new VBox();
    leaderboard.setAlignment(Pos.TOP_CENTER);
    leaderboard.setPrefWidth(150);
    leaderboard.getChildren().add(leaderTitle);
    leaderboard.getChildren().add(leaderBoard);
    super.mainPane.setLeft(leaderboard);

    chat = new VBox();
    chatHistory = new TextArea();
    chatHistory.setPrefSize(70, 70);
    chatHistory.setEditable(false);
    chatInput = new TextField();
    chatInput.setFocusTraversable(true);
    chatInput.setOnMouseClicked(
        e -> {
          chatInput.requestFocus();
        });
    chatInput.setPromptText("Send chat");
    chatInput.setOnAction(e -> sendMessage(chatInput.getText()));
    Text msgLabel = new Text("Messages:");
    msgLabel.getStyleClass().add("heading");

    StackPane timerPane = setUpTimer();

    chat.getChildren().addAll(timerPane, msgLabel, chatHistory, chatInput);
    mainPane.setBottom(chat);
  }

  /** Initialize the scene. */
  @Override
  public void initialise() {
    super.initialise();
    scene.addEventHandler(KeyEvent.KEY_PRESSED, this::keyBoard);
    communicator.addListener(
        msg -> {
          if (msg.startsWith("SCORES")) {
            filterScores(msg.substring(7));
          }
        });
    communicator.send("SCORES");
    communicator.send("NICK");
    game.setOnGameLoop(this::gameLoop);
    root.getScene().addEventHandler(KeyEvent.KEY_PRESSED, this::keyBoard);
  }

  /** Set up the game for multiplayer mode. */
  public void setupGame() {
    this.game = new MultiplayerGame(5, 5, communicator);
  }

  /**
   * Sends a message to the server to be broadcasted to all players.
   *
   * @param message The message to be sent
   */
  public void sendMessage(String message) {
    if (msgListenerCounter < 1) {
      msgListenerCounter++;
      communicator.addListener(
          sms -> {
            if (sms.startsWith("MSG")) {
              Platform.runLater(
                  () -> {
                    chatHistory.appendText(sms.substring(4) + "\n");
                    Multimedia.playAudio(getClass().getResource("/sounds/message.wav").getPath());
                  });
            }
          });
    }
    if (message != null && !message.isEmpty()) {
      communicator.send("MSG " + message);
    }
    chatInput.clear();
  }

  /**
   * Handles keyboard events.
   *
   * @param event The KeyEvent
   */
  public void keyBoard(KeyEvent event) {
    super.handleKeyPress(event);
    switch (event.getCode()) {
      case ESCAPE:
        communicator.send("DIE");
        gameWindow.startMenu();
        break;
    }
  }

  /**
   * Filters the scores received from the server and updates the leaderboard.
   *
   * @param msg The message containing the scores
   */
  public void filterScores(String msg) {
    List<Pair<String, String>> user = new ArrayList<>();
    String[] array = msg.split("\n");
    for (String string : array) {
      String[] split = string.split(":");
      if(split[2] == "DEAD") {
        user.add(new Pair<>(split[0], "DEAD"));
      }
      else if (leaderBoard.parseInt(split[1]) != -1) {
        user.add(new Pair<>(split[0], split[1]));
      }
    }
    Platform.runLater(
        () -> {
          leaderBoard.updateLeaderboard(user);
        });
  }
}
