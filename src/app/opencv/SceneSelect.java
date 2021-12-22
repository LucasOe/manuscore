package app.opencv;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.awt.Image;
import java.awt.Color;

import org.opencv.core.Mat;

import app.Utils;
import app.gui.UserInterface;

public class SceneSelect {

    UserInterface userInterface;

    public SceneSelect(UserInterface userInterface) {
        this.userInterface = userInterface;
    }

    // TODO: Scene selection mit richtiger Szenen-Auswahl ersetzen.
    // Placeholder to select the scene depending on the top left pixel color
    public void selectScene(Image image) {
        Color pixelColor = getPixelColor(image);

        if (pixelColor.getRed() > 250) {
            System.out.println("Red");
            setScene(0);
        } else if (pixelColor.getGreen() > 250) {
            System.out.println("Green");
            setScene(1);
        } else if (pixelColor.getBlue() > 250) {
            System.out.println("Blue");
            setScene(2);
        }
    }

    private void setScene(int scene) {
        userInterface.setContentScene(scene);
        userInterface.setControlsScene();
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
