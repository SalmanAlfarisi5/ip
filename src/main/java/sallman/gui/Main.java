package sallman.gui;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import sallman.Sallman;

/**
 * The JavaFX application that puts a window around the chatbot.
 */
public class Main extends Application {

    /** Where the task list is saved, relative to the folder the app runs in. */
    private static final String DATA_PATH = "data/sallman.txt";

    /**
     * The chatbot itself, told not to print to the console because the window
     * displays its replies instead.
     */
    private final Sallman sallman = new Sallman(DATA_PATH, false);

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/MainWindow.fxml"));
            AnchorPane root = fxmlLoader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("saLLMan");

            // Below roughly this size the layout stops making sense: the input
            // row and one line of dialogue need the height, and the Send button
            // needs the width.
            stage.setMinHeight(220);
            stage.setMinWidth(417);

            fxmlLoader.<MainWindow>getController().setSallman(sallman);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
