package app;

import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

public class WebcamCapture {
    // a timer for acquiring the video stream
    private ScheduledExecutorService timer;
    // the OpenCV object that realizes the video capture
    private VideoCapture capture = new VideoCapture();
    // a flag to change the button behavior
    private boolean cameraActive = false;
    // the id of the camera to be used
    private static int cameraId = 0;

    public WebcamCapture() {
        System.out.println("Loaded ImageProcessor");
    }

    public void startCapture() {
        System.out.println("Start Video Capture");
        if (!this.cameraActive) {
            // start the video capture
            this.capture.open(cameraId);

            // is the video stream available?
            if (this.capture.isOpened()) {
                this.cameraActive = true;

                // grab a frame every 33 ms (30 frames/sec)
                Runnable frameGrabber = new Runnable() {

                    @Override
                    public void run() {
                        // effectively grab and process a single frame
                        Mat frame = grabFrame();
                        Mat frameProcessed = grabFrame();

                        // Process image
                        frameProcessed = ImageProcessor.processImage(frameProcessed);

                        // convert and show the frame
                        try {
                            BufferedImage imageOut = Utils.Mat2BufferedImage(frame);
                            BufferedImage imageProcessedOut = Utils.Mat2BufferedImage(frameProcessed);

                            MainWindow.showImage(imageOut, imageProcessedOut);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };

                timer = Executors.newSingleThreadScheduledExecutor();
                timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

            } else {
                // log the error
                System.err.println("Impossible to open the camera connection...");
            }
        }
    }

    public void stopCapture() {
        System.out.println("Stop Video Capture");
        // the camera is not active at this point
        this.cameraActive = false;

        // stop the timer
        this.stopAcquisition();
    }

    // Get a frame from the opened video stream (if any)
    private Mat grabFrame() {
        // init everything
        Mat frame = new Mat();

        // check if the capture is open
        if (this.capture.isOpened()) {
            try {
                // read the current frame
                this.capture.read(frame);
            } catch (Exception e) {
                // log the error
                System.err.println("Exception during the image elaboration: " + e);
            }
        }

        return frame;
    }

    private void stopAcquisition() {
        if (this.timer != null && !this.timer.isShutdown()) {
            try {
                // stop the timer
                timer.shutdown();
                timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // log any exception
                System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
            }
        }

        if (this.capture.isOpened()) {
            // release the camera
            this.capture.release();
        }
    }
}