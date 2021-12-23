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

/**
 * Die UserInterface Klasse ist für das Erstellen und Anzeigen des User Interfaces zuständig.
 * Das Interface ist aufgeteilt in das contentPanel, welches das Bild von der Webcam oder den OpenGL-
 * Canvas anzeigt, und in das controlsPanel, welches die Buttons zur Navigation der jeweiligen
 * Ansicht anzeigt. Der Inhalt wird ausgetauscht je nachdem welche Ansicht gerade aktiv ist.
 */
public class UserInterface extends JFrame {
	// Für Debugzwecke. Ist der Wert auf true, dann startet das Programm mit dem OpenGL-Canvas
	// und speichert ein Vorher/Nachher-Bild von der Webcam, wenn man auf "Weiter" klickt.
	private final boolean isDebug = false;

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

	// Wird an SceneSelect übergeben für die Auswahl der Szene.
	private Image currentFrame;
	// Speichert das unverarbeitete Bild von der Webcam für einen Vorher/Nachher-Vergleich.
	private Image currentFrameBefore;

	/**
	 * Konstruktor erstellt die benötigten Instanzen und zeigt das GUI an.
	 */
	public UserInterface() {
		// Initialisierung
		webcamCapture = new WebcamCapture(this, CONTENT_WIDTH, CONTENT_HEIGHT);
		fileSelect = new FileSelect(this);
		sceneSelect = new SceneSelect(this);

		// Erstellt das User Interface
		initializeUserInterface();

		// Stoppt den Animator wenn man das Programm verlässt.
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
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

	/**
	 * Zuständig für das Erstellen des User Interfaces. JSplitPane teilt das User Interface in
	 * zwei Bereiche für den Inhalt und die Buttons. Wenn isDebug true ist, startet das
	 * Programm mit dem OpenGL-Canvas.
	 */
	private void initializeUserInterface() {
		// Erstellt den Window Container mit der angegbenen Größe
		this.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));

		// Erstellt ein JSplitPane
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setDividerLocation(500);
		splitPane.setEnabled(false);

		// Fügt contentPanel als oberen Component zu splitPane hinzu
		contentPanel = new JPanel(new GridBagLayout());
		splitPane.setTopComponent(contentPanel);
		if (!isDebug)
			setContentWebcam();
		else
			setContentScene(0);

		// Fügt controlsPanel als unteren Component zu splitPane hinzu
		controlsPanel = new JPanel(new GridBagLayout());
		splitPane.setBottomComponent(controlsPanel);
		if (!isDebug)
			setControlsWebcam();
		else
			setControlsScene();

