package app.opencv;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.awt.Image;
import java.awt.Color;

import org.opencv.core.Mat;

public class SceneSelect {

    // Placeholder to select the scene depending on the top left pixel color
    public void selectScene(Image image) {
        Color pixelColor = getPixelColor(image);

        if (pixelColor.getRed() > 250) {
            System.out.println("Red");
        } else if (pixelColor.getGreen() > 250) {
            System.out.println("Green");
        } else if (pixelColor.getBlue() > 250) {
            System.out.println("Blue");
        }
    }

    // Placeholder method to get the values of the top left pixel
    private Color getPixelColor(Image image) {
        BufferedImage bufferedImage = (BufferedImage) image;
        try {
            Mat mat = Utils.BufferedImage2Mat(bufferedImage);
            double[] pixel = mat.get(0, 0);
            return new Color((int) pixel[2], (int) pixel[1], (int) pixel[0]);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
