package player.videoTool;
import com.xuggle.mediatool.MediaToolAdapter;
import com.xuggle.mediatool.event.*;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.IVideoPicture;

public class MediaConcatenator extends MediaToolAdapter
{
    private long mOffset = 0;
    private long mNextVideo = 0;
    private long mNextAudio = 0;
    private final int mAudoStreamIndex;
    private final int mVideoStreamIndex;
    public long currentTimestamp = 0L;


    private boolean toggleChangeTimestamp = false;
    private long changeTimestamp = 0; // will be added: NewTime = OldTime + changeTimestamp

    public MediaConcatenator(int audioStreamIndex, int videoStreamIndex)
    {
        mAudoStreamIndex = audioStreamIndex;
        mVideoStreamIndex = videoStreamIndex;
    }



    @Override
    public void onAudioSamples(IAudioSamplesEvent event)
    {
        currentTimestamp = event.getTimeStamp();
        IAudioSamples samples = event.getAudioSamples();


        long originalTimeStamp = samples.getTimeStamp();
        long newTimeStamp = originalTimeStamp + mOffset;

        mNextAudio = samples.getNextPts();
        if(toggleChangeTimestamp) {
            newTimeStamp = newTimeStamp + changeTimestamp;
            //System.out.println("Audio:" + originalTimeStamp + " = " + newTimeStamp + " " + changeTimestamp);
        }

        samples.setTimeStamp(newTimeStamp);

        super.onAudioSamples(new AudioSamplesEvent(this, samples,
                mAudoStreamIndex));
    }
    @Override
    public void onVideoPicture(IVideoPictureEvent event)
    {
        currentTimestamp = event.getTimeStamp();
        IVideoPicture picture = event.getMediaData();
        long originalTimeStamp = picture.getTimeStamp();

        long newTimeStamp = originalTimeStamp + mOffset;

        mNextVideo = originalTimeStamp + 1;
        if(toggleChangeTimestamp) {
            newTimeStamp = originalTimeStamp + changeTimestamp;
            //System.out.println("Video:" +newTimeStamp + " = " + originalTimeStamp + " " + changeTimestamp);
        }

        picture.setTimeStamp(newTimeStamp);

        super.onVideoPicture(new VideoPictureEvent(this, picture,
                mVideoStreamIndex));
    }

    public void onClose(ICloseEvent event)
    {
        // update the offset by the larger of the next expected audio or video
        // frame time

        mOffset = Math.max(mNextVideo, mNextAudio);

        if (mNextAudio < mNextVideo)
        {
        }
    }

    public void onAddStream(IAddStreamEvent event)
    {
        // overridden to ensure that add stream events are not passed down
        // the tool chain to the writer, which could cause problems
    }

    public void onOpen(IOpenEvent event)
    {
        // overridden to ensure that open events are not passed down the tool
        // chain to the writer, which could cause problems
    }

    public void onOpenCoder(IOpenCoderEvent event)
    {
        // overridden to ensure that open coder events are not passed down the
        // tool chain to the writer, which could cause problems
    }

    public void onCloseCoder(ICloseCoderEvent event)
    {
        // overridden to ensure that close coder events are not passed down the
        // tool chain to the writer, which could cause problems
    }

    public void setToggleChangeTimestamp(boolean toggleChangeTimestamp) {
        this.toggleChangeTimestamp = toggleChangeTimestamp;
    }

    public void setChangeTimestamp(long changeTimestamp) {
        this.changeTimestamp = changeTimestamp;
    }

    public boolean isToggleChangeTimestamp() {

        return toggleChangeTimestamp;
    }

    public long getChangeTimestamp() {
        return changeTimestamp;
    }


}

