package app.opencv;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

public class ImageProcessor {

	public static Mat processImage(Mat frame) {
		// Stop when frame is empty
		if (frame.empty())
			return null;

		Mat frameHSV = new Mat();
		// Wandelt das RGB Bild in HSV um
		Imgproc.cvtColor(frame, frameHSV, Imgproc.COLOR_BGR2HSV);
		// Speichert HSV werte in separaten variablen in einer Liste
		List<Mat> hsv = new ArrayList<>(3);
		Core.split(frameHSV, hsv);

		// Speichert die hsv-Werte in einzelnen Matrizen
		Mat frameValue = hsv.get(2);

		Mat frameBlur = new Mat();
		Mat frameValueBlur = new Mat();
		int blurStrength = 15;
		// Weichzeichnung des original Bildes und vom Value Bild
		Imgproc.GaussianBlur(frame, frameBlur, new Size(blurStrength, blurStrength), 0);
		Imgproc.medianBlur(frameValue, frameValueBlur, blurStrength);

		// Threshold in binäres Bild mit der OTSU-Methode
		Mat frameBinary = new Mat();
		int threshold = 0;
		Imgproc.threshold(frameValueBlur, frameBinary, threshold, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

		// Alle Variablen von ConnectedComponentsWithStats separat speichern
		Mat frameLabels = new Mat();
		Mat stats = new Mat();
		Mat centroids = new Mat();

		// Sucht zusammengehörige Regionen
		int labelCount = Imgproc.connectedComponentsWithStats(frameBinary, frameLabels, stats, centroids, 4);

		// Filtern nach Komponenten in der Mitte des Bildes
		int width = frame.width();
		int height = frame.height();
		double tolerance = 0.5; // Erlaubte Abweichung von der Mitte des Bildes
		ArrayList<Integer> selectedLabels = new ArrayList<>();

		// Neues Bild mit ausschließlich ausgewählten labels
		Mat frameCenterLabels = new Mat(frameBinary.rows(), frameBinary.cols(), frameBinary.type(), Scalar.all(0));

		// Fange bei eins an, damit der Hintergrund ignoriert wird
		for (int i = 1; i < labelCount; i++) {
			int centroidX = (int) centroids.get(i, 0)[0];
			int centroidY = (int) centroids.get(i, 1)[0];
			//Point center = new Point(centroidX, centroidY);

			// Füge alle labels hinzu, die sich in der Mitte des Bildes befinden
			if (centroidX >= (width / 2) - tolerance * (width / 2)
					&& centroidX <= (width / 2) + tolerance * (width / 2)
					&& centroidY >= (height / 2) - tolerance * (height / 2)
					&& centroidY <= (height / 2) + tolerance * (height / 2)) {
				selectedLabels.add(i);
				// Zeige einen Punkt an, wenn das label sich in der Mitte befindnet
				/*
				Imgproc.circle(frameCenterLabels, center, 3, new Scalar(0, 0, 0));
				Imgproc.circle(frameCenterLabels, center, 4, new Scalar(255, 255, 255));
				*/
			}
		}

		// Füllt alle Regionen weiß die außerhalb der Mitte sind
		fillRegion(frameLabels, frameCenterLabels, selectedLabels);

		return frameCenterLabels;
	}

	public static void fillRegion(Mat src, Mat dst, ArrayList<Integer> selectedLabel) {
		for (int r = 0; r < dst.rows(); r++) {
			for (int c = 0; c < dst.cols(); c++) {
				if (!selectedLabel.contains((int) src.get(r, c)[0])) {
					dst.put(r, c, 255);
				}
			}
		}
	}
}