		// Fügt die splitPane zum Fenster hinzu
		this.getContentPane().add(splitPane);
	}

	/**
	 * Erstellt die Controls für die Steuerungselemente in der Webcam-Ansicht.
	 * Fügt Aufnahme Starten, Bild hochladen, Weiter und die direkte Auswahl der 3D-Szene hinzu.
	 * @return	Controls für die Webcam-Ansicht
	 */
	private JPanel getControlsWebcam() {
		JPanel controls = new JPanel();

		Dimension buttonSize = new Dimension(130, 25);

		BoxLayout boxLayout = new BoxLayout(controls, BoxLayout.Y_AXIS);
		controls.setLayout(boxLayout);

		JPanel webcamControls = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));

		// Aufnahme Starten / Aufnahme Stoppen
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

		// Bild hochladen
		JButton uploadButton = new JButton("Bild hochladen");
		uploadButton.setPreferredSize(buttonSize);
		uploadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fileSelect.selectFile();
			}
		});
		webcamControls.add(uploadButton);

		// Weiter
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

		// Szene 0
		JButton scene0Button = new JButton("(A)pfel");
		scene0Button.setPreferredSize(buttonSize);
		scene0Button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sceneSelect.setScene(0);
			}
		});
		sceneControls.add(scene0Button);

		// Szene 1
		JButton scene1Button = new JButton("(C)hristbaum");
		scene1Button.setPreferredSize(buttonSize);
		scene1Button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sceneSelect.setScene(1);
			}
		});
		sceneControls.add(scene1Button);

		// Szene 2
		JButton scene2Button = new JButton("G(eschenk)");
		scene2Button.setPreferredSize(buttonSize);
		scene2Button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sceneSelect.setScene(2);
			}
		});
		sceneControls.add(scene2Button);

		// Szene 3
		JButton scene3Button = new JButton("(L)iebe");
		scene3Button.setPreferredSize(buttonSize);
		scene3Button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sceneSelect.setScene(3);
			}
		});
		sceneControls.add(scene3Button);

		// Szene 4
		JButton scene4Button = new JButton("(W)eihnachtsmann");
		scene4Button.setPreferredSize(buttonSize);
		scene4Button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sceneSelect.setScene(4);
			}
		});
		sceneControls.add(scene4Button);

		controls.add(sceneControls);

		return controls;
	}

	/**
	 * Erstellt die Controls für die Steuerungselemente in der OpenGL-Ansicht.
	 * Fügt "Zurück" Button zum Zurückkehren in die Webcam-Ansicht hinzu.
	 * @return	Controls für die OpenGL-Ansicht
	 */
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

	/**
	 * Fügt ein JLabel hinzu welches das Bild von der Webcam anzeigt.
	 * In setWebcamIcon() wird das Bild mit dem aktuellen Frame geupdated.
	 * @return	JPanel mit dem webcamLabel als Inhalt
	 */
	private JPanel getContentWebcam() {
		JPanel content = new JPanel();

		webcamLabel = new JLabel();
		content.add(webcamLabel);

		return content;
	}

	/**
	 * Fügt einen GLCnavas von StartRenderer hinzu welcher die 3D-Szene anzeigt und startet den FPSAnimator.
	 * Jedes mal, wenn der Nutzer eine andere Szene auswählt, bzw. eine andere Geste zeigt, wird der
	 * renderCanvas neu erstellt mit der jeweiligen Szene als Eingabeparameter.
	 * @param scene	Szene die angezeigt werden soll
	 * @return		JPanel mit dem renderCanvas als Inhalt
	 */
	private JPanel getContentScene(int scene) {
		JPanel content = new JPanel();

		renderCanvas = new StartRenderer(scene);

		// Erstellt ein Animator-Objekt für den GLCanvas mit der angegebenen Framerate.
		animator = new FPSAnimator(renderCanvas, FRAME_RATE, true);
		animator.start();

		renderCanvas.setSize(new Dimension(CONTENT_WIDTH, CONTENT_HEIGHT));
		renderCanvas.requestFocusInWindow();

		content.add(renderCanvas);

		return content;
	}

	/**
	 * Ändert contentPanel zu dem Inhalt von getContentWebcam und stoppt den
	 * Animator wenn dieser aktiv ist.
	 */
	public void setContentWebcam() {
		if (animator != null && animator.isAnimating())
			animator.stop();
		contentPanel.removeAll();

		JPanel content = getContentWebcam();
		contentPanel.add(content);

		contentPanel.revalidate();
		contentPanel.repaint();
	}

	/**
	 * Ändert contentPanel zu dem Inhalt von getContentScene mit der jeweiligen 3D-Szene.
	 * @param scene	Szene die angezeigt werden soll
	 */
	public void setContentScene(int scene) {
		contentPanel.removeAll();
		JPanel content = getContentScene(scene);
		contentPanel.add(content);

		contentPanel.revalidate();
		contentPanel.repaint();
	}

	/**
	 * Ändert controlsPanel zu dem Inhalt von getControlsWebcam.
	 */
	public void setControlsWebcam() {
		controlsPanel.removeAll();
		JPanel controls = getControlsWebcam();
		controlsPanel.add(controls);

		controlsPanel.revalidate();
		controlsPanel.repaint();
	}

	/**
	 * Ändert controlsPanel zu dem Inhalt von getControlsScene.
	 */
	public void setControlsScene() {
		controlsPanel.removeAll();
		JPanel controls = getControlsScene();
		controlsPanel.add(controls);

		controlsPanel.revalidate();
		controlsPanel.repaint();
	}

	/**
	 * Ändert das webcamLabel zu dem aktuellen Frame wenn die Webcam aktiv ist, oder
	 * zu dem Bild das der Nutzer hochlädt.
	 * @param image	Das Bild, das angezeigt werden soll
	 */
	public void setWebcamIcon(Image image) {
		Image imageResized = Utils.resizeImage(image, CONTENT_WIDTH, CONTENT_HEIGHT);
		webcamLabel.setIcon(new ImageIcon(imageResized));
	}

	/**
	 * Ändert den Text von captureButton zu "Aufnahme beginnen" / "Aufnahme beenden", je nachdem
	 * ob die Webcam gerade aufnimmt oder nicht.
	 * @param text	Text für den captureButton
	 */
	public void setWebcamText(String text) {
		captureButton.setText(text);
	}

	/**
	 * Setzt currentFrame und currentFrameBefore zu dem aktuellen Bild, das für die Szenen-
	 * Auswahl verwendet werden soll. Nimmt die Webcam auf, wird der letzte Frame verwendet,
	 * lädt der Nutzer ein Bild hoch, wird dieses verwendet.
	 * @param imageProcessed	Verarbeitetes Bild
	 * @param imageBefore		Unverarbeitetes Bild
	 */
	public void setCurrentFrame(Image imageProcessed, Image imageBefore) {
		this.currentFrame = imageProcessed;
		this.currentFrameBefore = imageBefore;
	}

	/**
	 * Klickt der Nutzer auf "Weiter" wird currentFrame an sceneSelect übergeben für die
	 * Auswahl der 3D-Szene.
	 */
	private void continueWithFrame() {
		if (currentFrame != null) {
			sceneSelect.selectScene(currentFrame);
			// Wenn isDebug true ist, wird das Vorher/Nachher-Bild als jpg gespeichert.
			if (isDebug) {
				Utils.writeFile(currentFrame, "Debug_WebcamOutput_After");
				Utils.writeFile(currentFrameBefore, "Debug_WebcamOutput_Before");
			}
		}
	}

}
