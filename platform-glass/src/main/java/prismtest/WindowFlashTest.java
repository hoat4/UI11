package prismtest;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class WindowFlashTest extends Application {

    public static class L {

        public static void main(String[] args) {
            Application.launch(WindowFlashTest.class);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Text text = new Text("Hello world!");
        text.setLayoutX(100);
        text.setLayoutY(100);
        primaryStage.setScene(new Scene(new Group(text), Color./*FIREBRICK*/WHITE));
        primaryStage.setWidth(500);
        primaryStage.setHeight(500);
        primaryStage.show();
    }
}
