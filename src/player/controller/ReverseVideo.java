package player.controller;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IAddStreamEvent;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.*;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

//This class reverses the audio and video feed of an .mp4 file
//The first part of this class is pretty much the same as CaptureFrames, but that class wasnt very inheritance friendly, so there is some duplicate code

public class ReverseVideo extends MediaListenerAdapter {

    //around 29 FPS
    private static final double SECONDS_BETWEEN_FRAMES = 0.035;


    private static final long MICRO_SECONDS_BETWEEN_FRAMES =
            (long)(Global.DEFAULT_PTS_PER_SECOND * SECONDS_BETWEEN_FRAMES);

    private static long mLastPtsWrite = Global.NO_PTS;

    private int mVideoStreamIndex = -1;

    private File inFile;

    private File outDirectory;

    private int frameCount;

    private boolean isDone;

    //default values for width and height
    private int width = 1920;
    private int height = 1080;
    private int duration = 0;


    public ReverseVideo(File inFile, File outDirectory, int duration) {
        this.inFile = inFile;
        this.outDirectory = outDirectory;
        this.duration  = duration;
        frameCount = 0;
        isDone = false;
    }


    public void start(){
        IMediaReader reader = ToolFactory.makeReader(inFile.getPath());
        reader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
        reader.addListener(this);

        while (reader.readPacket() == null)
            do {} while(false);
        final IMediaWriter writer = ToolFactory.makeWriter(outDirectory.getAbsolutePath() + "/video.mp4");

        //use the frames to create a backwards video
        writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4, IRational.make(1/SECONDS_BETWEEN_FRAMES), width, height);
        int sec = 0;
        System.out.println("Creating Video...");

        try {
            //-10 is a bandaid fix because the splitting of the frames is not exact
            for (int i = (int)(duration/SECONDS_BETWEEN_FRAMES)-10; i > 0; i--) {
                BufferedImage image = ImageIO.read(new File(outDirectory.getAbsolutePath() + "/frame" + i + ".png"));
                writer.encodeVideo(0, image, sec, TimeUnit.MILLISECONDS);
                sec += 1000 * SECONDS_BETWEEN_FRAMES;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }



        writer.flush();
        writer.close();

        System.out.println("Creating Audio...");
        //extract audio and reverse it
        File audio = new File(outDirectory.getAbsolutePath()+"/audio.wav");
        File reverse = new File(outDirectory.getAbsolutePath()+"/reverse.wav");

        extractAudio(audio);
        createReverseAudio(audio, reverse);

        System.out.println("Merging Video and Audio...");
        //merge audio and video
        mergeAudioAndVideo(reverse, new File(outDirectory.getAbsolutePath() + "/video.mp4"));

        isDone = true;

    }

