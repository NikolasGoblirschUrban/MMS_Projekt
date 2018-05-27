package player.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import player.model.AudioEditor;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class AudioController implements Initializable{

    private File musicFile;
    private AudioEditor audioEditor;
    private File videoFile;
    private File newVideoFile;

    @FXML
    private TextField txtStartposition;
    @FXML
    private TextField txtEndposition;
    @FXML
    private TextField txtMusicFile;
    @FXML
    private Button btwBrowse;
    @FXML
    private Button btwSubmit;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        audioEditor = new AudioEditor();
    }

    public File getNewVideoFile() {
        return newVideoFile;
    }

    public void setVideoFile(File videoFile) {
        this.videoFile = videoFile;
    }

    public void handleBrowse(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Musicfile");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Music", "*.mp3"));
        musicFile = fileChooser.showOpenDialog(null);
        txtMusicFile.setText(musicFile.getAbsolutePath());
    }



    public void handleSubmit(){
        int start = Integer.parseInt(txtStartposition.getText());
        int end = Integer.parseInt(txtEndposition.getText());
        String newfile = audioEditor.editAudio(videoFile, musicFile, start, end);
        newVideoFile = new File(newfile);
    }
}
