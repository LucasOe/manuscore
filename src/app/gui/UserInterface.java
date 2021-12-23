package app.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.jogamp.opengl.util.FPSAnimator;

import app.Utils;
import app.opencv.SceneSelect;
import app.opencv.WebcamCapture;
import app.opengl.StartRenderer;

public class UserInterface extends JFrame {
	private final boolean isDebug = true; // FOR DEBUG PURPOSES JUMP STRAIGHT TO MODEL VIEW

	private static final String FRAME_TITLE = "ManusCore";
	private static final int FRAME_WIDTH = 1280;
	private static final int FRAME_HEIGHT = 720;
	private static final int FRAME_RATE = 60;
	private static final int CONTENT_WIDTH = 720;
	private static final int CONTENT_HEIGHT = 480;

	private StartRenderer renderCanvas;
	private WebcamCapture webcamCapture;
	private FileSelect fileSelect;
	private SceneSelect sceneSelect;
	private FPSAnimator animator;

	private JPanel contentPanel;
	private JPanel controlsPanel;

	private JLabel webcamLabel;
	private JButton captureButton;

	// The image that is used to select the scene
	private Image currentFrame, currentFrameBefore;

	public UserInterface() {
		// initialize Components
		webcamCapture = new WebcamCapture(this, CONTENT_WIDTH, CONTENT_HEIGHT);
		fileSelect = new FileSelect(this);
		sceneSelect = new SceneSelect(this);

		initializeUserInterface();

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// Thread to stop the animator before the program exits
				new Thread() {
					@Override
					public void run() {
						if (animator != null && animator.isStarted())
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
	}

	private void initializeUserInterface() {
		// Create the window container
		this.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));

		// Create and add split pane to window
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setDividerLocation(500);
		splitPane.setEnabled(false);

		// Create and add webcam output as the top component of the split pane
		contentPanel = new JPanel(new GridBagLayout());
		splitPane.setTopComponent(contentPanel);
		if (!isDebug)
			setContentWebcam();
		else
			setContentScene(0);

		// Create and add menu panel as the bottom component of the split pane
		controlsPanel = new JPanel(new GridBagLayout());
		splitPane.setBottomComponent(controlsPanel);
		if (!isDebug)
			setControlsWebcam();
		else
			setControlsScene();

		// Add split pane to window
		this.getContentPane().add(splitPane);
	}

	// Add the buttons for starting & stopping the webcam and a button for uploading an image file instead
	private JPanel getControlsWebcam() {
		JPanel controls = new JPanel();

		Dimension buttonSize = new Dimension(130, 25);

		BoxLayout boxLayout = new BoxLayout(controls, BoxLayout.Y_AXIS);
		controls.setLayout(boxLayout);

		JPanel webcamControls = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));

		String label = webcamCapture.getLabel();
		captureButton = new JButton(label);
		captureButton.setPreferredSize(buttonSize);
		captureButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				webcamCapture.toggleCapture();
			}
		});
		webcamControls.add(captureButton);

		JButton uploadButton = new JButton("Bild hochladen");
		uploadButton.setPreferredSize(buttonSize);
		uploadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fileSelect.selectFile();
			}
		});
		webcamControls.add(uploadButton);

		JButton continueButton = new JButton("Weiter");
		continueButton.setPreferredSize(buttonSize);
		continueButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				continueWithFrame();
			}
		});
		webcamControls.add(continueButton);

		controls.add(webcamControls);

		// Unsichtbares JPanel für vertikalen Abstand
		controls.add(new JPanel());

		// Szenenauswahl
		JPanel sceneControls = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));

		JButton scene1Button = new JButton("(A)pfel");
		scene1Button.setPreferredSize(buttonSize);
		scene1Button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sceneSelect.setScene(0);
			}
		});
		sceneControls.add(scene1Button);

		JButton scene2Button = new JButton("(C)hristbaum");
		scene2Button.setPreferredSize(buttonSize);
		scene2Button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sceneSelect.setScene(1);
			}
		});
		sceneControls.add(scene2Button);

		JButton scene3Button = new JButton("G(eschenk)");
		scene3Button.setPreferredSize(buttonSize);
		scene3Button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sceneSelect.setScene(2);
			}
		});
		sceneControls.add(scene3Button);

		JButton scene4Button = new JButton("(L)iebe");
		scene4Button.setPreferredSize(buttonSize);
		scene4Button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sceneSelect.setScene(3);
			}
		});
		sceneControls.add(scene4Button);

		JButton scene5Button = new JButton("(W)eihnachtsmann");
		scene5Button.setPreferredSize(buttonSize);
		scene5Button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sceneSelect.setScene(4);
			}
		});
		sceneControls.add(scene5Button);

		controls.add(sceneControls);

		return controls;
	}

	// Add back button to return to the webcam view
	private JPanel getControlsScene() {
		JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEADING, 20, 0));

		JButton backButton = new JButton("Zurück");
		backButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setContentWebcam();
				setControlsWebcam();
			}
		});
		controls.add(backButton);

		return controls;
	}

	// Get image label to show webcam output
	private JPanel getContentWebcam() {
		JPanel content = new JPanel();

		webcamLabel = new JLabel();
		content.add(webcamLabel);

		return content;
	}

	// Get the opengl canvas
	private JPanel getContentScene(int scene) {
		JPanel content = new JPanel();

		renderCanvas = new StartRenderer(scene);

		// Create an animator object for calling the display method of the GLCanvas at the defined frame rate.
		animator = new FPSAnimator(renderCanvas, FRAME_RATE, true);
		animator.start();

		renderCanvas.setSize(new Dimension(CONTENT_WIDTH, CONTENT_HEIGHT));
		renderCanvas.requestFocusInWindow();

		content.add(renderCanvas);

		return content;
	}

	// Set content to the webcam output
	public void setContentWebcam() {
		if (animator != null && animator.isAnimating())
			animator.stop();
		contentPanel.removeAll();

		JPanel content = getContentWebcam();
		contentPanel.add(content);

		contentPanel.revalidate();
		contentPanel.repaint();
	}

	// Set content to the opengl canvas
	public void setContentScene(int scene) {
		contentPanel.removeAll();
		JPanel content = getContentScene(scene);
		contentPanel.add(content);

		contentPanel.revalidate();
		contentPanel.repaint();
	}

	// Set controls to the webcam buttons
	public void setControlsWebcam() {
		controlsPanel.removeAll();
		JPanel controls = getControlsWebcam();
		controlsPanel.add(controls);

		controlsPanel.revalidate();
		controlsPanel.repaint();
	}

	// Set controls to the opengl buttons
	public void setControlsScene() {
		controlsPanel.removeAll();
		JPanel controls = getControlsScene();
		controlsPanel.add(controls);

		controlsPanel.revalidate();
		controlsPanel.repaint();
	}

	public void setWebcamIcon(Image image) {
		Image imageResized = Utils.resizeImage(image, CONTENT_WIDTH, CONTENT_HEIGHT);
		webcamLabel.setIcon(new ImageIcon(imageResized));
	}

	public void setWebcamText(String text) {
		captureButton.setText(text);
	}

	public void setCurrentFrame(Image imageProcessed, Image imageBefore) {
		this.currentFrame = imageProcessed;
		this.currentFrameBefore = imageBefore;
	}

	// Continue with current frame
	private void continueWithFrame() {
		if (currentFrame != null) {
			sceneSelect.selectScene(currentFrame);
			if (isDebug) {
				Utils.writeFile(currentFrame, "Debug_WebcamOutput_After");
				Utils.writeFile(currentFrameBefore, "Debug_WebcamOutput_Before");
			}
		}
	}

}
