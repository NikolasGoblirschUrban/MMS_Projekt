package player.controller;

import com.xuggle.mediatool.MediaListenerAdapter;
import javafx.scene.control.Dialog;

public class CaptureRunnable implements Runnable{
    CaptureFrames adapter;
    public CaptureRunnable(CaptureFrames adapter) {
        this.adapter = adapter;
    }
    @Override
    public void run() {
        if(!adapter.getIsDone()) {
            adapter.start();

        }
    }
}
