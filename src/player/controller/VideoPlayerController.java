package player.controller;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class VideoPlayerController implements Initializable {
    private boolean isPlaying = false;
    private MediaPlayer mediaPlayer;
    private File selectedFile;
    private CaptureFrames capturer;

    @FXML
    private MediaView mvPlayer;
    @FXML
    private MenuBar mbMenu;
    @FXML
    private Button btnPlay;
    @FXML
    private Slider sldTime;
    @FXML
    private ToggleButton btnMute;
    @FXML
    private Slider sldVolume;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FileChooser fileChooser = new FileChooser();

        mbMenu.getMenus().get(1).getItems().get(1).setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File directory = directoryChooser.showDialog(null);
            if(directory != null) {
                capturer = new CaptureFrames(selectedFile, directory);
                Thread captureThread = new Thread(new CaptureRunnable(capturer));
                captureThread.start();
            }
        });

        mbMenu.getMenus().get(0).getItems().get(0).setOnAction(event -> {
            if (mediaPlayer != null) {
                mediaPlayer.dispose();
                setIsPlaying();
            }

            fileChooser.setTitle("Chose Video");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video", "*.mp4"));
            selectedFile = fileChooser.showOpenDialog(null);

            Media m = new Media(Paths.get(selectedFile.getAbsolutePath()).toUri().toString());
            mediaPlayer = new MediaPlayer(m);
            mvPlayer.setMediaPlayer(mediaPlayer);

            mediaPlayer.setOnEndOfMedia(() -> {
                mediaPlayer.seek(new Duration(0));
                setIsPlaying();
            });

            sldTime.maxProperty().bind(Bindings.createDoubleBinding(() -> mediaPlayer.getTotalDuration().toSeconds(),
                    mediaPlayer.totalDurationProperty()));

            mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) ->
                    sldTime.setValue(newValue.toSeconds()));

            sldVolume.setValue(mediaPlayer.getVolume() * 100);

            sldVolume.valueProperty().addListener(observable -> mediaPlayer.setVolume(sldVolume.getValue() / 100));
            mvPlayer.autosize();
        });

        mbMenu.getMenus().get(0).getItems().get(1).setOnAction(event -> {
            fileChooser.setTitle("Save Video");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video", "*.mp4"));
            try {
                Files.copy(selectedFile.toPath(), fileChooser.showSaveDialog(null).toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

    }

    public void handlePlay() {
        setIsPlaying();
    }

    public void handleState() {
        setIsPlaying();
    }

    private void setIsPlaying() {
        if (mediaPlayer != null) {
            if (isPlaying) {
                this.isPlaying = false;
                mediaPlayer.pause();
                btnPlay.setText("Play");
            } else {
                this.isPlaying = true;
                mediaPlayer.play();
                btnPlay.setText("Pause");
            }
        }
    }

    public void handleMute() {
        if (mediaPlayer != null) {
            mediaPlayer.setMute(btnMute.isSelected());
        }
    }

    public void handleTimeChange() {
        if (mediaPlayer != null) {
            mediaPlayer.seek(new Duration(sldTime.getValue() * 1000));
        }
    }

}