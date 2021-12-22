package app.opencv;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class TrainingData {

    public static void getFiles(){
        String trainingDataPath = ".\\resources\\trainingdata\\";
        File folder = new File(trainingDataPath);
        File[] files = folder.listFiles();

        for (File file:files){
            if (file.isFile()){
                System.out.println("File -> " + file.getName());
                try {
                    BufferedImage image = ImageIO.read(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

}
