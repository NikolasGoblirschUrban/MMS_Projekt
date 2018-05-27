package player.videoResize;
import com.xuggle.mediatool.MediaToolAdapter;
import com.xuggle.mediatool.event.IAddStreamEvent;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IStreamCoder;

public class MyVideoListener extends MediaToolAdapter {
    private Integer width;
    private Integer height;

    public MyVideoListener(Integer aWidth, Integer aHeight) {
        this.width = aWidth;
        this.height = aHeight;
    }

    @Override
    public void onAddStream(IAddStreamEvent event) {
        int streamIndex = event.getStreamIndex();
        IStreamCoder streamCoder = event.getSource().getContainer().getStream(streamIndex).getStreamCoder();
        if (streamCoder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO) {
        } else if (streamCoder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
            streamCoder.setWidth(width);
            streamCoder.setHeight(height);
        }
        super.onAddStream(event);
    }

}