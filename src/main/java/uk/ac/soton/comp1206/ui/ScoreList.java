package uk.ac.soton.comp1206.ui;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;

import java.util.ArrayList;

public class ScoreList extends VBox {

  public SimpleListProperty<Pair<String, Integer>> scoreList = new SimpleListProperty<>();
  protected StringProperty name = new SimpleStringProperty();
  public ArrayList<HBox> scoreBoxList = new ArrayList<>();
  int numberOfScores;
  int amountOfUsers = 9;

  /** Create a new ScoreList */
  public ScoreList() {
    setAlignment(Pos.CENTER);
    this.scoreList.addListener((InvalidationListener) e -> updateScore());
  }

  /** method to update the score list. */
  public void updateScore() {
    Text scoreTitle = new Text();
    scoreTitle.getStyleClass().add("");
    scoreBoxList.clear();
    getChildren().clear();

    for (Pair<String, Integer> score : scoreList) {
      numberOfScores++;
      if (numberOfScores == amountOfUsers) break;

      HBox scoreBox = new HBox();
      scoreBox.setAlignment(Pos.CENTER);

      Text userName = new Text(score.getKey() + ":");
      userName.getStyleClass().add("scorelist");
      if (score.getKey().equals(name.get())) {
        userName.getStyleClass().add("scorelist");
      }

      Text userScore = new Text(score.getValue().toString());
      userScore.getStyleClass().add("scorelist");

      scoreBox.getChildren().addAll(userName, userScore);
      getChildren().add(scoreBox);
      scoreBoxList.add(scoreBox);
      reveal();
    }
  }

  /** method to do the reveal animation for the scores list */
  public void reveal() {
    ArrayList<Transition> transition = new ArrayList<>();
    for (HBox nodes : scoreBoxList) {
      FadeTransition f = new FadeTransition(new Duration(500), nodes);
      f.setFromValue(0);
      f.setToValue(1);
      f.play();
      transition.add(f);
    }
    SequentialTransition sT = new SequentialTransition(transition.toArray(e -> new Animation[e]));
    sT.play();
  }

  /**
   * method to get the scores
   *
   * @return score list
   */
  public SimpleListProperty<Pair<String, Integer>> getScoresProperty() {
    return scoreList;
  }

  /**
   * method to get the name of the user
   *
   * @return name
   */
  public StringProperty getName() {
    return name;
  }
}
