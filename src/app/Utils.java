package app;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

/**
 * Klasse mit statischen Helfermethoden.
 */
public class Utils {
	/**
	 * Gibt ein Bild aus einem Dateipfad als BufferedImage zurück.
	 * @param path	Dateipfad zum Bild
	 * @return		BufferedImage
	 */
	public static BufferedImage readImageFile(String path) {
		try {
			return ImageIO.read(new File(path));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Skaliert ein Bild auf die anegegebene Breite und Höhe.
	 * @param image		Eingabebild
	 * @param width		Breite
	 * @param height	Höhe
	 * @return			Ausgabebild
	 */
	public static Image resizeImage(Image image, int width, int height) {
		return image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
	}

	/**
	 * Speichert ein Bild mit dem angegebenen Pfad als jpg.
	 * @param image			Bild das gespeichert werden soll
	 * @param outputPath	Pfad
	 */
	public static void writeFile(Image image, String outputPath) {
		try {
			File outputfile = new File(outputPath + ".jpg");
			System.out.println("Write output File to: " + outputPath + ".jpg");
			ImageIO.write((RenderedImage) image, "jpg", outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Konvertiert ein BufferedImage in eine Mat.
	 * @param image		Eingabebild
	 * @return			Ausgabematrix
	 * @throws IOException
	 */
	public static Mat BufferedImage2Mat(BufferedImage image) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ImageIO.write(image, "jpg", byteArrayOutputStream);
		byteArrayOutputStream.flush();
		return Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()), Imgcodecs.IMREAD_UNCHANGED);
	}

	/**
	 * Konvertiert eine Mat in ein BufferedImage.
	 * @param matrix	Eingabematrix
	 * @return			Ausgabebild
	 * @throws IOException
	 */
	public static BufferedImage Mat2BufferedImage(Mat matrix) throws IOException {
		MatOfByte mob = new MatOfByte();
		Imgcodecs.imencode(".jpg", matrix, mob);
		return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
	}
}
