package app.opencv;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
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
		Mat frameCenterLabels = new Mat(frameBinary.rows(), frameBinary.cols(), frameBinary.type(), Scalar.all(255));

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

		// Füllt alle Regionen die außerhalb der Mitte sind
		fillRegion(frameLabels, frameCenterLabels, selectedLabels);

		// Frame anhand der center labels maskieren
		Mat frameMasked = new Mat();
		frame.copyTo(frameMasked, frameCenterLabels);

		// Erkenne Konturen
		List<MatOfPoint> contours = new ArrayList<>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(frameCenterLabels, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
		//System.out.println("Amount of Contours: " + contours.size());

		// Finde die größte Kontur
		MatOfPoint biggestContour = new MatOfPoint();
		double maxArea = 0;
		for (int i = 0; i < contours.size(); i++) {
			double area = Imgproc.contourArea(contours.get(i));
			if (area >= maxArea) {
				maxArea = area;
				biggestContour = contours.get(i);
			}
		}

		// Returned frameMasked wenn keine Kontur gefunden wurde
		//System.out.println("Amount of biggestContour: " + biggestContour.size());
		if (biggestContour.size().empty())
			return frameMasked;

		// Convex Hull bilden
		// https://coderedirect.com/questions/285270/android-java-opencv-2-4-convexhull-convexdefect
		MatOfInt hull = new MatOfInt();
		List<Point> hullPointList = new ArrayList<>();

		Imgproc.convexHull(biggestContour, hull);

		for (int i = 0; i < hull.toList().size(); i++) {
			hullPointList.add(biggestContour.toList().get(hull.toList().get(i)));
		}

		MatOfPoint hullPointMat = new MatOfPoint();
		hullPointMat.fromList(hullPointList);

		List<MatOfPoint> hullPointsMatList = new ArrayList<>();
		List<MatOfPoint> biggestContourList = new ArrayList<>();
		hullPointsMatList.add(hullPointMat);
		biggestContourList.add(biggestContour);

		Mat frameHull = new Mat(frameMasked.rows(), frameMasked.cols(), frameMasked.type(), Scalar.all(0));
		frameMasked.copyTo(frameHull);
		Imgproc.drawContours(frameHull, hullPointsMatList, -1, new Scalar(0, 255, 0), 1);
		Imgproc.drawContours(frameHull, biggestContourList, -1, new Scalar(0, 255, 0), 1);

		// Covex Defects erkennen
		MatOfInt4 defects = new MatOfInt4();
		Imgproc.convexityDefects(biggestContour, hull, defects);

		Point[] data = biggestContour.toArray();
		List<Integer> defectsLists = defects.toList();
		for (int j = 0; j < defectsLists.size(); j = j + 4) {
			Point defectStart = data[defectsLists.get(j)];
			Point defectEnd = data[defectsLists.get(j + 1)];
			Point defect = data[defectsLists.get(j + 2)];

			int radius = 2;
			Imgproc.circle(frameHull, defectStart, radius, new Scalar(0, 255, 0), 1);
			Imgproc.circle(frameHull, defectEnd, radius, new Scalar(0, 255, 0), 1);
			Imgproc.circle(frameHull, defect, radius, new Scalar(0, 0, 255), 2);
		}

		// Processed frame zurückgeben
		return frameHull;
	}

	public static void fillRegion(Mat src, Mat dst, ArrayList<Integer> selectedLabel) {
		for (int r = 0; r < dst.rows(); r++) {
			for (int c = 0; c < dst.cols(); c++) {
				if (!selectedLabel.contains((int) src.get(r, c)[0])) {
					dst.put(r, c, 0);
				}
			}
		}
	}
}