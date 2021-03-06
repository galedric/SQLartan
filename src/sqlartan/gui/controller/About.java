package sqlartan.gui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;

/**
 * Controller for the About.fxml.
 */
public class About {

	@FXML
	private TextArea description;

	@FXML
	private Pane mainPane;

	/**
	 * First method call when FXML loaded.
	 * Used to disable mouse action on the description textArea.
	 */
	@FXML
	private void initialize() {
		description.setFocusTraversable(false);
		description.setMouseTransparent(true);
	}
}
