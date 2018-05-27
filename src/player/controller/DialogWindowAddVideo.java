package player.controller;


import player.videoTool.VideoEdit;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class DialogWindowAddVideo{
    private int startnumber1 = 0;
    private int startnumber2 = 0;
    private int endnumber2 = 0;
    private JDialog dialog;
    private JPanel panel;

    private JButton startButton;


    public DialogWindowAddVideo(String orignalFile, String secondFile ,String savefile){

        dialog = new JDialog();
        dialog.setTitle("Cut Video");
        dialog.setVisible(true);



        panel = new JPanel();

        JLabel startpoint1 = new JLabel("Start Point first Video");
        JLabel startpoint2 = new JLabel("Start Point second Video");
        JLabel endpoint2 = new JLabel("End Point second Video");
        JLabel ms = new JLabel("ms");

        JTextField numberstart1 = new JTextField(6);
        JTextField numberstart2 = new JTextField(6);
        JTextField numberend2 = new JTextField(6);

        startButton = new JButton("start");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startnumber1 = Integer.parseInt(numberstart1.getText());
                startnumber2 = Integer.parseInt(numberstart1.getText());
                endnumber2 = Integer.parseInt(numberend2.getText());

                VideoEdit.combineVideo(orignalFile, startnumber1, secondFile, startnumber2, endnumber2, savefile);

            }
        });



        GridLayout layout = new GridLayout(4,3, 10, 10);
        panel.setLayout(layout);

        panel.add("start Point", startpoint1);
        panel.add("numberstart", numberstart1);
        panel.add("ms", new JLabel("ms"));

        panel.add("start Point", startpoint2);
        panel.add("numberstart", numberstart2);
        panel.add("ms", new JLabel("ms"));

        panel.add("end Point", endpoint2);
        panel.add("numberend", numberend2);
        panel.add("ms", new JLabel("ms"));

        panel.add("", new JLabel(""));
        panel.add("", new JLabel(""));
        panel.add("start Button", startButton);
        dialog.add(panel);
        dialog.setSize(500,150);

    }
}
