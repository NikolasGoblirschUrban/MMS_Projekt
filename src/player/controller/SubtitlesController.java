package player.controller;

import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class SubtitlesController implements Initializable {

    private String type;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setType(String type) {
        this.type = type;
    }
}
