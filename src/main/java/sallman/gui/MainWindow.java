package sallman.gui;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import sallman.Sallman;

/**
 * Controller for the main window.
 */
public class MainWindow extends AnchorPane {

    /** How long the goodbye stays on screen before the window closes. */
    private static final Duration EXIT_DELAY = Duration.seconds(1.5);

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox dialogContainer;
    @FXML
    private TextField userInput;
    @FXML
    private Button sendButton;

    private Sallman sallman;

    private final Image userImage = new Image(this.getClass().getResourceAsStream("/images/DaUser.png"));
    private final Image sallmanImage = new Image(this.getClass().getResourceAsStream("/images/DaSallman.png"));

    /** Makes the dialogue scroll to the newest message as it grows. */
    @FXML
    public void initialize() {
        scrollPane.vvalueProperty().bind(dialogContainer.heightProperty());
    }

    /**
     * Hands the window the chatbot it should talk to, and shows its greeting.
     *
     * @param sallman the chatbot answering the user's commands
     */
    public void setSallman(Sallman sallman) {
        this.sallman = sallman;
        dialogContainer.getChildren().add(
                DialogBox.getSallmanDialog(sallman.getGreeting(), sallmanImage, ""));
    }

    /**
     * Shows what the user typed and the reply to it, then clears the input box.
     * <p>
     * A command that ends the session leaves its reply on screen briefly before
     * the window closes, so the goodbye can be read.
     */
    @FXML
    private void handleUserInput() {
        String input = userInput.getText();
        if (input.isBlank()) {
            return;
        }
        String response = sallman.getResponse(input);
        dialogContainer.getChildren().addAll(
                DialogBox.getUserDialog(input, userImage),
                DialogBox.getSallmanDialog(response, sallmanImage, sallman.getLastCommandType()));
        userInput.clear();

        if (sallman.isExitRequested()) {
            PauseTransition pause = new PauseTransition(EXIT_DELAY);
            pause.setOnFinished(event -> Platform.exit());
            pause.play();
        }
    }
}
