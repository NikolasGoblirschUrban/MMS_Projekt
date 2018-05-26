package VideoTool;

/**
 *  nur HIGH UND WIDTH in einer Klasse
 */
public class VideoSize {
    private int high;
    private int width;

    public void setHigh(int high) {
        this.high = high;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHigh() {
        return high;
    }

    public int getWidth() {
        return width;
    }

    public VideoSize(int high, int width){
        this.high = high;
        this.width = width;
    }
    public VideoSize(VideoSize size){
        this.high = size.getHigh();
        this.width = size.getWidth();
    }

    public VideoSize(){
    }

    public boolean Compair(VideoSize compSize){
        if(compSize.getHigh() == this.getHigh() && compSize.getWidth() == this.getWidth()){
            return true;
        }
        return false;
    }
}
