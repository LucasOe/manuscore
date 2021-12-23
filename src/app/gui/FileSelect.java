package app.gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

import org.opencv.core.Mat;

import app.Utils;
import app.opencv.ImageProcessor;

/**
 * Die FileSelect Klasse ist zur Auswahl einer Bilddatei zuständig, falls der Nutzer
 * seine Webcam nicht verwenden kann oder will.
 */
public class FileSelect {
	private UserInterface userInterface;
	private JFileChooser fileChooser;

	/**
	 * Konstruktor intialisiert die Instanzvariablen.
	 * @param userInterface	Referenz zum UserInterface
	 */
	public FileSelect(UserInterface userInterface) {
		this.userInterface = userInterface;

		fileChooser = new JFileChooser();
	}

	/**
	 * Zeigt einen Dialog zur Auswahl von Dateien und setzt currentFrame im userInterface
	 * zum ausgewählten und verarbeitetem Bild.
	 */
	public void selectFile() {
		// Zeigt den Dialog zur Dateiauswahl an
		int result = fileChooser.showOpenDialog(null);

		if (result == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			try {
				// Speichert die ausgewählte Datei als BufferedImage
				BufferedImage image = ImageIO.read(file);

				// Verarbeitet das Bild und speichert es im imageProcessed
				Mat frame = Utils.BufferedImage2Mat(image);
				Mat frameProcessed = ImageProcessor.processImage(userInterface, frame);
				BufferedImage imageProcessed = Utils.Mat2BufferedImage(frameProcessed);

				// Übergibt das verarbeitete und das unverarbeitete Bild an das UserInterface
				userInterface.setCurrentFrame(imageProcessed, image);
				userInterface.setWebcamIcon(imageProcessed);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Error choosing file");
		}
	}
}
