package player.videoTool;

import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;


//https://www.programcreek.com/java-api-examples/?code=destiny1020/java-learning-notes-cn/java-learning-notes-cn-master/Image%20&%20Video/video/XuggleTest.java#

public class VideoData {
    private String fileName = "";
    private int numStreams = 0;
    private long duration = 0;
    private long fileSize = 0;
    private long bitRate = 0;
    private IStream StreamInfoVideo;
    private IStreamCoder  CodecInfoVideo;
    private IStream StreamInfoAudio;
    private IStreamCoder  CodecInfoAudio;

    /**
     * Videodatein der Streams werden ausgelesen und abgespeichert
     * @param VideoFileName Videopfad
     */

    public VideoData(String VideoFileName){
        this.fileName = VideoFileName;
        // first we create a Xuggler container object
        IContainer container = IContainer.make();
        // we attempt to open up the container
        int result = container.open(fileName, IContainer.Type.READ, null);
        // check if the operation was successful
        if (result < 0) {
            throw new RuntimeException("Failed to open media file");
        }
        // query how many streams the call to open found
        this.numStreams = container.getNumStreams();
        // query for the total duration
        this.duration = container.getDuration();
        // query for the file size
        this.fileSize = container.getFileSize();
        // query for the bit rate
        this.bitRate = container.getBitRate();
        // iterate through the streams to print their meta data

        for (int i = 0; i < numStreams; i++) {
            // find the stream object
            setStreams(container.getStream(i));
        }
    }

    /**
     * Audiostream und Videostream
     * @param stream
     */

    private void setStreams (IStream stream){
        IStreamCoder coder = stream.getStreamCoder();
        if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO) {
            this.StreamInfoAudio = stream;
            this.CodecInfoAudio = coder;
        } else if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
            this.StreamInfoVideo = stream;
            this.CodecInfoVideo = coder;
        }
    }
    // Getter
    public int getNumStreams() {
        return numStreams;
    }

    public long getDuration() {
        return duration;
    }

    public long getFileSize() {
        return fileSize;
    }

    public long getBitRate() {
        return bitRate;
    }

    public IStream getStreamInfoVideo() {
        return StreamInfoVideo;
    }

    public IStreamCoder getCodecInfoVideo() {
        return CodecInfoVideo;
    }

    public IStream getStreamInfoAudio() {
        return StreamInfoAudio;
    }

    public IStreamCoder getCodecInfoAudio() {
        return CodecInfoAudio;
    }

    public String getFileName() {

        return fileName;
    }

}
