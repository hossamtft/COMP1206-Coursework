package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class LobbyScene extends BaseScene {
  private static Communicator communicator;
  private Timer timer;
  protected TextFlow channelNames;
  private VBox rightBox;
  private VBox leftBox;
  private HBox bottomBox;
  private ScrollPane scroller;
  private boolean channelCreated = false;
  private boolean isHost = false;
  public TextFlow chatBox = new TextFlow();
  protected TextFlow userList;
  public Label error;
  private Text startText;
  private Text leaveText;
  private Timer buttonTimer;

  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public LobbyScene(GameWindow gameWindow) {
    super(gameWindow);
    communicator = gameWindow.getCommunicator();
  }

  /**
   * Handle the tasks that are sent from the server
   *
   * @param message the message sent from the server
   */
  public void handleTasks(String message) {
    String[] parts = message.split(" ", 2);
    String part = parts[0];
    switch (part) {
      case "CHANNELS":
        this.handleChannel(message);
        break;
      case "ERROR":
        this.handleError();
        break;
      case "MSG":
        this.handleMsg(message);
        break;
      case "USERS":
        handleUserList(message);
        break;
      case "START":
        startGame();
        break;
      case "HOST":
        isHost = true;
        break;
    }
  }

  @Override
  /** Initialise this scene. Called after creation */
  public void initialise() {
    communicator.addListener(
        message ->
            Platform.runLater(
                () -> {
                  handleTasks(message);
                }));
    this.scene.setOnKeyPressed(
        (e) -> {
          if (e.getCode().equals(KeyCode.ESCAPE)) {
            if (channelCreated) {
              communicator.send("PART");
              timer.cancel();
              buttonTimer.cancel();
              this.gameWindow.startMenu();
            }
          }
        });
    lobbyTimer();
  }

  @Override
  /** Build the layout of the scene */
  public void build() {
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var lobbyPane = new BorderPane();
    lobbyPane.setMaxWidth(gameWindow.getWidth());
    lobbyPane.setMaxHeight(gameWindow.getHeight());
    lobbyPane.getStyleClass().add("menu-background");
    root.getChildren().add(lobbyPane);

    var lobbyStack = new StackPane();
    lobbyStack.setAlignment(Pos.CENTER);
    lobbyPane.setCenter(lobbyStack);

    var titleBox = new HBox();
    titleBox.setAlignment(Pos.CENTER);
    lobbyPane.setTop(titleBox);

    var multiplayerTitle = new Text("Multiplayer Lobby");
    multiplayerTitle.getStyleClass().add("title");
    titleBox.getChildren().add(multiplayerTitle);

    var hBox = new HBox();
    hBox.setAlignment(Pos.CENTER_LEFT);
    hBox.setSpacing(50);
    lobbyStack.getChildren().add(hBox);

    rightBox = new VBox();
    rightBox.setMaxHeight(600);
    rightBox.setMaxWidth(200);
    rightBox.setAlignment(Pos.CENTER_RIGHT);

    leftBox = new VBox();
    leftBox.setMaxWidth(150);
    leftBox.setPrefHeight(250);
    leftBox.setAlignment(Pos.CENTER_LEFT);
    hBox.getChildren().add(leftBox);
    hBox.getChildren().add(rightBox);

    var channelTitle = new Text("Active Channels");
    channelTitle.getStyleClass().add("title");
    rightBox.getChildren().add(channelTitle);

    channelNames = new TextFlow();
    channelNames.getStyleClass().add("textFlow");
    channelNames.setMaxWidth(200);
    channelNames.setPrefHeight(200);
    channelNames.setTextAlignment(TextAlignment.LEFT);
    leftBox.getChildren().add(channelNames);

    userList = new TextFlow();
    userList.getStyleClass().add("textFlow");

    ScrollPane scroller = new ScrollPane();
    scroller.getStyleClass().add("scroll-pane");
    scroller.setMinWidth(300);
    scroller.setFitToWidth(true);
    scroller.setPrefHeight(gameWindow.getHeight() / 2);

    scroller.setContent(channelNames);
    rightBox.getChildren().add(scroller);
    var createGameText = new Text("Create Game");
    createGameText.getStyleClass().add("menuItem");
    rightBox.getChildren().add(createGameText);
    createGameText.setOnMouseClicked(
        mouseEvent -> {
          if (!channelCreated) {
            channelCreated = true;
            TextInputDialog popup = new TextInputDialog();
            popup.setTitle("Create Channel");
            popup.setHeaderText("Enter a channel name");
            DialogPane dialogPane = popup.getDialogPane();
            dialogPane.getStyleClass().add("dialog-pane");
            Optional<String> newChannelName = popup.showAndWait();
            newChannelName.ifPresent(
                channelName -> {
                  channelName = newChannelName.get();
                  createChannel(channelName);
                  channelChat(channelName);
                  isHost = true;
                  popup.hide();
                });
          }
        });
  }

  /**
   * Create a timer that sends a request to the server every second to get the list of active
   * channels
   */
  public void lobbyTimer() {
    timer = new Timer();
    TimerTask timerTask =
        new TimerTask() {
          @Override
          public void run() {
            communicator.send("LIST");
          }
        };
    timer.schedule(timerTask, 100, 1000);
  }

  /**
   * Create a timer that sends a request to the server every second to get the list of active
   * channels
   */
  public void buttonTimer() {
    buttonTimer = new Timer();
    TimerTask timerTask =
        new TimerTask() {
          @Override
          public void run() {
            startText.setVisible(isHost);
          }
        };
    timer.schedule(timerTask, 100, 1000);
  }

  /**
   * Handle the channel
   *
   * @param message the message to be sent to the server
   */
  public void handleChannel(String message) {
    String[] channels;
    channels = message.replace("CHANNELS ", "").split("\n");

    channelNames.getChildren().clear();
    for (String channel : channels) {
      var channelName = new Text(channel + "\n");
      channelName.getStyleClass().add("stats");
      channelName.setOnMouseClicked(
          (e) -> {
            if (!channelCreated) {
              joinChannel(channel);
              channelChat(channel);
            }
          });
      channelNames.getChildren().add(channelName);
    }
  }

  /**
   * Create a chat channel
   *
   * @param name the name of the channel to be created
   */
  public void channelChat(String name) {
    channelCreated = true;

    var channelName = new Text(name);
    channelName.getStyleClass().add("heading");
    leftBox.getChildren().clear();
    leftBox.getChildren().add(channelName);

    chatBox = new TextFlow();
    chatBox.getStyleClass().add("TextField");
    chatBox.setMinWidth(300);
    chatBox.setPrefHeight(300);

    var userList = new TextFlow();
    userList.setMinWidth(200);
    chatBox.getChildren().add(userList);

    var msgBox = new TextField();
    msgBox.setPromptText("Send a message");
    msgBox.setPrefWidth(40);
    msgBox.getStyleClass().add("stats");
    leftBox.getChildren().add(msgBox);

    scroller = new ScrollPane();
    scroller.getStyleClass().add("scroll-pane");
    scroller.setMinWidth(gameWindow.getWidth() / 2);
    scroller.setPrefHeight(300);
    scroller.setFitToWidth(true);
    scroller.setContent(chatBox);
    leftBox.getChildren().add(scroller);

    Text instructions =
        new Text(
            "\n"
                + "Welcome to the lobby "
                + "\n"
                + "Type /name to change nickname \n"
                + "Type /quit to quit the channel \n"
                + "Type /start to start the game");

    instructions.getStyleClass().add("stats");
    chatBox.getChildren().add(instructions);

    msgBox.setOnKeyPressed(
        keyEvent -> {
          if (keyEvent.getCode() == KeyCode.ENTER && !msgBox.getText().isEmpty()) {
            if (msgBox.getText().startsWith("/start") && isHost == true) {
              communicator.send("START");
            } else if (msgBox.getText().startsWith("/quit")) {
              isHost = false;
              channelCreated = false;
              buttonTimer.cancel();
              leftBox.getChildren().removeAll(leftBox.getChildren());
              communicator.send("PART");
            } else if (msgBox.getText().startsWith("/name")) {
              String nick = msgBox.getText().replace("/name ", "");
              communicator.send("NICK " + nick);
            } else {
              String msg = msgBox.getText();
              communicator.send("MSG  " + msg);
            }
            msgBox.clear();
          }
        });
    bottomBox = new HBox();
    bottomBox.setSpacing(30);
    leftBox.getChildren().add(bottomBox);

    startText = new Text("Start Game");
    startText.getStyleClass().add("menuItem");
    bottomBox.getChildren().add(startText);
    startText.setOnMouseClicked(e -> communicator.send("START"));

    leaveText = new Text("Leave");
    leaveText.getStyleClass().add("menuItem");
    bottomBox.getChildren().add(leaveText);
    leaveText.setOnMouseClicked(
        e -> {
          isHost = false;
          channelCreated = false;
          buttonTimer.cancel();
          leftBox.getChildren().removeAll(leftBox.getChildren());
          communicator.send("PART");
        });
    buttonTimer();
  }

  /**
   * Handle the message
   *
   * @param message the message to be sent to the server
   */
  public void handleMsg(String message) {
    var current = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    Text msgTime = new Text("[" + current + "] " + (message.replace("MSG ", "")) + "\n");
    msgTime.getStyleClass().add("stats ");
    chatBox.getChildren().add(msgTime);
  }

  /**
   * Handle the user list
   *
   * @param message the message to be sent to the server
   */
  public void handleUserList(String message) {
    userList.getChildren().clear();
    String[] userNames = message.replace("USERS ", "").split("\n");
    for (String userName : userNames) {
      Text name = new Text(userName + ", ");
      name.getStyleClass().add("myname");
      userList.getChildren().add(name);

      if (userNames.length == 1) {
        isHost = true;
      }
    }
  }

  /** Handle an error message */
  public void handleError() {
    error = new Label("Error");
    error.getStyleClass();
    leftBox.getChildren().add(error);
  }

  /**
   * Join a channel
   *
   * @param message the message to be sent to the server
   */
  public void joinChannel(String message) {
    communicator.send("JOIN " + message);
  }

  /**
   * Create a new channel
   *
   * @param channelName the name of the channel to be created
   */
  public void createChannel(String channelName) {
    communicator.send("CREATE " + channelName);
  }

  /** Start the game by loading the multiplayer scene */
  public void startGame() {
    timer.cancel();
    buttonTimer.cancel();
    gameWindow.loadScene(new MultiplayerScene(gameWindow));
  }
}
