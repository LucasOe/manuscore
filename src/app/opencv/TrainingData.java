package app.opencv;
import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static app.Utils.BufferedImage2Mat;


public class TrainingData {

    public static List<double[]> matOfTrainingData;

    public static void getFiles(){

        //Features
        double area;
        double perimeter;
        double aspectRatio;
        double defectAmount;

        String trainingDataPath = ".\\resources\\trainingdata\\";
        File folder = new File(trainingDataPath);
        File[] files = folder.listFiles();

        for (File file:files){
            if (file.isFile()){
                //System.out.println("File -> " + file.getName());

                try {
                    BufferedImage image = ImageIO.read(file);
                    char label = file.getName().charAt(0);
                    Mat image2Mat = BufferedImage2Mat(image);
                    ImageProcessor.processImage(image2Mat);
                    System.out.println("Label: " + label);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        System.out.println("Amount of Trainingdata: " + files.length);

    }

}
