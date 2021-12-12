package app.opencv;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class ImageProcessor {

    public static Mat processImage(Mat frame) {
        // if the frame is not empty, process it
        if (!frame.empty()) {
            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2HLS);
        }

        return frame;
    }

}
