package app.opencv;

import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import app.gui.UserInterface;

public class WebcamCapture {
	// Timer for acquiring the video stream
	private ScheduledExecutorService timer;
	// The OpenCV object that realizes the video capture
	private VideoCapture capture;
	// Flag to change the button behavior
	private boolean cameraActive = false;
	// The id of the camera to be used
	private static int cameraId = 0;

	private UserInterface userInterface;

	public WebcamCapture(UserInterface userInterface, int width, int height) {
		this.userInterface = userInterface;

		capture = new VideoCapture();
		capture.set(Videoio.CAP_PROP_FRAME_WIDTH, width);
		capture.set(Videoio.CAP_PROP_FRAME_HEIGHT, height);
	}

	// Toggles the webcam capture on and off
	public void toggleCapture() {
		if (!cameraActive) {
			startCapture();
		} else {
			stopCapture();
		}

		userInterface.setWebcamText(getLabel());
	}

	// Get the label text for the JPanel component
	public String getLabel() {
		return cameraActive ? "Aufnahme stoppen" : "Aufnahme beginnen";
	}

	// Starts the webcam capture
	public void startCapture() {
		System.out.println("Start Video Capture");
		// Start the video capture
		this.capture.open(cameraId);

		// Test if the video stream is available
		if (this.capture.isOpened()) {
			this.cameraActive = true;

			// grab a frame every 33 ms (30 frames/sec)
			Runnable frameGrabber = new Runnable() {
				@Override
				public void run() {
					// Grab a single frame
					Mat frame = grabFrame();

					// Process Frame
					frame = ImageProcessor.processImage(frame);

					// Convert the frame to a bufferd image and display it
					try {
						BufferedImage image = Utils.Mat2BufferedImage(frame);
						userInterface.setCurrentFrame(image);
						userInterface.setWebcamIcon(image);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};

			timer = Executors.newSingleThreadScheduledExecutor();
			timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

		} else {
			System.err.println("Impossible to open the camera connection...");
		}
	}

	// Stops the webcam capture
	public void stopCapture() {
		System.out.println("Stop Video Capture");
		this.cameraActive = false;

		this.stopAcquisition(); // Stop the timer
	}

	// Get a frame from the opened video stream
	private Mat grabFrame() {
		Mat frame = new Mat();

		// Check if the capture is open
		if (this.capture.isOpened()) {
			try {
				this.capture.read(frame); // Read the current frame
			} catch (Exception e) {
				System.err.println("Exception during the image elaboration: " + e);
			}
		}

		return frame;
	}

	// Stops and terminates the timer
	private void stopAcquisition() {
		if (this.timer != null && !this.timer.isShutdown()) {
			try {
				timer.shutdown();
				timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
			}
		}

		if (this.capture.isOpened()) {
			this.capture.release(); // Release the camera
		}
	}
}
