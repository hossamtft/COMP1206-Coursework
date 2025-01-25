package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlock;

/** Right Click Listener is used to handle the event when a block is right-clicked. */
public interface RightClickListener {

  /** Handle the block being right-licked. */
  void rightClicked();
}
