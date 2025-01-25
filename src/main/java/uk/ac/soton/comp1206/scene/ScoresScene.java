package uk.ac.soton.comp1206.scene;

import java.io.*;
import java.util.ArrayList;

import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.ScoreList;

public class ScoresScene extends BaseScene {
  /** Logger for logging messages related to the ScoresScene class. */
  private static final Logger logger = LogManager.getLogger(ScoresScene.class);

  /** The Game object associated with this ScoresScene. */
  protected Game game;

  /** The Communicator object for communicating with the server. */
  private static Communicator communicator;

  /** Wrapper for holding the local scores as a list property. */
  protected SimpleListProperty<Pair<String, Integer>> localScores;

  /** Observable list that holds the local scores. */
  protected ObservableList<Pair<String, Integer>> localScoresList =
      FXCollections.observableArrayList();

  /** Wrapper for holding the remote scores as a list property. */
  protected SimpleListProperty<Pair<String, Integer>> remoteScore;

  /** Observable list that holds the remote scores. */
  protected ObservableList<Pair<String, Integer>> remoteScoresList;

  /** ArrayList for storing online scores received from the server. */
  protected ArrayList<Pair<String, Integer>> onlineScores = new ArrayList<>();

  /** Property for storing the user name. */
  private final SimpleStringProperty userName = new SimpleStringProperty("Name");

  /** Flag indicating whether a local score should be inserted. */
  boolean insertScore = false;

  /** Flag indicating whether an online score should be inserted. */
  boolean insertOnlineScore = false;

  /** VBox layout for organizing UI elements related to scores. */
  protected VBox scoreScreen;

  /** ScoreList for displaying local scores. */
  public ScoreList scoreList;

  /** ScoreList for displaying online scores. */
  public ScoreList onlineScoreList;

  /** Flag indicating whether to check for new scores. */
  public boolean checkScore = true;

  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public ScoresScene(GameWindow gameWindow, Game game) {
    super(gameWindow);
    this.game = game;
    logger.info("Creating Score Scene");
    communicator = gameWindow.getCommunicator();
  }

  /**
   * Initializes the ScoresScene by adding listeners, loading online scores, and setting up key
   * event handling.
   */
  public void initialise() {
    communicator.addListener(
        message ->
            Platform.runLater(
                () -> {
                  filterScore(message);
                }));

    // Load online scores from the server
    loadOnlineScores();

    // Handle key events to navigate back to the main menu when the ESCAPE key is pressed
    root.getScene()
        .setOnKeyPressed(
            event -> {
              if (event.getCode() == KeyCode.ESCAPE) {
                gameWindow.startMenu();
              }
            });
  }

  /** Builds the visual layout of the ScoresScene, including text elements and score lists. */
  public void build() {
    // Create a GamePane as the root node of the scene
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    // Create a StackPane to hold the score scene elements
    var scoreScenePane = new StackPane();
    scoreScenePane.setMaxHeight(gameWindow.getHeight());
    scoreScenePane.setMaxWidth(gameWindow.getWidth());
    scoreScenePane.getStyleClass().add("menu-background");
    root.getChildren().add(scoreScenePane);

    // Create a VBox to organize the score screen elements vertically
    scoreScreen = new VBox();
    scoreScreen.setAlignment(Pos.TOP_CENTER);
    scoreScenePane.getChildren().add(scoreScreen);

    // Add a title for all scores
    Text allScores = new Text("High Scores");
    allScores.getStyleClass().add("bigtitle");
    scoreScreen.getChildren().add(allScores);

    // Create an HBox to organize the title texts for local and online scores
    var scoreTitles = new HBox();
    scoreTitles.setAlignment(Pos.CENTER);
    scoreTitles.setSpacing(30);
    scoreScreen.getChildren().add(scoreTitles);

    // Create an HBox to organize the score lists for local and online scores
    var scoreBoxes = new HBox();
    scoreBoxes.setAlignment(Pos.BOTTOM_CENTER);
    scoreBoxes.setSpacing(30);
    scoreScreen.getChildren().add(scoreBoxes);

    // Add title text for local scores
    Text localScoresText = new Text("Local Scores");
    localScoresText.getStyleClass().add("title");
    scoreTitles.getChildren().add(localScoresText);

    // Add title text for online scores
    Text onlineScoresText = new Text("Online Scores");
    onlineScoresText.getStyleClass().add("title");
    scoreTitles.getChildren().add(onlineScoresText);

    // Initialize and bind properties for the local score list
    scoreList = new ScoreList();
    scoreBoxes.getChildren().add(scoreList);
    localScoresList = FXCollections.observableArrayList(loadScores());
    logger.info(localScoresList + " local scores list");
    localScores = new SimpleListProperty(localScoresList);
    scoreList.getScoresProperty().bind(localScores);
    scoreList.getName().bind(userName);

    // Initialize and bind properties for the online score list
    onlineScoreList = new ScoreList();
    scoreBoxes.getChildren().add(onlineScoreList);
    remoteScoresList = FXCollections.observableArrayList(onlineScores);
    remoteScore = new SimpleListProperty(remoteScoresList);
    onlineScoreList.getScoresProperty().bind(remoteScore);
    onlineScoreList.getName().bind(userName);
  }

