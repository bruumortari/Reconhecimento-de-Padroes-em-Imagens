import org.opencv.core.CvType;
import org.opencv.core.Mat;
import ij.process.ImageProcessor;

public class ImageProcessorToMatConverter {
    public Mat convert(ImageProcessor ip) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        byte[] pixels = (byte[]) ip.getPixels();
        Mat mat = new Mat(height, width, CvType.CV_8UC1);
        mat.put(0, 0, pixels);
        return mat;
    }
}
