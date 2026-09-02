package sallman.gui;

import java.io.IOException;
import java.util.Collections;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * One message in the conversation: what was said, beside the avatar of whoever
 * said it.
 */
public class DialogBox extends HBox {

    @FXML
    private Label dialog;
    @FXML
    private ImageView displayPicture;

    private DialogBox(String text, Image img) {
        try {
            // This box is both the root of the loaded layout and its controller,
            // which is what lets the same layout be built once per message.
            FXMLLoader fxmlLoader = new FXMLLoader(MainWindow.class.getResource("/view/DialogBox.fxml"));
            fxmlLoader.setController(this);
            fxmlLoader.setRoot(this);
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        dialog.setText(text);
        displayPicture.setImage(img);
    }

    /** Puts the avatar on the left and the text on the right. */
    private void flip() {
        ObservableList<Node> nodes = FXCollections.observableArrayList(this.getChildren());
        Collections.reverse(nodes);
        getChildren().setAll(nodes);
        setAlignment(Pos.TOP_LEFT);
        // The bubble's square corner points at the avatar, so flipping the box
        // has to move the corner too.
        dialog.getStyleClass().add("reply-label");
    }

    /**
     * Colours the bubble by the kind of command being answered, so the effect of
     * a command can be seen before the reply is read.
     *
     * @param commandType simple class name of the command, or an empty string
     */
    private void colourByCommand(String commandType) {
        switch (commandType) {
            case "AddCommand":
                dialog.getStyleClass().add("add-label");
                break;
            case "MarkCommand":
                dialog.getStyleClass().add("marked-label");
                break;
            case "DeleteCommand":
                dialog.getStyleClass().add("delete-label");
                break;
            default:
                // Anything else keeps the plain bubble.
        }
    }

    /**
     * Returns a box showing something the user typed.
     *
     * @param text what the user said
     * @param img  the user's avatar
     * @return the dialog box to add to the conversation
     */
    public static DialogBox getUserDialog(String text, Image img) {
        return new DialogBox(text, img);
    }

    /**
     * Returns a box showing one of the chatbot's replies, flipped so the two
     * speakers line up on opposite sides.
     *
     * @param text        what the chatbot said
     * @param img         the chatbot's avatar
     * @param commandType simple class name of the command that produced the
     *                    reply, used to colour the bubble
     * @return the dialog box to add to the conversation
     */
    public static DialogBox getSallmanDialog(String text, Image img, String commandType) {
        DialogBox box = new DialogBox(text, img);
        box.flip();
        box.colourByCommand(commandType);
        return box;
    }
}
