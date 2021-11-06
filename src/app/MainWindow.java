package app;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

public class MainWindow extends JFrame {

    private static final String FRAME_TITLE = "ManosCore - Programmable Pipeline";

    private static final int WINDOW_WIDTH = 1280;
    private static final int WINDOW_HEIGHT = 720;

    private static final int FRAME_RATE = 60;

    private static GLCanvas canvas;

    // Constructor generating Java Swing window
    public MainWindow() {
        // Setup an OpenGL context for the GLCanvas
        // Using the JOGL-Profile GL2
        // GL2: Compatibility profile, OpenGL Versions 1.0 to 3.0
        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities capabilities = new GLCapabilities(profile);

        // Create the OpenGL Canvas for rendering content
        canvas = new StartRenderer(capabilities);

        // Create an animator object for calling the display method of the GLCanvas at the defined frame rate.
        final FPSAnimator animator = new FPSAnimator(canvas, FRAME_RATE, true);

        // Create the window container
        this.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));

        // Create and add split pane to window
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(300);
        splitPane.setEnabled(false);

        // Create and add menu panel to left side of split pane
        JPanel menuPanel = new JPanel();
        splitPane.setLeftComponent(menuPanel);

        // Add buttons for switching between scenes
        addButtons(menuPanel, capabilities);

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

    private static void addButtons(JPanel menuPanel, GLCapabilities capabilities) {
        // Add Scene 1 button
        JButton buttonScene1 = new JButton("Szene 1");
        buttonScene1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Szene 1");
            }
        });
        menuPanel.add(buttonScene1);

        // Add Scene 2 button
        JButton buttonScene2 = new JButton("Szene 2");
        buttonScene2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Szene 2");
            }
        });
        menuPanel.add(buttonScene2);
    }
}
