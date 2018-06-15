package player.model;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.*;
import player.videoTool.MediaConcatenator;
import player.videoTool.VideoCombine;
import player.videoTool.VideoCutter;
import player.videoTool.VideoData;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;


public class AudioEditor {

    //This method deletes the audio of a video file
    public String deleteAudio(File videofile, String saveFile){
        IContainer videoContainer = IContainer.make(); //make Container of video
        if(videoContainer.open(videofile.getPath(), IContainer.Type.READ, null) < 0){
            throw new IllegalArgumentException("Cant find " + videofile);
        }
        int numStreamVideo = videoContainer.getNumStreams();

        int videostreamID = -1;

        IStreamCoder videocoder = null;

        IMediaWriter mWriter = ToolFactory.makeWriter(saveFile);

        for(int i=0; i<numStreamVideo; i++) {
            IStream stream = videoContainer.getStream(i);
            IStreamCoder code = stream.getStreamCoder();

            if (code.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
                videostreamID = i; //Set VideoStreamID
                videocoder = code; //set IStreamCoder
            }
        }
        if (videocoder.open(null, null) < 0)
            throw new RuntimeException("Cant open video coder");

        IPacket packetvideo = IPacket.make();
        mWriter.addVideoStream(0, 0, videocoder.getWidth(), videocoder.getHeight()); //new Video with only videostream

        while(videoContainer.readNextPacket(packetvideo) >= 0) {

            if (packetvideo.getStreamIndex() == videostreamID) {

                //video packet
                IVideoPicture picture = IVideoPicture.make(videocoder.getPixelType(),
                        videocoder.getWidth(),
                        videocoder.getHeight()); //get every picture of video
                int offset = 0;
                while (offset < packetvideo.getSize()) {
                    int bytesDecoded = videocoder.decodeVideo(picture,
                            packetvideo,
                            offset);
                    if (bytesDecoded < 0) throw new RuntimeException("bytesDecoded not working");
                    offset += bytesDecoded;

                    if (picture.isComplete()) {
                        mWriter.encodeVideo(0, picture); // copy to the new video

                    }
                }
            }
        }
        mWriter.close();
        videocoder.close();
        videoContainer.close();
        return saveFile;
    }

    //the method add audio to video to specified begin and ends at a specified time
    //function not finished -> Some error occurs -> not enough time to debug
    public String editAudio(File videofile, File audiofile, int begin, int end, String destinationUrl){

       /* IContainer videoContainer = IContainer.make();
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

        IMediaWriter mWriter = ToolFactory.makeWriter("tempvideo.mp4");

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
                        System.out.println(samples.getPts());
                        if(samples.getPts() < (begin*1000000) || samples.getPts() > (end*1000000)) { //Idea was to compare the Presentation time stamp
                            // and copy the selected audio only its between the begin and end otherwise the usually audio will copy to the new video
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
        return "tempvideo.mp4";*/
        String part1 = VideoCutter.VideoCutBegin(videofile.getPath(), begin);
        String part2 = VideoCutter.VideoCut(videofile.getPath(), begin, end);
        String part3 = VideoCutter.VideoCutEnd(videofile.getPath(), end);

        VideoData videoDataPart2 = new VideoData(part2);
        IMediaReader readerVideo = ToolFactory.makeReader(part2);

        String audioPath = audiofile.getPath();
        IMediaReader readerAudio = ToolFactory.makeReader(audioPath);

        IContainer audioContainer = IContainer.make(); //make Container of Audio
        if(audioContainer.open(audiofile.getPath(), IContainer.Type.READ, null) < 0){
            throw new IllegalArgumentException("Cant find " + videofile);
        }

        MediaConcatenator concatenator = new MediaConcatenator(videoDataPart2.getStreamInfoAudio().getIndex(),
                videoDataPart2.getStreamInfoVideo().getIndex());

        concatenator.setToggleChangeTimestamp(false);
        readerVideo.addListener(concatenator);
        readerAudio.addListener(concatenator);


        IStream audioStream = audioContainer.getStream(0);
        IStreamCoder audioCoder = audioStream.getStreamCoder();

        IMediaWriter writer = ToolFactory.makeWriter(destinationUrl);
        concatenator.addListener(writer);

        writer.addVideoStream(videoDataPart2.getStreamInfoVideo().getIndex(), 0, videoDataPart2.getCodecInfoVideo().getWidth(), videoDataPart2.getCodecInfoVideo().getHeight());
        writer.addAudioStream(videoDataPart2.getStreamInfoAudio().getIndex(), 0, audioCoder.getChannels(),
                audioCoder.getSampleRate());

        // read packets from the first source file until done

        while (readerVideo.readPacket() == null)
            ;

        // read packets from the second source file until done

        while (readerAudio.readPacket() == null)
            ;

        // close the writer

        writer.close();

        VideoCombine.combineThreeEqualVideos(part1, destinationUrl, part3, destinationUrl);
        return destinationUrl;
    }
}
