package player;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import player.controller.AudioController;
import player.controller.VideoPlayerController;

import java.io.File;
import java.io.IOException;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Main.primaryStage = primaryStage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("view/VideoPlayer.fxml"));
        Parent root = loader.load();

        primaryStage.getIcons().add(new Image("player/icons/icon.jpeg"));
        primaryStage.setScene(new Scene(root, 500, 500));
        primaryStage.setTitle("Java Media Player");
        primaryStage.setFullScreen(true);


        VideoPlayerController controller = loader.getController();
        controller.setMain(this);

        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

    //method to show the dialog window
    public File showEditAudio(File videofile) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("view/EditAudio.fxml"));
        Parent root = loader.load();

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Edit Sound");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(primaryStage);
        Scene scene = new Scene(root);
        dialogStage.setScene(scene);

        AudioController audioController = loader.getController();
        audioController.setVideoFile(videofile);

        dialogStage.showAndWait();
        return audioController.getNewVideoFile();
    }
}
