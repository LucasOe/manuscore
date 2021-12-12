package app.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.jogamp.opengl.util.FPSAnimator;

import app.StartRenderer;
import app.WebcamCapture;

public class UserInterface extends JFrame {
    private static final String FRAME_TITLE = "ManosCore - Programmable Pipeline";
    private static final int FRAME_WIDTH = 1280;
    private static final int FRAME_HEIGHT = 720;
    private static final int FRAME_RATE = 60;

    private StartRenderer renderCanvas;
    private WebcamCapture webcamCapture;
    private FPSAnimator animator;

    public UserInterface(WebcamCapture webcamCapture, StartRenderer renderCanvas) {
        this.webcamCapture = webcamCapture;
        this.renderCanvas = renderCanvas;

        initializeUserInterface();

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
        this.setResizable(true);
        this.setTitle(FRAME_TITLE);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }

    private void initializeUserInterface() {
        // Create an animator object for calling the display method of the GLCanvas at the defined frame rate.
        animator = new FPSAnimator(renderCanvas, FRAME_RATE, true);

        // Create the window container
        this.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));

        // Create and add split pane to window
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(600);
        splitPane.setEnabled(false);

        // Create and add webcam output as the top component of the split pane
        JPanel contentPanel = new JPanel(new GridBagLayout());
        JPanel content = getContentWebcam();
        contentPanel.add(content);
        splitPane.setTopComponent(contentPanel);

        // Create and add menu panel as the bottom component of the split pane
        JPanel contolsPanel = new JPanel(new GridBagLayout());
        JPanel controls = getControlsWebcam();
        contolsPanel.add(controls);
        splitPane.setBottomComponent(contolsPanel);

        // Add split pane to window
        this.getContentPane().add(splitPane);
    }

    // Add the buttons for starting & stopping the webcam and a button for uploading an image file instead
    private JPanel getControlsWebcam() {
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEADING, 20, 0));

        JButton captureButton = new JButton("Aufnahme beginnen");
        captureButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                webcamCapture.startCapture();
            }
        });
        controls.add(captureButton);

        JButton uploadButton = new JButton("Bild hochladen");
        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                webcamCapture.stopCapture();
            }
        });
        controls.add(uploadButton);

        return controls;
    }

    // Get image label to show webcam output
    private JPanel getContentWebcam() {
        JPanel content = new JPanel();

        // Get and resize image
        Image placeholderImage = readImageFile(".\\resources\\images\\webcam_placeholder.jpg");
        Image placeholderImageResized = resizeImage(placeholderImage, 720, 480);

        JLabel webcamLabel = new JLabel(new ImageIcon(placeholderImageResized));
        content.add(webcamLabel);

        return content;
    }

    private BufferedImage readImageFile(String path) {
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Image resizeImage(Image image, int width, int height) {
        return image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }
}
