package player.model;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.*;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;

public class AudioEditor {

    public File deleteAudio(File videofile){
        IContainer videoContainer = IContainer.make();
        if(videoContainer.open(videofile.getPath(), IContainer.Type.READ, null) < 0){
            throw new IllegalArgumentException("Cant find " + videofile);
        }
        int numStreamVideo = videoContainer.getNumStreams();

        System.out.println("Number of video streams: "+numStreamVideo );

        int videostreamID = -1; //this is the video stream id
        int audiostreamt = -1;

        IStreamCoder videocoder = null;

        IMediaWriter mWriter = ToolFactory.makeWriter("E:\\Dokumente\\OneDrive\\JKU\\test.mp4");

        for(int i=0; i<numStreamVideo; i++) {
            IStream stream = videoContainer.getStream(i);
            IStreamCoder code = stream.getStreamCoder();

            if (code.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
                videostreamID = i;
                videocoder = code;
            }
        }
        IPacket packetvideo = IPacket.make();
        mWriter.addVideoStream(1, 1, videocoder.getWidth(), videocoder.getHeight());

        while(videoContainer.readNextPacket(packetvideo) >= 0) {

            if (packetvideo.getStreamIndex() == videostreamID) {

                //video packet
                IVideoPicture picture = IVideoPicture.make(videocoder.getPixelType(),
                        videocoder.getWidth(),
                        videocoder.getHeight());
                int offset = 0;
                while (offset < packetvideo.getSize()) {
                    int bytesDecoded = videocoder.decodeVideo(picture,
                            packetvideo,
                            offset);
                    if (bytesDecoded < 0) throw new RuntimeException("bytesDecoded not working");
                    offset += bytesDecoded;

                    if (picture.isComplete()) {
                        System.out.println(picture.getPixelType());
                        mWriter.encodeVideo(1, picture);

                    }
                }
            }
        }
        mWriter.close();
        File newFile = new File("E:\\Dokumente\\OneDrive\\JKU\\test.mp4");
        return newFile;
    }

    public boolean editAudio(File videofile, File audiofile, Timestamp begin, Timestamp end){

        return true;
    }
}
