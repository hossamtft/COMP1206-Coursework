package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

public interface NextPieceListener {
  /**
   * Handle the incoming game piece.
   *
   * @param nextPiece the piece to add to the game
   */
  void nextPiece(GamePiece nextPiece);
}
