package uk.ac.soton.comp1206.game;

import uk.ac.soton.comp1206.component.GameBoard;

public class PieceBoard extends GameBoard {
  public PieceBoard(Grid grid, double width, double height) {

    super(grid, width, height);
  }

  /**
   * Set the piece to be displayed in the PieceBoard
   *
   * @param piece the piece to be displayed
   */
  public void setPiece(GamePiece piece) {
    getGrid().clear();
    getGrid().playPiece(piece, 1, 1);
  }

  /**
   * Places middle dot in the PieceBoard
   */
  public void middleDot() {
    double midX = Math.ceil((double) getGrid().getRows() / 2) - 1;
    double midY = Math.ceil((double) getGrid().getCols() / 2) - 1;
    getBlocks()[(int) midX][(int) midY].setDot();
  }
}
