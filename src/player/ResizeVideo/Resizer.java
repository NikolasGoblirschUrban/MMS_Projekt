package ResizeVideo;

import com.xuggle.mediatool.MediaToolAdapter;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.mediatool.event.VideoPictureEvent;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.IVideoResampler;

public class Resizer extends MediaToolAdapter {
    private Integer width;
    private Integer height;
    private IVideoResampler videoResampler = null;

    public Resizer(int aWidth, int aHeight) {
        this.width = aWidth;
        this.height = aHeight;
    }

    @Override
    public void onVideoPicture(IVideoPictureEvent event) {
        IVideoPicture pic = event.getPicture();
        if (videoResampler == null) {
            videoResampler = IVideoResampler.make(width, height, pic.getPixelType(), pic.getWidth(), pic
                    .getHeight(), pic.getPixelType());
        }
        IVideoPicture out = IVideoPicture.make(pic.getPixelType(), width, height);
        videoResampler.resample(out, pic);

        IVideoPictureEvent asc = new VideoPictureEvent(event.getSource(), out, event.getStreamIndex());
        super.onVideoPicture(asc);
        out.delete();
    }
}


