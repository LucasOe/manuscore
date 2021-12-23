package app.opencv;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static app.Utils.BufferedImage2Mat;


public class TrainingData {

    //Label
    private int id;

    //Features
    private double area;
    private double perimeter;
    private double aspectRatio;
    private double defectAmount;

    public static void getFiles(){
        String trainingDataPath = ".\\resources\\trainingdata\\";
        File folder = new File(trainingDataPath);
        File[] files = folder.listFiles();

        for (File file:files){
            if (file.isFile()){
                //System.out.println("File -> " + file.getName());

                try {
                    BufferedImage image = ImageIO.read(file);
                    BufferedImage2Mat(image);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        System.out.println("Amount of Trainingdata: " + files.length);

    }

}
