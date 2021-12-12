package app.gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

public class FileSelect {
    private UserInterface userInterface;
    private JFileChooser fileChooser;

    public FileSelect(UserInterface userInterface) {
        this.userInterface = userInterface;
        fileChooser = new JFileChooser();
    }

    // Show the choose file dialogue
    public void selectFile() {
        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            //This is where a real application would open the file.
            System.out.println(file.getName());

            try {
                BufferedImage image = ImageIO.read(file);
                userInterface.setWebcamIcon(image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Error choosing file");
        }
    }
}