  /**
   * Writes the provided scores to a file.
   *
   * @param scores The list of scores to be written to the file.
   */
  public static void writeScores(ObservableList<Pair<String, Integer>> scores) {
    try {
      File file = new File("scores.txt");
      if (!file.exists()) {
        // Add default scores if the file doesn't exist
        scores.add(new Pair<>("Player 1", 10));
        scores.add(new Pair<>("Player 2", 120));
        scores.add(new Pair<>("Player 3", 50));
        scores.add(new Pair<>("Player 4", 60));
        scores.add(new Pair<>("Player 5", 10));
        scores.add(new Pair<>("Player 6", 16));
      }
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
        int count = 0;
        for (Pair<String, Integer> score : scores) {
          writer.write(score.getKey() + ":" + score.getValue() + "\n");
          count++;
          if (count == 10) {
            writer.close();
            break;
          }
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } catch (RuntimeException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * method to split the message received into parts and handle when the message read "HISCORES" The
   * scores are then further filtered and put into a remote score list
   *
   * @param message
   */
  public void filterScore(String message) {
    logger.info("Filter Score");
    onlineScores.clear();
    String[] parts = message.split(" ", 2);
    String receivedScore = "null";
    if (parts[0].equals("HISCORES")) {
      if (parts.length > 1) {
        receivedScore = parts[1];
      }
    }

    String[] lines = receivedScore.split("\n");
    for (String line : lines) {
      String[] component = line.split(":", 2);
      onlineScores.add(new Pair(component[0], Integer.parseInt(component[1])));
    }
    onlineScores.sort((s1, s2) -> (s2.getValue().compareTo(s1.getValue())));
    logger.info(remoteScoresList + " remote scores list");
    remoteScoresList.clear();
    remoteScoresList.addAll(onlineScores);
    if (checkScore) {
      checkScore = false;
      scoreScene();
    }
    scoreList.reveal();
    onlineScoreList.reveal();
  }

  /**
   * method to check whether a score obtained by the user is a new local or online high score. If so
   * the user is prompted to enter his or her name and the list is iterated through and compared to
   * the score which is then put into the right position in the list.
   */
  public void scoreScene() {
    int newScore = game.getScore();

    int counter = 0;
    int onlineCounter = 0;

    if (localScoresList.size() < 10) {
      insertScore = true;
    }

    // loop through the scores and see where the new score ranks
    for (Pair<String, Integer> score : localScoresList) {
      if (score.getValue() < newScore) {
        logger.info("local score");
        insertScore = true;
        break;
      }
      counter++;
    }

    for (Pair<String, Integer> score : remoteScoresList) {
      if (score.getValue() < newScore) {
        insertOnlineScore = true;
        break;
      }
      onlineCounter++;
    }

    if (insertOnlineScore || insertScore) {
      logger.info("Checking local score");

      var enter = new Button("Enter");
      enter.getStyleClass().add("menuItem");

      var highScoreText = new Text();
      highScoreText.setText("New High Score");
      highScoreText.getStyleClass().add("hiscore");

      var nameField = new TextField();
      nameField.setPromptText("Enter your name");
      nameField.requestFocus();
      nameField.setMaxWidth(300);

      scoreScreen.getChildren().add(highScoreText);
      scoreScreen.getChildren().add(nameField);
      scoreScreen.getChildren().add(enter);

      int onlineIndex = onlineCounter;
      int index = counter;

      enter.setOnAction(
          (event) -> {
            String name = nameField.getText();
            userName.set(name);
            scoreScreen.getChildren().remove(highScoreText);
            scoreScreen.getChildren().remove(nameField);
            scoreScreen.getChildren().remove(enter);

            if (insertScore) {
              localScoresList.add(index, new Pair<String, Integer>(name, newScore));
            }
            if (insertOnlineScore) {
              remoteScoresList.add(onlineIndex, new Pair<String, Integer>(name, newScore));
              communicator.send("HISCORE " + name + ":" + newScore);
            }

            writeScores(localScoresList);
            writeOnlineScores(name, newScore);
            loadOnlineScores();
            insertOnlineScore = false;
          });
    } else {
      scoreList.reveal();
      onlineScoreList.reveal();
    }
  }

  /**
   * method to load the new high scores within a list if a file does not exist, the program runs the
   * write scores method to put in default scores.
   *
   * @return
   */
  public static ArrayList<Pair<String, Integer>> loadScores() {

    File file = new File("scores.txt");
    ArrayList<Pair<String, Integer>> scores = new ArrayList<>();
    BufferedReader bufferedReader = null;
    String line;
    if (!file.exists()) {
      writeScores(FXCollections.observableArrayList(scores));
    }
    try {
      bufferedReader = new BufferedReader(new FileReader(file));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    try {
      while ((line = bufferedReader.readLine()) != null) {
        String[] newLine = line.split(":");
        scores.add(new Pair<>(newLine[0], Integer.parseInt(newLine[1])));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      bufferedReader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    scores.sort((s1, s2) -> (s2.getValue().compareTo(s1.getValue())));
    logger.info(scores + " local scores list");
    return scores;
  }

  /** loading online high scores scores */
  public static void loadOnlineScores() {
    communicator.send("HISCORES");
  }

  /**
   * writing online high scores
   *
   * @param name
   * @param score
   */
  public static void writeOnlineScores(String name, Integer score) {
    communicator.send("HISCORE" + name + " :" + score);
  }
}
