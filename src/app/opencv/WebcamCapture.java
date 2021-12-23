package app.opencv;

import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import app.Utils;
import app.gui.UserInterface;

/**
 * Zuständig für das Aufzeichnen der Webcam.
 * 
 * Der Code für diese Klasse basiert auf dem Beispiel von Luigi De Russis und Alberto Sacco:
 * https://opencv-java-tutorials.readthedocs.io/en/latest/03-first-javafx-application-with-opencv.html#video-capturing
 */
public class WebcamCapture {
	// Timer fürs Erhalten des Video Streams
	private ScheduledExecutorService timer;
	// OpenCV Objekt, zuständig für die Aufnahme
	private VideoCapture capture;
	// Gibt an ob die Aufnahme an oder aus ist
	private boolean cameraActive = false;
	// Die ID der Kamera die benutzt wird
	private static int cameraId = 0;

	private UserInterface userInterface;
	private SceneSelect sceneSelect;

	/**
	 * Konstruktor intialisiert die Instanzvariablen und legt die Breite und Höhe fest.
	 * @param userInterface	Referenz zum UserInterface
	 * @param width			Breite der Ausgabe
	 * @param height		Höhe der Ausgabe
	 */
	public WebcamCapture(UserInterface userInterface, int width, int height) {
		this.userInterface = userInterface;
		this.sceneSelect = sceneSelect;

		capture = new VideoCapture();
		capture.set(Videoio.CAP_PROP_FRAME_WIDTH, width);
		capture.set(Videoio.CAP_PROP_FRAME_HEIGHT, height);
	}

	/**
	 * Startet oder stoppt die Aufnahme und setzt entsprechend den Text vom Button
	 * im User Interface.
	 */
	public void toggleCapture() {
		if (!cameraActive) {
			startCapture();
		} else {
			stopCapture();
		}

		userInterface.setWebcamText(getLabel());
	}

	/**
	 * Gibt den Text für den JPanel Component zurück der anzeigt ob die Aufnahme aktiv ist
	 * oder nicht.
	 * @return	Text
	 */
	public String getLabel() {
		return cameraActive ? "Aufnahme stoppen" : "Aufnahme beginnen";
	}

	/**
	 * Startet die Webcam Aufnahme.
	 */
	public void startCapture() {
		System.out.println("Start Video Capture");
		// Startet die Aufnahme
		this.capture.open(cameraId);

		// Testet ob der Video Stream verfügbar ist
		if (this.capture.isOpened()) {
			this.cameraActive = true;

			// Speichert den frame alle 33ms (30 FPS)
			Runnable frameGrabber = new Runnable() {
				@Override
				public void run() {
					// Speichert im Video Stream zu diesem Zeitpunkt
					Mat frame = grabFrame();

					// Wendet die Bildverarbeitung auf dem Frame an
					Mat frameProcessed = ImageProcessor.processImage(userInterface, frame);

					// Konvertiert den Frame in ein BufferedImage und übergibt ihn an das User Interface
					try {
						BufferedImage imageProcessed = Utils.Mat2BufferedImage(frameProcessed);
						BufferedImage imageBefore = Utils.Mat2BufferedImage(frame);

						userInterface.setCurrentFrame(imageProcessed, imageBefore);
						userInterface.setWebcamIcon(imageProcessed);
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

	/**
	 * Stoppt die Webcam aufnahme.
	 */
	public void stopCapture() {
		System.out.println("Stop Video Capture");
		this.cameraActive = false;

		// Stoppt den Timer
		this.stopAcquisition();
	}

	/**
	 * Speichert den Frame aus dem Webcam Stream.
	 * @return	Frame
	 */
	private Mat grabFrame() {
		Mat frame = new Mat();

		// Überprüft ob der Video Stream aktiv ist
		if (this.capture.isOpened()) {
			try {
				// Liest den Frame aus
				this.capture.read(frame);
			} catch (Exception e) {
				System.err.println("Exception during the image elaboration: " + e);
			}
		}

		return frame;
	}

	/**
	 * Stoppt und terminiert den laufenden Timer.
	 */
	private void stopAcquisition() {
		// Wenn der Timer existiert und aktiv ist wird er beendet
		if (this.timer != null && !this.timer.isShutdown()) {
			try {
				timer.shutdown();
				timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
			}
		}

		if (this.capture.isOpened())
			this.capture.release();
	}
}
