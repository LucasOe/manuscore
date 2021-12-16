package app.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.jogamp.opengl.util.FPSAnimator;

import app.opencv.SceneSelect;
import app.opencv.WebcamCapture;
import app.opengl.StartRenderer;

public class UserInterface extends JFrame {
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
	private Image currentFrame;

	public UserInterface() {
		// initialize Components
		webcamCapture = new WebcamCapture(this, CONTENT_WIDTH, CONTENT_HEIGHT);
		fileSelect = new FileSelect(this);
		sceneSelect = new SceneSelect(this);
		renderCanvas = new StartRenderer();

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
		this.setResizable(false);
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
		contentPanel = new JPanel(new GridBagLayout());
		splitPane.setTopComponent(contentPanel);
		setContentWebcam();

		// Create and add menu panel as the bottom component of the split pane
		controlsPanel = new JPanel(new GridBagLayout());
		splitPane.setBottomComponent(controlsPanel);
		setControlsWebcam();

		// Add split pane to window
		this.getContentPane().add(splitPane);
	}

	// Add the buttons for starting & stopping the webcam and a button for uploading an image file instead
	private JPanel getControlsWebcam() {
		JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEADING, 20, 0));

		String label = webcamCapture.getLabel();

		captureButton = new JButton(label);
		captureButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				webcamCapture.toggleCapture();
			}
		});
		controls.add(captureButton);

		JButton uploadButton = new JButton("Bild hochladen");
		uploadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fileSelect.selectFile();
			}
		});
		controls.add(uploadButton);

		JButton continueButton = new JButton("Weiter");
		continueButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				continueWithFrame();
			}
		});
		controls.add(continueButton);

		return controls;
	}

	// Add back button to return to the webcam view
	private JPanel getControlsScene() {
		JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEADING, 20, 0));

		JButton continueButton = new JButton("Zurück");
		continueButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setContentWebcam();
				setControlsWebcam();
			}
		});
		controls.add(continueButton);

		return controls;
	}

	// Get image label to show webcam output
	private JPanel getContentWebcam(String path) {
		JPanel content = new JPanel();

		// Get and resize image
		Image placeholderImage = Utils.readImageFile(path);
		Image placeholderImageResized = Utils.resizeImage(placeholderImage, CONTENT_WIDTH, CONTENT_HEIGHT);

		webcamLabel = new JLabel(new ImageIcon(placeholderImageResized));
		content.add(webcamLabel);

		return content;
	}

	// Get the opengl canvas
	private JPanel getContentScene(int scene) {
		JPanel content = new JPanel();

		// Set scene as text
		JLabel text = new JLabel("Szene: " + String.valueOf(scene));
		content.add(text);

		return content;
	}

	// set content to the webcam output
	public void setContentWebcam() {
		if (contentPanel.getComponents().length > 0) {
			contentPanel.remove(0);
		}
		JPanel content = getContentWebcam(".\\resources\\images\\placeholder_webcam.jpg");
		contentPanel.add(content);

		contentPanel.revalidate();
		contentPanel.repaint();
	}

	// set content to the opengl canvas
	public void setContentScene(int scene) {
		if (contentPanel.getComponents().length > 0) {
			contentPanel.remove(0);
		}
		JPanel content = getContentScene(scene);
		contentPanel.add(content);

		contentPanel.revalidate();
		contentPanel.repaint();
	}

	// Set controls to the webcam buttons
	public void setControlsWebcam() {
		if (controlsPanel.getComponents().length > 0) {
			controlsPanel.remove(0);
		}
		JPanel controls = getControlsWebcam();
		controlsPanel.add(controls);

		controlsPanel.revalidate();
		controlsPanel.repaint();
	}

	// Set controls to the opengl buttons
	public void setControlsScene() {
		if (controlsPanel.getComponents().length > 0) {
			controlsPanel.remove(0);
		}
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

	public void setCurrentFrame(Image image) {
		this.currentFrame = image;
	}

	// Continue with current frame
	private void continueWithFrame() {
		if (currentFrame != null) {
			sceneSelect.selectScene(currentFrame);
		}
	}

}
