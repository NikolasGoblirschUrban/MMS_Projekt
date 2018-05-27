package player.controller;


import player.videoTool.VideoEdit;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class DialogWindowCutVideo{
    private int startnumber = 0;
    private int endnumber = 0;
    private JDialog dialog;
    private JPanel panel;

    private JButton startButton;


    public DialogWindowCutVideo(String orignalFile, String savefile){

        dialog = new JDialog();
        dialog.setTitle("Cut Video");
        dialog.setVisible(true);



        panel = new JPanel();

        JLabel startpoint = new JLabel("Start Point");
        JLabel endpoint = new JLabel("End Point");
        JLabel ms = new JLabel("ms");

        JTextField numberstart = new JTextField(6);
        JTextField numberend = new JTextField(6);

        startButton = new JButton("start");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startnumber = Integer.parseInt(numberstart.getText());
                endnumber = Integer.parseInt(numberend.getText());

                VideoEdit.VideoCut(orignalFile,startnumber, endnumber,  savefile);

            }
        });



        GridLayout layout = new GridLayout(3,3, 10, 10);
        panel.setLayout(layout);

        panel.add("start Point", startpoint);
        panel.add("numberstart", numberstart);
        panel.add("ms", new JLabel("ms"));

        panel.add("end Point", endpoint);
        panel.add("numberend", numberend);
        panel.add("ms", new JLabel("ms"));

        panel.add("", new JLabel(""));
        panel.add("", new JLabel(""));
        panel.add("start Button", startButton);
        dialog.add(panel);
        dialog.setSize(400,150);

    }
}
