package player.controller;

import javafx.application.Platform;
import javafx.scene.control.Alert;

public class CaptureRunnable implements Runnable{
    CaptureFrames adapter;
    public CaptureRunnable(CaptureFrames adapter) {
        this.adapter = adapter;
    }
    @Override
    public void run() {
        if(!adapter.getIsDone()) {
            adapter.start();
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Capture Information");
                alert.setHeaderText("Capturing Frames Succeeded");
                alert.setContentText("Frames were Stored in choosen directory!");
                alert.showAndWait();

            });
        }
    }
}
