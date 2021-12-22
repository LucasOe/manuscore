package app.gui;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Utils {
	// Read image from file and return the buffered image
	public static BufferedImage readImageFile(String path) {
		try {
			return ImageIO.read(new File(path));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	// Resize image to set width and height
	public static Image resizeImage(Image image, int width, int height) {
		return image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
	}

	public static void writeFile(Image image) {
		try {
			File outputfile = new File("Debug_WebcamOutput.jpg");
			ImageIO.write((RenderedImage) image, "jpg", outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
