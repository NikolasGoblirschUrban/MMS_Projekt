package player.controller;

import javafx.application.Platform;
import javafx.scene.control.Alert;

public class ReverseRunnable implements Runnable{
    ReverseVideo adapter;
    public ReverseRunnable(ReverseVideo adapter) {
        this.adapter = adapter;
    }
    @Override
    public void run() {
        if(!adapter.getIsDone()) {
            adapter.start();
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Reverse Information");
                alert.setHeaderText("Reversing Video Succeeded");
                alert.setContentText("Reversed video was stored chosen directory!");
                alert.showAndWait();

            });
        }
    }
}
