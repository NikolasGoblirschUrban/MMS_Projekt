package player.videoTool;
import com.xuggle.mediatool.*;

public class VideoCutter {
    /**
     * schneidet ein mittlers Stück heraus
     * @param videoPath Videopfad
     * @param time1     erste Zeit in ms
     * @param time2     zweite Zeit in ms
     * @return
     */

    public static String VideoCut(String videoPath, long time1, long time2){
        String videoCut = "";
        videoCut = VideoCutBegin(videoPath,time2);
        videoCut = VideoCutEnd(videoCut, time1);
        return videoCut;
    }

    /**
     *
     * @param videoPath Videopfad
     * @param time      Zeitpunkt des schneidens in ms
     * @return          gibt die Zweite Hälfte zurück
     */


    public static String VideoCutEnd(String videoPath, long time){
        String videoCut = videoPath.replace(".mp4", "-end.mp4");
        VideoCutEnd(videoPath,time, videoCut);
        return videoCut;

    }

    /**
     *
     * @param videoPath     Videopfad
     * @param time          Zeitpunkt des schneidens in ms
     * @param videoOutput   Videopfad für die zweite Häfte aus Ausgabe
     */

    public static void VideoCutEnd(String videoPath, long time, String videoOutput){
        VideoData videoData = new VideoData(videoPath);
        String videoCut = videoPath.replace(".mp4", "-begin.mp4");
        IMediaReader reader = ToolFactory.makeReader(videoPath);


        MediaConcatenator cutChecker =  new MediaConcatenator(videoData.getStreamInfoAudio().getIndex(),
                videoData.getStreamInfoVideo().getIndex());

        reader.addListener(cutChecker);
        IMediaWriter writer = ToolFactory.makeWriter(videoCut);
        cutChecker.addListener(writer);

        writer.addVideoStream(0, 0,
                videoData.getCodecInfoVideo().getWidth(),videoData.getCodecInfoVideo().getHeight());
        writer.addAudioStream(1, 0,2, 44100);

        IMediaWriter writer2 = ToolFactory.makeWriter("");
        boolean updated1 = false;
        while (reader.readPacket() == null) {
            // 15 below is the point to split, in seconds
            if ((cutChecker.currentTimestamp >= time * 1000) && (!updated1)) {
                cutChecker.removeListener(writer);
                writer.close();
                writer2 = ToolFactory.makeWriter(videoOutput, reader);
                cutChecker.addListener(writer2);
                cutChecker.setChangeTimestamp((-1000)*time);
                cutChecker.setToggleChangeTimestamp(true);
                updated1 = true;
            }
        }

        cutChecker.removeListener(writer2);
        writer2.close();


    }

    /**
     *
     * @param videoPath Videopfad
     * @param time      Zeitpunkt des schneidens in ms
     * @return          gibt die erste Hälfte zurück
     */

    public static String VideoCutBegin(String videoPath, long time){
        String videoCut = videoPath.replace(".mp4", "-begin.mp4");
        VideoCutBegin(videoPath, time, videoCut);
        return videoCut;
    }

    /**
     *
     * @param videoPath     Videopfad
     * @param time          Zeitpunkt des schneidens in ms
     * @param videoOutput   Videopfad für die zweite Häfte aus Ausgabe
     */
    public static void VideoCutBegin(String videoPath, long time, String videoOutput){
        VideoData videoData = new VideoData(videoPath);
        String videoCut2 = videoPath.replace(".mp4", "-end.mp4");
        IMediaReader reader = ToolFactory.makeReader(videoPath);


        MediaConcatenator cutChecker =new MediaConcatenator(videoData.getStreamInfoAudio().getIndex(),
                videoData.getStreamInfoVideo().getIndex());
        reader.addListener(cutChecker);
        IMediaWriter writer = ToolFactory.makeWriter(videoOutput);
        cutChecker.addListener(writer);

        writer.addVideoStream(0, 0,
                videoData.getCodecInfoVideo().getWidth(),videoData.getCodecInfoVideo().getHeight());
        writer.addAudioStream(1, 0,2, 44100);
        IMediaWriter writer2 = ToolFactory.makeWriter("");
        boolean updated1 = false;
        while (reader.readPacket() == null) {
            // 15 below is the point to split, in seconds
            if ((cutChecker.currentTimestamp >= time * 1000) && (!updated1)) {
                cutChecker.removeListener(writer);
                writer.close();
                writer2 = ToolFactory.makeWriter(videoCut2, reader);
                cutChecker.addListener(writer2);
                cutChecker.setChangeTimestamp((-1000)*time);
                cutChecker.setToggleChangeTimestamp(true);
                updated1 = true;
            }
        }
        cutChecker.removeListener(writer2);
        writer2.close();


    }
}
