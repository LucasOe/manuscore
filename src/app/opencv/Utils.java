package app.opencv;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import javax.imageio.ImageIO;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

public final class Utils {

	// Conver Mat object to BufferedImage
	static BufferedImage Mat2BufferedImage(Mat matrix) throws Exception {
		MatOfByte mob = new MatOfByte();
		Imgcodecs.imencode(".jpg", matrix, mob);
		byte bufferedArray[] = mob.toArray();

		BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(bufferedArray));
		return bufferedImage;
	}
}