    //reverse audio file
    private void createReverseAudio(File inFile, File outFile) {

        try {
            AudioInputStream in = AudioSystem.getAudioInputStream(new File(inFile.getAbsolutePath()));

            AudioFormat audioFormat = in.getFormat();

            FrameBuffer frameStream = new FrameBuffer(in);
            int frame = frameStream.numberFrames() - 1;
            byte[] ba = new byte[frameStream.numberFrames()];
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            //write sound data in reverse order into the outputStream
            while (frame >= 0) {
                out.write(frameStream.getFrame(frame), 0, frameStream.frameSize());
                frame--;
            }
            //turn outputStream into inputStream to be able to write it into a file

            ByteArrayInputStream bis = new ByteArrayInputStream(out.toByteArray());

            AudioInputStream in2 = new AudioInputStream(bis, audioFormat, ba.length);

            AudioSystem.write(in2, AudioFileFormat.Type.WAVE, outFile);
            in2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // extract audio from video
    private void extractAudio (File out) {
        IMediaWriter writer = ToolFactory.makeWriter(out.getPath());

        String filename = inFile.getPath();

        IContainer container = IContainer.make();
        if (container.open(filename, IContainer.Type.READ, null) < 0)
            throw new IllegalArgumentException("could not open file: " + filename);
        int numStreams = container.getNumStreams();
        int audioStreamId = -1;
        IStreamCoder audioCoder = null;
        for(int i = 0; i < numStreams; i++)
        {
            IStream stream = container.getStream(i);
            IStreamCoder coder = stream.getStreamCoder();
            if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO)
            {
                audioStreamId = i;
                audioCoder = coder;
                break;
            }
        }
        if (audioStreamId == -1)
            throw new RuntimeException("Cant open audio stream in container: "+filename);
        if (audioCoder.open() < 0)
            throw new RuntimeException("Cant open audio decoder for container: "+filename);

        writer.addAudioStream(0, 0, audioCoder.getChannels(), audioCoder.getSampleRate());
        IPacket packet = IPacket.make();
        while(container.readNextPacket(packet) >= 0)
        {
            if (packet.getStreamIndex() == audioStreamId)
            {
                IAudioSamples samples = IAudioSamples.make(1024, audioCoder.getChannels());
                int offset = 0;
                while(offset < packet.getSize())
                {
                    int bytesDecoded = audioCoder.decodeAudio(samples, packet, offset);
                    if (bytesDecoded < 0)
                        throw new RuntimeException("Couldn't decod audio " + filename);
                    offset += bytesDecoded;
                    if (samples.isComplete())
                    {
                        writer.encodeAudio(0, samples);                   }
                }
            }
            else
            {
                do {} while(false);
            }
        }


        if (audioCoder != null)
        {
            audioCoder.close();
            audioCoder = null;
        }
        if (container !=null)
        {
            container.close();
            container = null;
        }
        writer.flush();
        writer.close();
    }


    private void mergeAudioAndVideo(File audio, File video){
        IMediaWriter mWriter = ToolFactory.makeWriter(outDirectory.getAbsolutePath() + "/reverse.mp4");
        //create video and audio Containers
        IContainer containerVideo = IContainer.make();
        IContainer containerAudio = IContainer.make();

        if (containerVideo.open(video.getPath(), IContainer.Type.READ, null) < 0)
            throw new IllegalArgumentException(video.getPath());
        if (containerAudio.open(audio.getPath(), IContainer.Type.READ, null) < 0)
            throw new IllegalArgumentException("Cant find " + audio.getPath());

        //creat video and audio Coders
        IStreamCoder coderVideo = containerVideo.getStream(0).getStreamCoder();
        if (coderVideo.open(null, null) < 0)
            throw new RuntimeException("Cant open video coder");
        IPacket packetvideo = IPacket.make();
        int width = coderVideo.getWidth();
        int height = coderVideo.getHeight();

        IStreamCoder coderAudio = containerAudio.getStream(0).getStreamCoder();
        if (coderAudio.open(null, null) < 0)
            throw new RuntimeException("Cant open audio coder");
        IPacket packetaudio = IPacket.make();

        //write both streams into same file
        mWriter.addAudioStream(1, 0, coderAudio.getChannels(), coderAudio.getSampleRate());
        mWriter.addVideoStream(0, 0, width, height);

        //fill up both streams
        while (containerVideo.readNextPacket(packetvideo) >= 0) {

            containerAudio.readNextPacket(packetaudio);

            IVideoPicture picture = IVideoPicture.make(coderVideo.getPixelType(), width, height);
            coderVideo.decodeVideo(picture, packetvideo, 0);
            if (picture.isComplete())
                mWriter.encodeVideo(0, picture);

            IAudioSamples samples = IAudioSamples.make(512, coderAudio.getChannels(), IAudioSamples.Format.FMT_S32);
            coderAudio.decodeAudio(samples, packetaudio, 0);
            if (samples.isComplete())
                mWriter.encodeAudio(1, samples);

        }


        //fill up remaining audiostream because of different runtimes
        IAudioSamples samples;
        do {
            samples = IAudioSamples.make(512, coderAudio.getChannels(), IAudioSamples.Format.FMT_S32);
            containerAudio.readNextPacket(packetaudio);
            coderAudio.decodeAudio(samples, packetaudio, 0);
            mWriter.encodeAudio(1, samples);
        }while (samples.isComplete() );



        coderAudio.close();
        coderVideo.close();
        containerAudio.close();
        containerVideo.close();
        mWriter.close();
    }

    //get width and height of video
    @Override
    public void onAddStream(IAddStreamEvent event) {
        int streamIndex = event.getStreamIndex();
        IStreamCoder streamCoder = event.getSource().getContainer().getStream(streamIndex).getStreamCoder();
        if (streamCoder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
            width = streamCoder.getWidth();
            height = streamCoder.getHeight();
        }
        super.onAddStream(event);
    }
//extract Frames (copied from CaptureFrames)
    @Override
    public void onVideoPicture(IVideoPictureEvent event) {
        try {
            if (event.getStreamIndex() != mVideoStreamIndex) {
                if (-1 == mVideoStreamIndex)
                    mVideoStreamIndex = event.getStreamIndex();

                else
                    return;
            }

            if (mLastPtsWrite == Global.NO_PTS)
                mLastPtsWrite = event.getTimeStamp() - MICRO_SECONDS_BETWEEN_FRAMES;

            if (event.getTimeStamp() - mLastPtsWrite >= MICRO_SECONDS_BETWEEN_FRAMES) {

                File file = new File(outDirectory.getAbsolutePath() +
                        "/frame" + frameCount + ".png");

                frameCount++;

                ImageIO.write(event.getImage(), "png", file);

                double seconds = ((double) event.getTimeStamp())
                        / Global.DEFAULT_PTS_PER_SECOND;
                System.out.printf("at elapsed time of %6.3f seconds wrote: %s\n",
                        seconds, file);

                mLastPtsWrite += MICRO_SECONDS_BETWEEN_FRAMES;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean getIsDone(){
        return this.isDone;
    }
}

