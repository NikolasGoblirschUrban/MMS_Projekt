package player.videoResize;
import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.IMediaWriter;

import player.videoTool.VideoSize;
import com.xuggle.mediatool.ToolFactory;

public class ResizeVideo {

    /**
     *
     * @param videoPath     Videopfad
     * @param size          gewünschte Höhe und Breite
     * @return              Ausgabepfad
     */
    public static String ResizeVideo (String videoPath, VideoSize size){
        String videoOutput = videoPath.replace(".mp4", "-rezized.mp4");
        // create custom listeners
        ResizeVideo(videoPath, size, videoOutput);
        return videoOutput;
    }

    /**
     *
     * @param videoPath     Videopfad
     * @param size          gewünschte Höhe und Breite
     * @param videoOutput   Ausgabepfad
     */
    public static void ResizeVideo(String videoPath, VideoSize size, String videoOutput){
        // create custom listeners
        MyVideoListener myVideoListener = new MyVideoListener(size.getWidth(), size.getHigh());
        Resizer resizer = new Resizer(size.getWidth(), size.getHigh());

        // reader
        IMediaReader reader = ToolFactory.makeReader(videoPath);
        reader.addListener(resizer);

        // writer
        IMediaWriter writer = ToolFactory.makeWriter(videoOutput, reader);
        resizer.addListener(writer);
        writer.addListener(myVideoListener);

        // show video when encoding
        //   reader.addListener(ToolFactory.makeViewer(true));

        while (reader.readPacket() == null) {
        }
    }
}
