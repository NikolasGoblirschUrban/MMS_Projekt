package player.controller;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.Global;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class CaptureFrames extends MediaListenerAdapter
{
    private static final double SECONDS_BETWEEN_FRAMES = 5;


    private static final long MICRO_SECONDS_BETWEEN_FRAMES =
            (long)(Global.DEFAULT_PTS_PER_SECOND * SECONDS_BETWEEN_FRAMES);

    private static long mLastPtsWrite = Global.NO_PTS;

    private int mVideoStreamIndex = -1;

    private File inFile;

    private File outDirectory;

    private int count;

    private boolean isDone;

    public CaptureFrames(File inFile, File outDirectory)
    {
        isDone = false;
        this.inFile = inFile;
        count = 0;
        this.outDirectory = outDirectory;
    }

    public void start() {
        IMediaReader reader = ToolFactory.makeReader(inFile.getPath());
        reader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
        reader.addListener(this);

        while (reader.readPacket() == null)
            do {} while(false);
        isDone = true;

    }

    public void onVideoPicture(IVideoPictureEvent event)
    {
        try
        {
            if (event.getStreamIndex() != mVideoStreamIndex)
            {
                if (-1 == mVideoStreamIndex)
                    mVideoStreamIndex = event.getStreamIndex();

                else
                    return;
            }

            if (mLastPtsWrite == Global.NO_PTS)
                mLastPtsWrite = event.getTimeStamp() - MICRO_SECONDS_BETWEEN_FRAMES;

            if (event.getTimeStamp() - mLastPtsWrite >= MICRO_SECONDS_BETWEEN_FRAMES)
            {

                File file = new File(outDirectory.getAbsolutePath() +
                        "/frame" + count + ".png");

                count++;

                ImageIO.write(event.getImage(), "png", file);

                double seconds = ((double)event.getTimeStamp())
                        / Global.DEFAULT_PTS_PER_SECOND;
                System.out.printf("at elapsed time of %6.3f seconds wrote: %s\n",
                        seconds, file);

                mLastPtsWrite += MICRO_SECONDS_BETWEEN_FRAMES;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public boolean getIsDone(){
        return this.isDone;
    }
}

