package app.opencv;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.awt.Image;
import java.awt.Color;

import org.opencv.core.Mat;

import app.Utils;
import app.gui.UserInterface;

/**
 * Zustädnig für die Auswahl der passenden 3D-Szene basierend auf der Geste, die auf dem Eingagbebild
 * erkannt wird. Das Erkennen der Handgeste soll später mit den Convexity Defects und k-Nearest-Neighbors
 * erkannt werden.
 * Momentan befinden sich hier placeholder Methoden, die die Szene basieren auf der Farbe des Pixels an der
 * Stelle 0, 0 auswählen. Bei Rot wird Szene 0, bei Grün Szene 1 und bei Blau Szene 2 angezeigt. Die placeholder
 * Methoden sollen später noch ersetzt werden.
 */
public class SceneSelect {

    UserInterface userInterface;

    /**
     * Konstruktor intialisiert die Instanzvariable.
     * @param userInterface	Referenz zum UserInterface
     */
    public SceneSelect(UserInterface userInterface) {
        this.userInterface = userInterface;
    }

    /**
     * TODO: selectScene mit richtiger Szenen-Auswahl ersetzen basierend auf den Convexity Defects.
     * Wählt aus, welcher der fünf 3D-Szenen angezeigt werden soll, basierend auf dem Farbwert an der
     * Stelle 0, 0.
     * @param image Eingabebild
     */
    public void selectScene(Image image) {
        Color pixelColor = getPixelColor(image);

        if (pixelColor.getRed() > 250) {
            // Wenn der Pixel Rot ist, wird Szene 0 angezeigt
            System.out.println("Red");
            setScene(0);
        } else if (pixelColor.getGreen() > 250) {
            // Wenn der Pixel Grün ist, wird Szene 1 angezeigt
            System.out.println("Green");
            setScene(1);
        } else if (pixelColor.getBlue() > 250) {
            // Wenn der Pixel Blau ist, wird Szene 2 angezeigt
            System.out.println("Blue");
            setScene(2);
        }
    }

    /**
     * Ändert das User Interface zu der OpenGL-Ansicht und zeigt die angegebene Szene an.
     * @param scene Szene die angezeigt werden soll
     */
    public void setScene(int scene) {
        userInterface.setContentScene(scene);
        userInterface.setControlsScene();
    }

    /**
     * Gibt den Farbwert des Pixels an der Stelle 0, 0 zurück.
     * @param image Eingabebild
     * @return      Farbwert des Pixels an der Stelle 0, 0
     */
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
