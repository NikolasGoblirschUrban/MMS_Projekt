package VideoTool;
import ResizeVideo.*;


public class VideoEdit {
    /**
     *  Videoformat ändern
     * @param videoPath     Videopfad
     * @param size          gewünschte Höhe und Breite
     * @return              Ausgabepfad
     */
    public static String ResizeVideo (String videoPath, VideoSize size){
        return ResizeVideo.ResizeVideo (videoPath, size);
    }
    /**
     *  Videoformat ändern
     * @param videoPath     Videopfad
     * @param size          gewünschte Höhe und Breite
     * @param videoOutput   Ausgabepfad
     */
    public static void ResizeVideo(String videoPath, VideoSize size, String videoOutput){
        ResizeVideo.ResizeVideo (videoPath, size, videoOutput);
    }

    /**
     * combiniert 2 Videos in der Mitte von Video 1, und dem Herausgeschnittenen Video 2
     * @param videoPath1
     * @param startPoint1
     * @param videoPath2
     * @param startPoint2
     * @param endpPoint2
     * @param videoOutput
     */
    public static void combineVideo(String videoPath1, long startPoint1, String videoPath2, long startPoint2, long endpPoint2, String videoOutput){
        VideoCombine.combineVideo(videoPath1, startPoint1, videoPath2, startPoint2, endpPoint2, videoOutput);
    }

    /**
     * vergleicht die Videogrößen width und high und gibt in summe das Maximim zurück
     * @param Video1    Videogrößen von Video 1
     * @param Video2    Videogrößen von Video 2
     * @return          gibt die maximale höhe und breite zurück
     */

    private static VideoSize compairSize(VideoData Video1, VideoData Video2){
        return VideoCombine.compairSize(Video1, Video2);
    }

    /**
     * Kombiniert drei gleiche Videos
     * @param sourceUrl1        Videopfad erster Teil
     * @param sourceUrl2        Videopfad mittlerer Teil
     * @param sourceUrl3        Videopfad schluss Teil
     * @param destinationUrl    Videopfad der Ausgabedatei
     */
    public static void combineThreeEqualVideos(String sourceUrl1, String sourceUrl2,String sourceUrl3,String destinationUrl){
        VideoCombine.combineThreeEqualVideos(sourceUrl1, sourceUrl2, sourceUrl3, destinationUrl);
    }

    /**
     * Kombiniert zwei gleiche Videos
     * @param sourceUrl1
     * @param sourceUrl2
     * @param destinationUrl
     */

    public static void combineTtwoEqualVideos(String sourceUrl1, String sourceUrl2,String destinationUrl){
        VideoCombine.combineTtwoEqualVideos(sourceUrl1, sourceUrl2,destinationUrl);
    }
    /**
     * schneidet ein mittlers Stück heraus
     * @param videoPath Videopfad
     * @param time1     erste Zeit in ms
     * @param time2     zweite Zeit in ms
     * @return
     */

    public static String VideoCut(String videoPath, long time1, long time2){
       return VideoCutter.VideoCut(videoPath, time1, time2);
    }

    /**
     *
     * @param videoPath Videopfad
     * @param time      Zeitpunkt des schneidens in ms
     * @return          gibt die Zweite Hälfte zurück
     */
    public static String VideoCutEnd(String videoPath, long time){
        return VideoCutter.VideoCutEnd(videoPath, time);
    }

    /**
     *
     * @param videoPath     Videopfad
     * @param time          Zeitpunkt des schneidens in ms
     * @param videoOutput   Videopfad für die zweite Häfte aus Ausgabe
     */

    public static void VideoCutEnd(String videoPath, long time, String videoOutput){
        VideoCutter.VideoCutEnd(videoPath, time, videoOutput);
    }

    /**
     *
     * @param videoPath Videopfad
     * @param time      Zeitpunkt des schneidens in ms
     * @return          gibt die erste Hälfte zurück
     */

    public static String VideoCutBegin(String videoPath, long time){
        return  VideoCutter.VideoCutBegin(videoPath, time);
    }

    /**
     *
     * @param videoPath     Videopfad
     * @param time          Zeitpunkt des schneidens in ms
     * @param videoOutput   Videopfad für die zweite Häfte aus Ausgabe
     */
    public static void VideoCutBegin(String videoPath, long time, String videoOutput){
        VideoCutter.VideoCutBegin(videoPath, time,videoOutput);
    }




}
