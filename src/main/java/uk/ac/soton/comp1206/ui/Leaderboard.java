package uk.ac.soton.comp1206.ui;

import javafx.application.Platform;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.util.List;

/**
 * The Leaderboard class extends ScoresList to manage and display player statistics within the game.
 * It facilitates the rendering of player names, scores, and their corresponding statuses (whether
 * they are alive or dead).
 */
public class Leaderboard extends ScoreList {

  /**
   * Updates the leaderboard with the provided list of players and their respective performance
   * data.
   *
   * @param playerStats a list of pairs, where each pair contains a player's name and a pair of
   *     their score and status
   */
  public void updateLeaderboard(List<Pair<String, String>> playerStats) {
    Platform.runLater(
        () -> {
          getChildren().clear();
          playerStats.forEach(
              stats -> {
                String playerName = stats.getKey();
                String playerScore = stats.getValue();

                HBox scoreItem = new HBox();
                scoreItem.getStyleClass().add("stats");
                scoreItem.setSpacing(15);

                Text name = new Text(playerName);
                name.getStyleClass().add("stats");
                name.setFill(Color.WHITE);

                Text scoreText = new Text(playerScore.toString());
                scoreText.getStyleClass().add("stats");
                scoreText.setFill(Color.WHITE);

                scoreItem.getChildren().addAll(name, scoreText);
                getChildren().add(scoreItem);
              });
        });
  }

  /**
   * Converts a string into an integer value.
   *
   * @param number the string to be converted
   * @return the parsed integer value, or -1 if the string cannot be converted
   */
  public int parseInt(String number) {
    int num = -1;
    try {
      num = Integer.parseInt(number);
      return num;
    } catch (NumberFormatException e) {
      return -1;
    }
  }
}
