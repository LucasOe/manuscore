package app;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class ImageProcessor {

    public static Mat processImage(Mat frame) {
        System.out.println("Processing image");

        // if the frame is not empty, process it
        Mat processedImage = new Mat();

        if (!frame.empty()) {
            // Wandelt das RGB Bild in HSV um
            Imgproc.cvtColor(frame, processedImage, Imgproc.COLOR_BGR2HSV);
            List<Mat> hsv = new ArrayList<>(3);
            Core.split(processedImage, hsv);

            //entnimmt den S-Wert aus HSV und speichert ihn
            Mat S = hsv.get(1);
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
            System.out.println("MaxVal: " + x.maxVal);

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
            frame.copyTo(cropped, centroidRegion);

            /*
            List<double[]> allWidthValue = new ArrayList<>();
            
            double resultH = 1;
            double resultS = 1;
            double resultV = 1;
            
            //Inspiriert durch Thomas Engel aus meinem Studiengang
            //(Vewendung von ".get" fuer Farbwert eines Pixels und nur weiße Pixel in Liste einfuegen)
            
            ///////ERROR IN VIDEO WENN KEINE MASKE GEFUNDEN WIRD (DER HINTERGRUND SCHWARZ IST)
            for (int y = 0; y < cropped.rows(); y++) {
                for (int j = 0; j < cropped.cols(); j++) {
                    // HSV WERT in widthValue
                    double[] widthValues = cropped.get(y, j);
                    int value = (int) widthValues[0];
            
                    //waehlt nur Bildsegment innerhalb der Maske aus
                    if (value != 0) {
            
                        allWidthValue.add(widthValues);
            
                        resultH += widthValues[0];
                        resultS += widthValues[1];
                        resultV += widthValues[2];
                    }
                }
            }
            int[] hsvCrop = new int[3];
            hsvCrop[0] = (int) resultH / allWidthValue.size();
            hsvCrop[1] = (int) resultS / allWidthValue.size();
            hsvCrop[2] = (int) resultV / allWidthValue.size();
            */

            //Mittelwert der HSV Farben
            //System.out.println("H: " + hsvCrop[0] + ", " + "S: " + hsvCrop[1] + ", " + "V: " + hsvCrop[2]);

            return cropped;
        }

        return frame;
    }

    public static void fillRegion(Mat imgMat, Mat labels, ArrayList<Integer> selectedLabel) {
        for (int r = 0; r < imgMat.rows(); r++) {
            for (int c = 0; c < imgMat.cols(); c++) {
                if (selectedLabel.contains(new Integer((int) labels.get(r, c)[0]))) {
                    imgMat.put(r, c, 255);
                }
            }
        }
    }
}
