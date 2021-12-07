package app;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;

import com.jogamp.opengl.util.FPSAnimator;

import org.opencv.core.Core;

public class MainWindow extends JFrame {

    private static final String FRAME_TITLE = "ManosCore - Programmable Pipeline";

    private static final int WINDOW_WIDTH = 1280;
    private static final int WINDOW_HEIGHT = 720;

    private static final int FRAME_RATE = 60;

    static StartRenderer canvas;
    static JPanel menuPanel;
    static JLabel imageLabel;

    // Constructor generating Java Swing window
    public MainWindow() {
        // load the native OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        ImageProcessor imageProcessor = new ImageProcessor();

        // Create the OpenGL Canvas for rendering content
        canvas = new StartRenderer();

        // Create an animator object for calling the display method of the GLCanvas at the defined frame rate.
        FPSAnimator animator = new FPSAnimator(canvas, FRAME_RATE, true);

        // Create the window container
        this.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));

        // Create and add split pane to window
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(300);
        splitPane.setEnabled(false);

        // Create and add menu panel to left side of split pane
        menuPanel = new JPanel();
        splitPane.setLeftComponent(menuPanel);

        // Add buttons for switching between scenes
        addButtons();
        addVideoCaputeButtons(imageProcessor);
        addImage(".\\resources\\images\\placeholder.jpg");

        // Create and add glpanel to right side of split pane
        JPanel glPanel = new JPanel();
        splitPane.setRightComponent(glPanel);
        glPanel.add(canvas);

        // Add split pane to window
        this.getContentPane().add(splitPane);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Thread to stop the animator before the program exits
                new Thread() {
                    @Override
                    public void run() {
                        if (animator.isStarted())
                            animator.stop();
                        System.exit(0);
                    }
                }.start();
            }
        });
        this.setResizable(false);
        this.setTitle(FRAME_TITLE);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);
        animator.start();

        // Set canvas size to size of glpanel
        canvas.setSize(glPanel.getSize());

        // OpenGL: request focus for canvas
        canvas.requestFocusInWindow();
    }

    public static void main(String[] args) {
        // Ensure thread safety
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainWindow();
            }
        });
    }

    private static void addButtons() {
        // Add Scene 1 button
        JButton buttonScene1 = new JButton("Szene 1");
        buttonScene1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                canvas.clickButton(0);
            }
        });
        menuPanel.add(buttonScene1);

        // Add Scene 2 button
        JButton buttonScene2 = new JButton("Szene 2");
        buttonScene2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                canvas.clickButton(1);
            }
        });
        menuPanel.add(buttonScene2);
    }

    private static void addVideoCaputeButtons(ImageProcessor imageProcessor) {
        JButton startCaptureButton = new JButton("Start Capture");
        startCaptureButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                imageProcessor.startCapture();
            }
        });
        menuPanel.add(startCaptureButton);

        JButton stopCaptureButton = new JButton("Stop Capture");
        stopCaptureButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                imageProcessor.stopCapture();
            }
        });
        menuPanel.add(stopCaptureButton);
    }

    public static void addImage(String path) {
        BufferedImage newImage;
        try {
            newImage = ImageIO.read(new File(path));
            imageLabel = new JLabel(new ImageIcon(newImage));
            menuPanel.add(imageLabel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showImage(BufferedImage image) {
        imageLabel.setIcon(new ImageIcon(image));
    }
}
