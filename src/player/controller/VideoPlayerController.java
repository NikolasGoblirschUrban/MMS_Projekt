package player.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import player.Main;

import javax.sound.midi.Track;
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
    private File saveFile;
    private File selectedsecondFile;
    private CaptureFrames capturer;
    private boolean isMute;
    private double currentRate;
    private Main main;


    @FXML
    private MediaView mvPlayer;
    @FXML
    private MenuBar mbMenu;
    @FXML
    private Button btnPlay;
    @FXML
    private Slider sldTime;
    @FXML
    private Button btnMute;
    @FXML
    private Slider sldVolume;
    @FXML
    private ProgressBar pbTime;
    @FXML
    private AnchorPane apScene;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FileChooser fileChooser = new FileChooser();

        pbTime.prefWidthProperty().bind(apScene.widthProperty());

        isMute = false;
        currentRate = 1;

        mbMenu.getMenus().get(1).getItems().get(3).setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File directory = directoryChooser.showDialog(null);
            if(directory != null) {
                capturer = new CaptureFrames(selectedFile, directory);
                Thread captureThread = new Thread(new CaptureRunnable(capturer));
                captureThread.start();
            }
        });

        mbMenu.getMenus().get(0).getItems().get(0).setOnAction(event -> {
            fileChooser.setTitle("Chose Video");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video", "*.mp4"));
            selectedFile = fileChooser.showOpenDialog(null);

            setMedia();

            DoubleProperty width = mvPlayer.fitWidthProperty();
            DoubleProperty height = mvPlayer.fitHeightProperty();
            width.bind(Bindings.selectDouble(mvPlayer.sceneProperty(), "width"));
            height.bind(Bindings.selectDouble(mvPlayer.sceneProperty(), "height"));

            mediaPlayer.setOnEndOfMedia(() -> {
                mediaPlayer.seek(new Duration(0));
                setIsPlaying();
            });

            DoubleBinding timeBinding = Bindings.createDoubleBinding(() -> mediaPlayer.getTotalDuration().toSeconds(),
                    mediaPlayer.totalDurationProperty());

            sldTime.maxProperty().bind(timeBinding);

            mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                        sldTime.setValue(newValue.toSeconds());
                        pbTime.setProgress(newValue.toSeconds() / timeBinding.doubleValue());
                    });

            sldVolume.setValue(mediaPlayer.getVolume() * 100);

            sldVolume.valueProperty().addListener(observable -> mediaPlayer.setVolume(sldVolume.getValue() / 100));

            pbTime.setProgress(0);

            setIsPlaying();
        });

        mbMenu.getMenus().get(0).getItems().get(1).setOnAction(event -> {
            saveVideo(fileChooser);
        });

        mbMenu.getMenus().get(1).getItems().get(0).setOnAction(event -> {
            /*TODO Lummi: Add your Code for add Subtitles here!*/
            if(selectedFile != null){
                saveVideo(fileChooser);
                try {
                    main.showSubtitles("Add");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        mbMenu.getMenus().get(1).getItems().get(1).setOnAction(event -> {
            /*TODO Lummi: Add your Code for edit Subtitles here!*/
            if(selectedFile != null) {
                saveVideo(fileChooser);
                try {
                    main.showSubtitles("Edit");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        mbMenu.getMenus().get(1).getItems().get(2).setOnAction(event -> {
            /*TODO Lummi: Add your Code for delete Subtitles here!*/
            if(selectedFile != null) {
                saveVideo(fileChooser);
            }
        });
        mbMenu.getMenus().get(1).getItems().get(4).setOnAction(event -> {
            /*TODO Lummi: Add your Code for edit Audio here!*/
            File editAudioFile;
            if(selectedFile != null) {
                try {
                    editAudioFile = main.showEditAudio(selectedFile);
                    selectedFile = editAudioFile;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        mbMenu.getMenus().get(1).getItems().get(5).setOnAction(event -> {
            if(selectedFile != null) {
                saveVideo(fileChooser);
                /*TODO Lummi: Add your Code for delete Audio here!*/
            }
        });
        mbMenu.getMenus().get(1).getItems().get(6).setOnAction(event -> {
            /*TODO Kastner:: Add your Code for Reverse here!*/
        });

        mbMenu.getMenus().get(1).getItems().get(4).setOnAction(event -> {
            mediaPlayer.pause();
            fileChooser.setTitle("Choose Savefile");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video", "*.mp4"));
            saveFile = fileChooser.showSaveDialog(null);

            DialogWindowCutVideo dialog = new DialogWindowCutVideo(selectedFile.getAbsolutePath(),  saveFile.getAbsolutePath());
        });
        mbMenu.getMenus().get(1).getItems().get(5).setOnAction(event -> {
            mediaPlayer.pause();
            fileChooser.setTitle("Choose Savefile");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video", "*.mp4"));
            saveFile = fileChooser.showSaveDialog(null);

            fileChooser.setTitle("Choose second Video");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video", "*.mp4"));
            selectedsecondFile = fileChooser.showOpenDialog(null);
            DialogWindowAddVideo dialog = new DialogWindowAddVideo(selectedFile.getAbsolutePath(), selectedsecondFile.getAbsolutePath(), saveFile.getAbsolutePath());
        });

        RadioMenuItem doubleSpeed = ((RadioMenuItem)mbMenu.getMenus().get(2).getItems().get(2));
        RadioMenuItem halfSpeed = ((RadioMenuItem)mbMenu.getMenus().get(2).getItems().get(0));
        RadioMenuItem normalSpeed = ((RadioMenuItem)mbMenu.getMenus().get(2).getItems().get(1));
        normalSpeed.setSelected(true);

        doubleSpeed.setOnAction(e -> {
            if(mediaPlayer != null) {
                if (doubleSpeed.isSelected()) {
                    this.currentRate = 2;
                    mediaPlayer.setRate(2);
                    if(halfSpeed.isSelected()) {
                        halfSpeed.setSelected(false);
                    }
                    if(normalSpeed.isSelected()) {
                        normalSpeed.setSelected(false);
                    }
                }
            }
        });

        halfSpeed.setOnAction(e -> {
            if(mediaPlayer != null) {
                if (halfSpeed.isSelected()) {
                    mediaPlayer.setRate(0.5);
                    this.currentRate = 0.5;
                    if(doubleSpeed.isSelected()) {
                        doubleSpeed.setSelected(false);
                    }
                    if(normalSpeed.isSelected()) {
                        normalSpeed.setSelected(false);
                    }

                }
            }
        });

        normalSpeed.setOnAction(e -> {
            if(mediaPlayer != null) {
                if (normalSpeed.isSelected()) {
                    mediaPlayer.setRate(1);
                    this.currentRate = 1;
                    if (halfSpeed.isSelected()) {
                        halfSpeed.setSelected(false);
                    }
                    if (doubleSpeed.isSelected()) {
                        doubleSpeed.setSelected(false);
                    }
                }
            }
        });

    }

    @FXML
    private void handlePlay() {
        setIsPlaying();
    }

    public void setMain(Main main) {
        this.main = main;
    }

    @FXML
    private void handleState() {
        setIsPlaying();
    }

    private void setIsPlaying() {
        if (mediaPlayer != null) {
            if (isPlaying) {
                this.isPlaying = false;
                mediaPlayer.pause();
                btnPlay.setId("playButton");
            } else {
                this.isPlaying = true;
                mediaPlayer.setRate(currentRate);
                mediaPlayer.play();
                btnPlay.setId("pauseButton");
            }
        }
    }

    @FXML
    private void handleMute() {
        if (mediaPlayer != null) {
            if(isMute){
                mediaPlayer.setMute(false);
                isMute = false;
                btnMute.setId("muteButton");
            } else {
                mediaPlayer.setMute(true);
                isMute = true;
                btnMute.setId("unmuteButton");
            }
        }
    }

    @FXML
    private void handleTimeChange() {
        if (mediaPlayer != null) {
            mediaPlayer.seek(new Duration(sldTime.getValue() * 1000));
        }
    }

    private void setMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
            setIsPlaying();
        }
        Media m = new Media(Paths.get(selectedFile.getAbsolutePath()).toUri().toString());
        mediaPlayer = new MediaPlayer(m);
        mvPlayer.setMediaPlayer(mediaPlayer);
        mvPlayer.setPreserveRatio(true);
    }

    private void saveVideo(FileChooser fileChooser){
        fileChooser.setTitle("Save Video");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video", "*.mp4"));
        try {
            File newFile = new File(String.valueOf(fileChooser.showSaveDialog(null).toPath()));
            Files.copy(selectedFile.toPath(), newFile.toPath());
            if(newFile != null){
                selectedFile = newFile;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}