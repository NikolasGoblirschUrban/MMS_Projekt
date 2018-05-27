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
import player.model.AudioEditor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class VideoPlayerController implements Initializable {
    private boolean isPlaying = false;  //current playing state
    private MediaPlayer mediaPlayer;
    private File selectedFile; //current Video
    private File saveFile;
    private File selectedsecondFile;
    private CaptureFrames capturer; //For Capturing Frames
    private ReverseVideo reverser;
    private boolean isMute; //current mute state
    private double currentRate;
    private Main main;
    private AudioEditor audioEditor;

    //Fields from FXML File
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

    //Initialize while starting app
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FileChooser fileChooser = new FileChooser();

        audioEditor = new AudioEditor();

        pbTime.prefWidthProperty().bind(apScene.widthProperty());//Bind Progressbars width to window width

        isMute = false;
        currentRate = 1;

        mbMenu.getMenus().get(1).getItems().get(3).setOnAction(event -> { //MenuItem Capture Frames
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File directory = directoryChooser.showDialog(null); //Chosen Directory for Frame Images
            if(directory != null) {
                capturer = new CaptureFrames(selectedFile, directory);
                Thread captureThread = new Thread(new CaptureRunnable(capturer)); //Thread in which frames are captured
                captureThread.start();
            }
        });

        mbMenu.getMenus().get(0).getItems().get(0).setOnAction(event -> { //Open Video MenuItem
            fileChooser.setTitle("Chose Video");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video", "*.mp4"));
            selectedFile = fileChooser.showOpenDialog(null); //Current Video

            setMedia(); //initializing MediaView

            mediaPlayer.setOnEndOfMedia(() -> { //Seek to beginning when Video ends
                mediaPlayer.seek(new Duration(0));
                setIsPlaying(); //set to Pause at the beginning
            });

            setIsPlaying(); //start Video after loading
        });

        mbMenu.getMenus().get(0).getItems().get(1).setOnAction(event -> { //Save Video
            saveVideo(fileChooser);
        });

        mbMenu.getMenus().get(1).getItems().get(0).setOnAction(event -> {
            //Add Subtitles-Function is not available, because the audio-Modul took too much time.
        });
        mbMenu.getMenus().get(1).getItems().get(1).setOnAction(event -> {
            //Add Subtitles-Function is not available, because the audio-Modul took too much time.
        });
        mbMenu.getMenus().get(1).getItems().get(2).setOnAction(event -> {
            //Add Subtitles-Function is not available, because the audio-Modul took too much time.
        });
        mbMenu.getMenus().get(1).getItems().get(4).setOnAction(event -> { //Edit Audio
            File editAudioFile;
            if(selectedFile != null) {
                try {
                    editAudioFile = main.showEditAudio(selectedFile); //Insert Audio in Video -> Open Dialogwindow
                    selectedFile = editAudioFile;
                    saveVideo(fileChooser); //save it
                    setMedia(); //update media
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        mbMenu.getMenus().get(1).getItems().get(5).setOnAction(event -> { //Delete AUdio
            if(selectedFile != null) {
                selectedFile = new File(audioEditor.deleteAudio(selectedFile, "tempvideo.mp4")); //Delete audio
                saveVideo(fileChooser); //Save video
                setMedia(); // update media
            }
        });
        mbMenu.getMenus().get(1).getItems().get(6).setOnAction(event -> { //Reverse Video
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File directory = directoryChooser.showDialog(null);
            if(directory != null) {
                reverser = new ReverseVideo(selectedFile, directory, (int)mediaPlayer.getTotalDuration().toSeconds());
                Thread reverseThread = new Thread(new ReverseRunnable(reverser));
                reverseThread.start();
            }
        });

        mbMenu.getMenus().get(1).getItems().get(7).setOnAction(event -> {
            if(isPlaying) {
                setIsPlaying();
            }
            fileChooser.setTitle("Choose Savefile");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video", "*.mp4"));
            saveFile = fileChooser.showSaveDialog(null);

            DialogWindowCutVideo dialog = new DialogWindowCutVideo(selectedFile.getAbsolutePath(),  saveFile.getAbsolutePath());
        });
        mbMenu.getMenus().get(1).getItems().get(8).setOnAction(event -> {
            if(isPlaying) {
                setIsPlaying();
            }
            fileChooser.setTitle("Choose Savefile");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video", "*.mp4"));
            saveFile = fileChooser.showSaveDialog(null);

            fileChooser.setTitle("Choose second Video");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video", "*.mp4"));
            selectedsecondFile = fileChooser.showOpenDialog(null);
            DialogWindowAddVideo dialog = new DialogWindowAddVideo(selectedFile.getAbsolutePath(), selectedsecondFile.getAbsolutePath(), saveFile.getAbsolutePath());
        });

        RadioMenuItem doubleSpeed = ((RadioMenuItem)mbMenu.getMenus().get(2).getItems().get(2));  // Add Handler to MenuItems for speed
        RadioMenuItem halfSpeed = ((RadioMenuItem)mbMenu.getMenus().get(2).getItems().get(0));
        RadioMenuItem normalSpeed = ((RadioMenuItem)mbMenu.getMenus().get(2).getItems().get(1));
        normalSpeed.setSelected(true); //Select Speed 1x

        doubleSpeed.setOnAction(e -> { //When klicking 2x Speed
            if(mediaPlayer != null) {
                if (doubleSpeed.isSelected()) {
                    this.currentRate = 2;
                    mediaPlayer.setRate(2); //Rate indicates speed
                    if(halfSpeed.isSelected()) {
                        halfSpeed.setSelected(false);
                    }
                    if(normalSpeed.isSelected()) {
                        normalSpeed.setSelected(false);
                    }
                }
            }
        });

        halfSpeed.setOnAction(e -> { //0.5x Speed
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

        normalSpeed.setOnAction(e -> { //1x Speed
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
    private void handlePlay() { //Eventhandler for Play Button
        setIsPlaying();
    }

    public void setMain(Main main) {
        this.main = main;
    }

    @FXML
    private void handleState() {
        setIsPlaying();
    }

    private void setIsPlaying() { //Toggles isPlaying and Button icon
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
    private void handleMute() { //Toggles Mute and mute button icon
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
    private void handleTimeChange() { //Hander vor Slider seeks to selected position
        if (mediaPlayer != null) {
            mediaPlayer.seek(new Duration(sldTime.getValue() * 1000));
        }
    }

    private void setMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.dispose(); //if video is currently open dispose it
            setIsPlaying();
        }
        Media m = new Media(Paths.get(selectedFile.getAbsolutePath()).toUri().toString()); //Video
        mediaPlayer = new MediaPlayer(m); //Set Media for MediaPlayer
        mvPlayer.setMediaPlayer(mediaPlayer); //set MediaPlayer for MediaView
        mvPlayer.setPreserveRatio(true);

        DoubleProperty width = mvPlayer.fitWidthProperty(); //Bind VIdeo height an Width to Scene Height and width for resizeing window
        DoubleProperty height = mvPlayer.fitHeightProperty();
        width.bind(Bindings.selectDouble(mvPlayer.sceneProperty(), "width"));
        height.bind(Bindings.selectDouble(mvPlayer.sceneProperty(), "height"));

        DoubleBinding timeBinding = Bindings.createDoubleBinding(() -> mediaPlayer.getTotalDuration().toSeconds(),
                mediaPlayer.totalDurationProperty()); //Bind Slider Max to Medias total  time

        sldTime.maxProperty().bind(timeBinding);

        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            sldTime.setValue(newValue.toSeconds());
            pbTime.setProgress(newValue.toSeconds() / timeBinding.doubleValue());
        }); //Listener, when video current time changes update slider and progressbar

        sldVolume.setValue(mediaPlayer.getVolume() * 100); //Set Medias Volume to slider for volume

        sldVolume.valueProperty().addListener(observable -> mediaPlayer.setVolume(sldVolume.getValue() / 100));
        //Eventhandler updates Volume when volumeslider changes

        pbTime.setProgress(0); //Set progressbar to beginning

    }

    private void saveVideo(FileChooser fileChooser){ //Save Video
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