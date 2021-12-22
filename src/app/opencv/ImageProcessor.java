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

public class ImageProcessor {

	public static Mat processImage(Mat frame) {
		// if the frame is not empty, process it
		Mat processedImage = new Mat();

		if (!frame.empty()) {
			// Wandelt das RGB Bild in HSV um
			Imgproc.cvtColor(frame, processedImage, Imgproc.COLOR_BGR2HSV);
			//Speichert HSV werte in separaten variablen in einer Liste
			List<Mat> hsv = new ArrayList<>(3);
			Core.split(processedImage, hsv);

			//entnimmt den V-Wert aus HSV und speichert ihn
			Mat V = hsv.get(2);
			//Weichzeichnung des Bildes
			Imgproc.GaussianBlur(frame, frame, new Size(15, 15), 0);
			Imgproc.medianBlur(V, V, 15);

			// Threshold in binäres Bild mit der OTSU-Methode
			Mat region = new Mat();
			//Imgproc.cvtColor(frame, region, Imgproc.COLOR_BGR2GRAY);
			Imgproc.threshold(V, region, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

			// Alle Variablen von ConnectedComponentsWithStats separat speichern
			Mat labels = new Mat();
			Mat stats = new Mat();
			Mat centroids = new Mat();

			//Sucht zusammengehörige Regionen
			Imgproc.connectedComponentsWithStats(region, labels, stats, centroids, 4);

			// Wie viele Label haben wir?
			Core.MinMaxLocResult x = Core.minMaxLoc(labels);

			// Prüfe bei Komponenten ob in Mitte des Bildes
			// tolerance = Abweichung von der Mitte des Bildes
			int width = frame.width();
			int height = frame.height();
			double tolerance = 0.4;
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

			//Versuch mit konturen von - OpenCV Dokumentation entnommen und abgeändert
			List<MatOfPoint> contours = new ArrayList<>();
			MatOfPoint biggestContour;

			Mat hierarchy = new Mat();
			Imgproc.findContours(centroidRegion, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
			//System.out.println("Amount of Contours: " + contours.size());

			//Bild der Hand ausgeschnitten
			frame.copyTo(cropped, centroidRegion);
			Mat drawing = Mat.zeros(centroidRegion.size(), CvType.CV_8UC3);
			if (contours.size() < 2) {
				return cropped;
			}

			double maxArea = 0.0;
			//Ersten index ueberspringen, da darin automatisch riesiger Wert gespeichert wird
			int index = 1;
			for (int k = 1; k < contours.size(); k++) {
				double area = Imgproc.contourArea(contours.get(k));
				if (area >= maxArea) {
					maxArea = area;
					index = k;
					//System.out.println("max: " + maxArea + " | Index: " + index);
				}
			}
			biggestContour = contours.get(index);
			//System.out.println("Amount of biggestContour: " + biggestContour.size());

			//Convex Hull Bilden
			/*
			MatOfInt hull = new MatOfInt();
			Imgproc.convexHull(biggestContour, hull);
			
			Point[] contourArray = biggestContour.toArray();
			Point[] hullPoints = new Point[hull.rows()];
			List<Integer> hullContourIdxList = hull.toList();
			for (int i = 0; i < hullContourIdxList.size(); i++) {
				hullPoints[i] = contourArray[hullContourIdxList.get(i)];
			}
			MatOfPoint hullPointsMat = new MatOfPoint(hullPoints);
			*/
			/*
			//ConvexityDefects erkennen
			MatOfInt4 defects = new MatOfInt4();
			Imgproc.convexityDefects(contour, hull, defects);
			System.out.println(defects.size().area());
			*/

			List<MatOfPoint> biggestContourList = new ArrayList<>();
			//List<MatOfPoint> hullPointsMatList = new ArrayList<>();
			biggestContourList.add(biggestContour);
			//hullPointsMatList.add(hullPointsMat);

			Scalar color = new Scalar(0, 255, 0);
			Imgproc.drawContours(drawing, biggestContourList, 0, color);
			//Imgproc.drawContours(drawing, hullPointsMatList, 0, color);

			MatOfPoint2f NewMtx = new MatOfPoint2f(biggestContour.toArray());
			double area = Imgproc.contourArea(biggestContour);
			double perimeter = Imgproc.arcLength(NewMtx, true);
			System.out.println("Area of Contour: " + area / (720 * 480) * 100.0);
			System.out.println("Perimeter of Contour: " + perimeter / (720 * 480) * 100.0);
			System.out.println();

			// Versuch mit CornerHarris Kantenerkennung
			/*
			Mat harris = new Mat();
			Imgproc.cvtColor(frame, processedImage, Imgproc.COLOR_BGR2GRAY);
			
			Imgproc.cornerHarris(harris,2,3,0.04);
			*/

			//Hulls über das aktuelle Bild legen
			//frame.copyTo(cropped, drawing);

			return cropped;
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