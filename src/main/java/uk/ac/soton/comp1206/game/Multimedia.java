package uk.ac.soton.comp1206.game;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/** The Multimedia class handles audio and music playback in the game. */
public class Multimedia {

  private static final Logger logger = LogManager.getLogger(Multimedia.class);

  /** The audio player for sound effects. */
  private static MediaPlayer audioPlayer;

  /** The music player for background music. */
  private static MediaPlayer musicPlayer;

  /**
   * Play an audio file.
   *
   * @param filePath the path to the audio file
   */
  public static void playAudio(String filePath) {
    Media audio = new Media(new File(filePath).toURI().toString());
    audioPlayer = new MediaPlayer(audio);
    audioPlayer.play();
  }

  /**
   * Play background music.
   *
   * @param filePath the path to the background music file
   */
  public static void playBackgroundMusic(String filePath) {
    Media music = new Media(new File(filePath).toURI().toString());
    musicPlayer = new MediaPlayer(music);
    musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
    musicPlayer.play();
  }

  /** Stop playing background music. */
  public static void stopBackgroundMusic() {
    if (musicPlayer != null) {
      musicPlayer.stop();
    }
  }
}
