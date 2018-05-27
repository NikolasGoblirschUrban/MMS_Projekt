package player.videoTool;

import player.videoResize.ResizeVideo;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.IMediaWriter;


public class VideoCombine {

    /**
     *
     * @param sourceUrl1        Videopfad erster Teil
     * @param sourceUrl2        Videopfad mittlerer Teil
     * @param sourceUrl3        Videopfad schluss Teil
     * @param destinationUrl    Videopfad der Ausgabedatei
     */
    public static void combineThreeEqualVideos(String sourceUrl1, String sourceUrl2,String sourceUrl3,String destinationUrl)
    {
        String dest = destinationUrl.replace(".mp4", "-part1.mp4");
        combineTtwoEqualVideos(sourceUrl1, sourceUrl2, dest);
        combineTtwoEqualVideos(dest, sourceUrl3,destinationUrl);
        System.out.println("finished merging three");
    }

    /**
     *
     * @param sourceUrl1
     * @param sourceUrl2
     * @param destinationUrl
     */

    public static void combineTtwoEqualVideos(String sourceUrl1, String sourceUrl2,String destinationUrl)
    {
        long timestampVideo = 0;
        long timestampAudio = 0;
        VideoData videoData = new VideoData(sourceUrl1);
        IMediaReader reader1 = ToolFactory.makeReader(sourceUrl1);
        IMediaReader reader2 = ToolFactory.makeReader(sourceUrl2);

        MediaConcatenator concatenator = new MediaConcatenator(videoData.getStreamInfoAudio().getIndex(),
                videoData.getStreamInfoVideo().getIndex());

        reader1.addListener(concatenator);
        reader2.addListener(concatenator);

        IMediaWriter writer = ToolFactory.makeWriter(destinationUrl);
        concatenator.addListener(writer);

        writer.addVideoStream(0, 0,
                videoData.getCodecInfoVideo().getWidth(),videoData.getCodecInfoVideo().getHeight());
        writer.addAudioStream(1, 0,2, 44100);

        while(reader1.readPacket() == null);

        while(reader2.readPacket() == null);

        writer.close();
        System.out.println("finished merging two");
    }

    /**
     *
     * @param videoPath1
     * @param startPoint1
     * @param videoPath2
     * @param startPoint2
     * @param endpPoint2
     * @param videoOutput
     */
    public static void combineVideo(String videoPath1, long startPoint1, String videoPath2, long startPoint2, long endpPoint2, String videoOutput){

        String resizeString1 = videoPath1;
        String resizeString2 = videoPath2;

        System.out.println(resizeString1);
        VideoData videoData1 = new VideoData(videoPath1);
        VideoData videoData2 = new VideoData(videoPath2);
        VideoSize maxSize = new VideoSize(compairSize(videoData1, videoData2));

        // resize Videos
        VideoSize t1 = new VideoSize(videoData1.getCodecInfoVideo().getHeight(), videoData1.getCodecInfoVideo().getWidth());
        if(!maxSize.Compair(t1)){
            resizeString1 = videoPath1.replace(".mp4", "-resized.mp4");
            new ResizeVideo().ResizeVideo(videoPath1, maxSize, resizeString1);
        }
        VideoSize t2 = new VideoSize(videoData2.getCodecInfoVideo().getHeight(), videoData2.getCodecInfoVideo().getWidth());
        if(!maxSize.Compair(t2)){
            resizeString2 = videoPath2.replace(".mp4", "-resized.mp4");
            new ResizeVideo().ResizeVideo(videoPath2, maxSize, resizeString2);
        }


        //get all Videoparts
        String part1 = VideoCutter.VideoCutBegin(resizeString1, startPoint1);
        String part2 = VideoCutter.VideoCut(resizeString2, startPoint2, endpPoint2);
        String part3 = VideoCutter.VideoCutEnd(resizeString1, startPoint1);

        VideoCombine.combineThreeEqualVideos(part1, part2, part3, videoOutput);

    }

    /**
     * vergleicht die Videogrößen width und high und gibt in summe das Maximim zurück
     * @param Video1    Videogrößen von Video 1
     * @param Video2    Videogrößen von Video 2
     * @return          gibt die maximale höhe und breite zurück
     */

    public static VideoSize compairSize(VideoData Video1, VideoData Video2){
        VideoSize maxSize = new VideoSize();
        if(Video1.getCodecInfoVideo().getWidth() >= Video2.getCodecInfoVideo().getWidth()){
            maxSize.setWidth(Video1.getCodecInfoVideo().getWidth());
        } else {
            maxSize.setWidth(Video2.getCodecInfoVideo().getWidth());
        }

        if(Video1.getCodecInfoVideo().getHeight() >= Video2.getCodecInfoVideo().getHeight()){
            maxSize.setHigh(Video1.getCodecInfoVideo().getHeight());
        } else {
            maxSize.setHigh(Video2.getCodecInfoVideo().getHeight());
        }
        return maxSize;
    }

}
