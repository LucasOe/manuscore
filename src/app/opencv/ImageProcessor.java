package app.opencv;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.MatOfPoint;
import org.opencv.core.*;



import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class ImageProcessor {

	public static Mat processImage(Mat frame) {
		// if the frame is not empty, process it
		Mat processedImage = new Mat();
		Random rng = new Random(12345);

		if (!frame.empty()) {
			// Wandelt das RGB Bild in HSV um
			Imgproc.cvtColor(frame, processedImage, Imgproc.COLOR_BGR2HSV);
			//Speichert HSV werte in separaten variablen in einer Liste
			List<Mat> hsv = new ArrayList<>(3);
			Core.split(processedImage, hsv);

			//entnimmt den S-Wert aus HSV und speichert ihn
			Mat S = hsv.get(1);
			//Weichzeichnung des Bildes
			Imgproc.GaussianBlur(frame, frame, new Size(15, 15), 0);
			Imgproc.medianBlur(S, S, 15);

			// Threshold in binäres Bild mit der OTSU-Methode
			Mat region = new Mat();
			Imgproc.threshold(S, region, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

			// Gebe alle zusammengehörige Komponenten
			Mat labels = new Mat();
			Mat stats = new Mat();
			Mat centroids = new Mat();
			Imgproc.connectedComponentsWithStats(region, labels, stats, centroids, 4);

			// Wie viele Komponenten haben wir?
			Core.MinMaxLocResult x = Core.minMaxLoc(labels);

			// Prüfe bei Komponenten ob in Mitte des Bildes
			// tolerance = Abweichung von der Mitte des Bildes
			int width = frame.width();
			int height = frame.height();
			double tolerance = 0.3;
			Mat centroidRegion = new Mat(region.rows(), region.cols(), region.type(), Scalar.all(0));
			ArrayList<Integer> selectedLabels = new ArrayList<>();

			for (int l = 1; l < x.maxVal; l++) {
				int x_centroid = (int) centroids.get(l, 0)[0];
				int y_centroid = (int) centroids.get(l, 1)[0];

				if (x_centroid >= (width / 2) - tolerance * (width / 2)
						&& x_centroid <= (width / 2) + tolerance * (width / 2)) {
					if (y_centroid >= (height / 2) - tolerance * (height / 2)
							&& y_centroid <= (height / 2) + tolerance * (height / 2)) {
						// Alle Komponenten, die in der Mitte liegen werden in der Arraylist "selectedLabels" gespeichert
						selectedLabels.add(l);
					}
				}
			}

			// neues Bild mit weiß für alle ausgewählten Komponenten
			fillRegion(centroidRegion, labels, selectedLabels);

			// Video anhand der Maske Croppen und Maskiertes abspeichern
			Mat cropped = new Mat();

			//Versuch mit konturen von - OpenCV Dokumentation entnommen
			List<MatOfPoint> contours = new ArrayList<>();
			Mat hierarchy = new Mat();
			Imgproc.findContours(centroidRegion, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

			List<MatOfPoint> hullList = new ArrayList<>();
			for (MatOfPoint contour : contours) {
				MatOfInt hull = new MatOfInt();
				Imgproc.convexHull(contour, hull);
				Point[] contourArray = contour.toArray();
				Point[] hullPoints = new Point[hull.rows()];
				List<Integer> hullContourIdxList = hull.toList();
				for (int i = 0; i < hullContourIdxList.size(); i++) {
					hullPoints[i] = contourArray[hullContourIdxList.get(i)];
				}
				hullList.add(new MatOfPoint(hullPoints));
			}
			Mat drawing = Mat.zeros(centroidRegion.size(), CvType.CV_8UC3);
			for (int i = 0; i < contours.size(); i++) {
				Scalar color = new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256));
				Imgproc.drawContours(drawing, contours, i, color);
				Imgproc.drawContours(drawing, hullList, i, color );
			}

			frame.copyTo(cropped, centroidRegion);

			return drawing;
		}

		return null;
	}

	public static void fillRegion(Mat imgMat, Mat labels, ArrayList<Integer> selectedLabel) {
		for (int r = 0; r < imgMat.rows(); r++) {
			for (int c = 0; c < imgMat.cols(); c++) {
				if (!selectedLabel.contains((int) labels.get(r, c)[0])) {
					imgMat.put(r, c, 255);
				}
			}
		}
	}
}