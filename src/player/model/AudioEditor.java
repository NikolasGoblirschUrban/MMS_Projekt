package player.model;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.*;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;

public class AudioEditor {

    public String deleteAudio(File videofile, String saveFile){
        IContainer videoContainer = IContainer.make();
        if(videoContainer.open(videofile.getPath(), IContainer.Type.READ, null) < 0){
            throw new IllegalArgumentException("Cant find " + videofile);
        }
        int numStreamVideo = videoContainer.getNumStreams();

        System.out.println("Number of video streams: "+numStreamVideo );

        int videostreamID = -1; //this is the video stream id

        IStreamCoder videocoder = null;

        IMediaWriter mWriter = ToolFactory.makeWriter(saveFile);

        for(int i=0; i<numStreamVideo; i++) {
            IStream stream = videoContainer.getStream(i);
            IStreamCoder code = stream.getStreamCoder();

            if (code.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
                videostreamID = i;
                videocoder = code;
            }
        }
        if (videocoder.open(null, null) < 0)
            throw new RuntimeException("Cant open video coder");

        IPacket packetvideo = IPacket.make();
        mWriter.addVideoStream(0, 0, videocoder.getWidth(), videocoder.getHeight());

        while(videoContainer.readNextPacket(packetvideo) >= 0) {

            if (packetvideo.getStreamIndex() == videostreamID) {

                //video packet
                IVideoPicture picture = IVideoPicture.make(videocoder.getPixelType(),
                        videocoder.getWidth(),
                        videocoder.getHeight());
                picture.getPts();
                int offset = 0;
                while (offset < packetvideo.getSize()) {
                    int bytesDecoded = videocoder.decodeVideo(picture,
                            packetvideo,
                            offset);
                    if (bytesDecoded < 0) throw new RuntimeException("bytesDecoded not working");
                    offset += bytesDecoded;

                    if (picture.isComplete()) {
                        System.out.println(picture.getPixelType());
                        mWriter.encodeVideo(0, picture);

                    }
                }
            }
        }
        mWriter.close();
        videocoder.close();
        videoContainer.close();
        System.out.println("Finish");
        return saveFile;
    }

    public String editAudio(File videofile, File audiofile, int begin, int end){

        IContainer videoContainer = IContainer.make();
        if(videoContainer.open(videofile.getPath(), IContainer.Type.READ, null) < 0){
            throw new IllegalArgumentException("Cant find " + videofile);
        }

        IContainer audioContainer = IContainer.make();
        if (audioContainer.open(audiofile.getPath(), IContainer.Type.READ, null) < 0)
            throw new IllegalArgumentException("Cant find " + audiofile);

        int numStreamVideo = videoContainer.getNumStreams();
        int numStreamAudio = audioContainer.getNumStreams();

        System.out.println("Number of video streams: "+numStreamVideo );

        int videostreamID = -1; //this is the video stream id
        int videoaudiostreamID = -1;
        int audiostreamID = -1;

        IStreamCoder videocoder = null;
        IStreamCoder videoAudioCoder = null;
        IStreamCoder audioCoder= null;

        IMediaWriter mWriter = ToolFactory.makeWriter("E:\\Dokumente\\OneDrive\\JKU\\test.mp4");

        for(int i=0; i<numStreamVideo; i++) {
            IStream stream = videoContainer.getStream(i);
            IStreamCoder code = stream.getStreamCoder();

            if (code.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
                videostreamID = i;
                videocoder = code;
            }

            if(code.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO){
                videoaudiostreamID = i;
                videoAudioCoder = code;
            }
        }

        for(int i = 0; i<numStreamAudio; i++){
            IStream stream = audioContainer.getStream(i);
            IStreamCoder code = stream.getStreamCoder();

            if(code.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO){
                audiostreamID = i;
                audioCoder = code;
            }
        }

        if (videocoder.open(null, null) < 0)
            throw new RuntimeException("Cant open video coder");

        if(videoAudioCoder.open(null, null) < 0)
            throw new RuntimeException("Cant open video audio coder");

        if(audioCoder.open(null, null) < 0){
            throw new RuntimeException("Cant open audio codre");
        }

        if(videoAudioCoder.getChannels() != audioCoder.getChannels() && videoAudioCoder.getSampleRate() != audioCoder.getSampleRate()){
            throw new RuntimeException("Channel and SampelRate not even");
        }
        IPacket packetvideo = IPacket.make();
        IPacket packetaudio = IPacket.make();
        mWriter.addVideoStream(0, 0, videocoder.getWidth(), videocoder.getHeight());
        mWriter.addAudioStream(1,1,videoAudioCoder.getChannels(), videoAudioCoder.getSampleRate());

        long picturepts = 0;

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
                        picturepts = picture.getPts();
                        System.out.println(picture.getPixelType());
                        mWriter.encodeVideo(0, picture);

                    }
                }
            }
            if(packetvideo.getStreamIndex() == videoaudiostreamID){
                IAudioSamples samples = IAudioSamples.make(512,
                        videoAudioCoder.getChannels(),
                        IAudioSamples.Format.FMT_S32);
                int offset = 0;
                while(offset<packetvideo.getSize()) {
                    int bytesDecodedaudio = videoAudioCoder.decodeAudio(samples,
                            packetvideo,
                            offset);
                    if (bytesDecodedaudio < 0)
                        throw new RuntimeException("could not detect audio");
                    offset += bytesDecodedaudio;

                    if (samples.isComplete()) {
                        if(samples.getPts() < begin*1000000 && samples.getPts() > end*1000000) {
                            mWriter.encodeAudio(1, samples);
                        }
                        else {
                            if(audioContainer.readNextPacket(packetaudio) >= 0) {
                                if (packetaudio.getStreamIndex() == audiostreamID) {
                                    //audio packet

                                    IAudioSamples audioSamples = IAudioSamples.make(512,
                                            audioCoder.getChannels(),
                                            IAudioSamples.Format.FMT_S32);
                                    int audiooffset = 0;
                                    while (audiooffset < packetaudio.getSize()) {
                                        int audiobytesDecodedaudio = audioCoder.decodeAudio(audioSamples,
                                                packetaudio,
                                                offset);
                                        if (audiobytesDecodedaudio < 0)
                                            throw new RuntimeException("could not detect audio");
                                        audiooffset += audiobytesDecodedaudio;

                                        if (audioSamples.isComplete()) {
                                            audioSamples.setPts(picturepts);
                                            mWriter.encodeAudio(1, audioSamples);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        mWriter.close();
        videoAudioCoder.close();
        videocoder.close();
        videoContainer.close();
        audioCoder.close();
        audioContainer.close();
        System.out.println("Finish");
        return "E:\\Dokumente\\OneDrive\\JKU\\test.mp4";
    }
}
