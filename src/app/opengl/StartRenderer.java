package app.opengl;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.PMVMatrix;

import app.opengl.primitives.*;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.hshl.obj.loader.OBJLoader;
import de.hshl.obj.loader.Resource;
import de.hshl.obj.loader.objects.Mesh;

/**
 * Zuständig für das OpenGL Rendering mit der Programmable-Pipeline.
 * 
 * Zeichnet eins der .obj Modelle im models Ordner und erstellt aus primitiver Geometrie
 * eine 3D-Szene.
 * Das Objekt, welches über den OBJLoader geladen wird hat den Animation-Shader, die restliche
 * Geometrie benutzt den Basic-Shader.
 * 
 * Der Code basiert auf den Beispielen auf Moodle von Karsten Lehn, wurde allerdings stark abgeändert
 * und überarbeitet.
 */
public class StartRenderer extends GLCanvas implements GLEventListener {
	// Pfad zu dem Verzeichnis mit den .obj Modellen
	private String modelPath = ".\\resources\\models\\";
	// Das Objekt das vom OBJLoader geladen werden soll
	private int activeObject;
	// Liste an .obj Dateien im Modellverzeichnis
	private Path[] objectPaths = {
			Paths.get(modelPath + "apple.obj"),
			Paths.get(modelPath + "tree.obj"),
			Paths.get(modelPath + "present.obj"),
			Paths.get(modelPath + "heart.obj"),
			Paths.get(modelPath + "santa.obj"),
	};

	// Liste an Modellen
	private Model[] models = new Model[25];

	// Die OpenGL Buffer für Datenbelegung auf der GPU
	private int[] vaoName;
	private int[] vboName;
	private int[] iboName;

	// Zuständig für die Interaktion mit Maus und Tastatur
	private InteractionHandler interactionHandler;

	// Deklaration der projection-model-view Matrix
	private PMVMatrix pmvMatrix;

	// Shader
	private ShaderProgram shaderProgram;
	private int shaderProgramBasicId, shaderProgramAnimationId;
	private String shaderPath = ".\\resources\\shaders\\";
	private String vertexShaderFileNameBasic = "Basic.vert";
	private String fragmentShaderFileNameBasic = "Basic.frag";
	private String vertexShaderFileNameAniamtion = "Animation.vert";
	private String fragmentShaderFileNameAnimation = "Animation.frag";

	// Animation
	private int elapsedTimeUniform;
	private long startingTime;

	/**
	 * Konstruktor erstellt den OpenGL Canvas und fügt sich als Event Listener zum Canvas hinzu.
	 * @param activeObject	Das Objekt welches vom OBJLoader geladen werden soll
	 */
	public StartRenderer(int activeObject) {
		// Erstellt den OpenGL Canvas mit den standard Capabilities
		super();

		// Fügt dieses Object als OpenGL Event Listener zum Canvas hinzu
		this.addGLEventListener(this);
		// Erstellt und registriert den InteractionHandler
		createAndRegisterInteractionHandler();

		// Initialisiert das activeObject
		this.activeObject = activeObject;
	}

	/**
	 * Fügt Listener für Maus und Tastatur zum interactionHandler hinzu.
	 */
	private void createAndRegisterInteractionHandler() {
		interactionHandler = new InteractionHandler();
		this.addKeyListener(interactionHandler);
		this.addMouseListener(interactionHandler);
		this.addMouseMotionListener(interactionHandler);
		this.addMouseWheelListener(interactionHandler);
	}

	/**
	 * Wird aufgerufen wenn der OpenGL Renderer zum ersten mal gestartet wird.
	 * @param drawable	OpenGL drawable
	 */
	@Override
	public void init(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3();

		// Erstellt Basic ShaderProgram
		shaderProgram = new ShaderProgram(gl);
		ShaderProgram.loadShaderAndCreateProgram(
				shaderPath,
				vertexShaderFileNameBasic,
				fragmentShaderFileNameBasic);
		shaderProgramBasicId = shaderProgram.getShaderProgramID();

		// Erstellt Animation ShaderProgram
		shaderProgram = new ShaderProgram(gl);
		ShaderProgram.loadShaderAndCreateProgram(
				shaderPath,
				vertexShaderFileNameAniamtion,
				fragmentShaderFileNameAnimation);
		shaderProgramAnimationId = shaderProgram.getShaderProgramID();

		// Zeigt Information über das gewählte Profil an
		/*
		System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
		System.err.println("GL_VENDOR: " + gl.glGetString(GL.GL_VENDOR));
		System.err.println("GL_RENDERER: " + gl.glGetString(GL.GL_RENDERER));
		System.err.println("GL_VERSION: " + gl.glGetString(GL.GL_VERSION));
		*/

		// Erstellt das Vertex Array Objekt auf der GPU
		vaoName = new int[models.length];
		gl.glGenVertexArrays(models.length, vaoName, 0);
		if (vaoName[0] < 1)
			System.err.println("Error allocating vertex array object (VAO).");

		// Erstellt das Vertex Buffer Objekt auf der GPU
		vboName = new int[models.length];
		gl.glGenBuffers(models.length, vboName, 0);
		if (vboName[0] < 1)
			System.err.println("Error allocating vertex buffer object (VBO).");

		// Erstellt das Index Buffer Objekt auf der GPU
		iboName = new int[models.length];
		gl.glGenBuffers(models.length, iboName, 0);
		if (iboName[0] < 1)
			System.err.println("Error allocating index buffer object.");

		// Erstellt die Modelle
		createModels(gl);

		// Aktiviert Transparenz vom Alpha Kanal
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

		// Aktiviert Backface-Culling
		gl.glEnable(GL.GL_CULL_FACE);
		gl.glCullFace(GL.GL_BACK);

		// Aktiviert den Depth Test
		gl.glEnable(GL.GL_DEPTH_TEST);

		// Setzt die Hintergrundfarbe vom OpenGL Canvas
		gl.glClearColor(0.933f, 0.933f, 0.933f, 1.0f);

		// Erstellt das Objekt zur Berechnung der projection-model-view Matrix.
		pmvMatrix = new PMVMatrix();

		// Setzt die Position der Kamera
		interactionHandler.setEyeZ(10.0f);
		interactionHandler.setyPosition(-1.5f);

		// Kommunikation mit dem Animation-Shader für die Animation
		elapsedTimeUniform = gl.glGetUniformLocation(shaderProgramAnimationId, "time");
		int loopDurationUnf = gl.glGetUniformLocation(shaderProgramAnimationId, "loopDuration");

		// Übergibt die Länge einer Animations-Schleife an den Shader
		gl.glUseProgram(shaderProgramAnimationId);
		gl.glUniform1f(loopDurationUnf, 5f);
		gl.glUseProgram(0);

		// Zeitpunkt zu dem der OpenGL Renderer gestartet wurde für die Berechnung der vergangenden Zeit
		startingTime = System.currentTimeMillis();
	}

	/**
	 * Wird vom OpenGL Animator jeden Frame aufgerufen.
	 * @param drawable	OpenGL drawable
	 */
	@Override
	public void display(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3();

		// Löscht den Color und Depth Buffer
		gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

		// Wendet die Transformation von der pmvMatrix an
		// Die Position der Kamera wird durch den Interaction Handler gesteuert
		pmvMatrix.glLoadIdentity();
		pmvMatrix.gluLookAt(0f, 0f, interactionHandler.getEyeZ(), 0f, 0f, 0f, 0f, 1.0f, 0f);
		pmvMatrix.glTranslatef(interactionHandler.getxPosition(), interactionHandler.getyPosition(), 0f);
		pmvMatrix.glRotatef(interactionHandler.getAngleXaxis(), 1f, 0f, 0f);
		pmvMatrix.glRotatef(interactionHandler.getAngleYaxis(), 0f, 1f, 0f);

		// Zeigt die erstellten Modelle an
		for (int i = 0; i < models.length; i++) {
			if (models[i] != null) {
				pmvMatrix.glPushMatrix();
				// Position
				pmvMatrix.glTranslatef(models[i].getPosX(), models[i].getPosY(), models[i].getPosZ());
				// Rotation
				pmvMatrix.glRotatef(models[i].getAng(), models[i].getRotX(), models[i].getRotY(), models[i].getRotZ());
				// Anzeigen
				displayModel(gl, i, models[i].getMode());
				pmvMatrix.glPopMatrix();
			}
		}

		// Übergibt die vergangende Zeit an den Animation-Shader
		gl.glUseProgram(shaderProgramAnimationId);
		gl.glUniform1f(elapsedTimeUniform, (System.currentTimeMillis() - startingTime) / 1000.0f);
		gl.glUseProgram(0);
	}

	/**
	 * Wird aufgerufen wenn das OpenGL Fenster skaliert wird.
	 * @param drawable	OpenGL drawable
	 * @param x			Viewport x-Koordinate in Pixeln
	 * @param y			Viewport y-Koordinate in Pixeln
	 * @param width		Viewport Breite in Pixeln
	 * @param height	Viewport Höhe in Pixeln
	 */
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		GL3 gl = drawable.getGL().getGL3();

		// Setzt die Breite und Höhe vom Viewport
		gl.glViewport(0, 0, width, height);

		// Ändert die pmvMatrix zur perspektivischen Projektion
		pmvMatrix.glMatrixMode(PMVMatrix.GL_PROJECTION);
		// Setzt die Projection Matrix zur Identity zurück
		pmvMatrix.glLoadIdentity();
		// Berechnet die Projection Matrix mit der neuen Größe
		pmvMatrix.gluPerspective(45f, (float) width / (float) height, 0.1f, 10000f);
		// Ändert die pmvMatrix zur modelview Transformation
		pmvMatrix.glMatrixMode(PMVMatrix.GL_MODELVIEW);
	}

	/**
	 * Wird aufgerufen wenn der OpenGL Canvas entfernt wird.
	 * @param drawable	OpenGL drawable
	 */
	@Override
	public void dispose(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3();

		// Löscht die Shader Programme
		gl.glUseProgram(0);
		gl.glDeleteProgram(shaderProgramBasicId);
		gl.glDeleteProgram(shaderProgramAnimationId);

		// Deaktiviert die VAOs und VBOs
		gl.glBindVertexArray(0);
		gl.glDisableVertexAttribArray(0);
		gl.glDisableVertexAttribArray(1);
		gl.glDeleteVertexArrays(1, vaoName, 0);
		gl.glDeleteBuffers(1, vboName, 0);
	}

	/**
	 * Erstellt die Modelle auf der GPU mit den Vertices und Indices aus dem übergebenen Modell an dem index.
	 * Modelle aus dem OBJLoader haben einen Stride von 6, da die Vertices keine Farbinformationen besitzen.
	 * Primitve Geometrie haben einen Stride von 9, für die Position, Normals und Farbinformationen.
	 * @param gl		OpenGL Graphics Context
	 * @param model		Modell mit Vertices und Indices
	 * @param index		Index im VAO, VBO und IBO Array
	 * @param stride	Anzahl der Informationen im Vertex Array (x,y,z;nx,ny,nz;r,g,b)
	 */
	private void loadModel(GL3 gl, Model model, int index, int stride) {
		if (index > models.length)
			System.err.println("Index is bigger than the allocated number of objects.");

		// Wechsel zu diesem VAO
		gl.glBindVertexArray(vaoName[index]);

		// Aktiviere und initialisiere das VBO
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboName[index]);
		gl.glBufferData(GL.GL_ARRAY_BUFFER, model.getVertices().length * Float.BYTES,
				FloatBuffer.wrap(model.getVertices()), GL.GL_STATIC_DRAW);

		// Aktiviere und initialisiere das IBO
		gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, iboName[index]);
		gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, model.getIndices().length * Integer.BYTES,
				IntBuffer.wrap(model.getIndices()), GL.GL_STATIC_DRAW);

		// Vertex Shader Position für jeden Vertex (x, y, z)
		if (stride >= 3) {
			gl.glEnableVertexAttribArray(0);
			gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, stride * Float.BYTES, 0 * Float.BYTES);
		}
		// Vertex Shader Normal für jeden Vertex (nx, ny, nz)
		if (stride >= 6) {
			gl.glEnableVertexAttribArray(1);
			gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, stride * Float.BYTES, 3 * Float.BYTES);
		}
		// Vertex Shader Farbe für jeden vertex (r, g, b)
		if (stride >= 9) {
			gl.glEnableVertexAttribArray(2);
			gl.glVertexAttribPointer(2, 3, GL.GL_FLOAT, false, stride * Float.BYTES, 6 * Float.BYTES);
		}
	}

	/**
	 * Aktiviert das Shader Programm mit dem entsprechenden index.
	 * Modelle aus dem OBJLoader benutzen GL.GL_TRIANGLES als Mode.
	 * Primitve Geometrie benutzt GL.GL_TRIANGLE_STRIP als Mode.
	 * @param gl	OpenGL Graphics Context
	 * @param index	Index im VAO Array
	 * @param mode	GL_TRIANGLES oder GL_TRIANGLE_STRIP
	 */
	private void displayModel(GL3 gl, int index, int mode) {
		gl.glUseProgram(models[index].getShaderProgramId());

		// Transformiert die Projection-Matix and der uniform layout position 0
		gl.glUniformMatrix4fv(0, 1, false, pmvMatrix.glGetPMatrixf());
		// Transformiert die Modelview-Matix and der uniform layout position 1
		gl.glUniformMatrix4fv(1, 1, false, pmvMatrix.glGetMvMatrixf());
		// Wechsel zu diesem VAO
		gl.glBindVertexArray(vaoName[index]);
		// Zeichnet das Modell mit dem angegebenen Modus
		gl.glDrawElements(mode, models[index].getIndices().length, GL.GL_UNSIGNED_INT, 0);

		gl.glUseProgram(0);
	}

	/**
	 * Erstellt neue Modelle und speichert diese im Models Array.
	 * Das erste Modell wird vom OBJLoader geladen und bekommt den Animation-Shader.
	 * Die restlichen Modelle werden primitiv erzeugt und bekommen den Basic-Shader.
	 * @param gl	OpenGL Graphics Context
	 */
	private void createModels(GL3 gl) {
		int index = 0;

		// Erstellt ein neues Modell aus dem Mesh
		try {
			OBJLoader loader = new OBJLoader();
			loader.setLoadNormals(true); // Normals werden mit geladen
			loader.setLoadTextureCoordinates(false);
			loader.setGenerateIndexedMeshes(true);

			// Erzeugt Mesh aus ausgelesener Datei
			Mesh mesh = loader.loadMesh(Resource.file(objectPaths[activeObject]));
			models[index] = new Model(gl, shaderProgramAnimationId, mesh.getVertices(), mesh.getIndices(),
					GL.GL_TRIANGLES);
			models[index].setPos(0.0f, 1.5f, 0.0f);

			loadModel(gl, models[index], index, 6);
			index++;
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Primitives benutzen TRIANGLE_STIP für schnelleres Zeichnen
		int mode = GL.GL_TRIANGLE_STRIP;

		// Farben
		float[] colorGround = { 0.1f, 0.5f, 0.1f };
		float[] colorGreen = { 0.1f, 0.6f, 0.1f };
		float[] colorBrown = { 0.6f, 0.4f, 0.2f };
		float[] colorRed = { 0.8f, 0.1f, 0.1f };
		float[] colorBlue = { 0.1f, 0.1f, 0.8f };

		// Boden
		models[index] = new Cone(gl, shaderProgramBasicId, mode, 32, 5.0f, 5.0f, 0.1f, colorGround);
		models[index].setPos(0.0f, 0.0f, 0.0f);
		loadModel(gl, models[index], index, 9);
		index++;

		// Baum 1 Stamm
		models[index] = new Cone(gl, shaderProgramBasicId, mode, 32, 0.3f, 0.3f, 1.0f, colorBrown);
		models[index].setPos(-3.0f, 0.5f, -1.0f);
		loadModel(gl, models[index], index, 9);
		index++;
		// Baum 1 Blätter Unten
		models[index] = new Cone(gl, shaderProgramBasicId, mode, 32, 0.5f, 1.3f, 1.0f, colorGreen);
		models[index].setPos(-3.0f, 1.5f, -1.0f);
		loadModel(gl, models[index], index, 9);
		index++;
		// Baum 1 Blätter Mitte
		models[index] = new Cone(gl, shaderProgramBasicId, mode, 32, 0.2f, 1.0f, 1.0f, colorGreen);
		models[index].setPos(-3.0f, 2.5f, -1.0f);
		loadModel(gl, models[index], index, 9);
		index++;
		// Baum 1 Blätter Oben
		models[index] = new Cone(gl, shaderProgramBasicId, mode, 32, 0.01f, 0.7f, 1.0f, colorGreen);
		models[index].setPos(-3.0f, 3.5f, -1.0f);
		loadModel(gl, models[index], index, 9);
		index++;

		// Baum 2 Stamm
		models[index] = new Cone(gl, shaderProgramBasicId, mode, 32, 0.3f, 0.3f, 1.2f, colorBrown);
		models[index].setPos(3.2f, 0.6f, -0.8f);
		loadModel(gl, models[index], index, 9);
		index++;
		// Baum 2 Blätter Unten
		models[index] = new Cone(gl, shaderProgramBasicId, mode, 32, 0.5f, 1.3f, 1.0f, colorGreen);
		models[index].setPos(3.2f, 1.6f, -0.8f);
		loadModel(gl, models[index], index, 9);
		index++;
		// Baum 2 Blätter Mitte
		models[index] = new Cone(gl, shaderProgramBasicId, mode, 32, 0.2f, 1.0f, 1.0f, colorGreen);
		models[index].setPos(3.2f, 2.6f, -0.8f);
		loadModel(gl, models[index], index, 9);
		index++;
		// Baum 2 Blätter Oben
		models[index] = new Cone(gl, shaderProgramBasicId, mode, 32, 0.01f, 0.7f, 1.0f, colorGreen);
		models[index].setPos(3.2f, 3.6f, -0.8f);
		loadModel(gl, models[index], index, 9);
		index++;

		// Baum 3 Stamm
		models[index] = new Cone(gl, shaderProgramBasicId, mode, 32, 0.3f, 0.3f, 1.2f, colorBrown);
		models[index].setPos(0.2f, 0.6f, -3.8f);
		loadModel(gl, models[index], index, 9);
		index++;
		// Baum 3 Blätter Unten
		models[index] = new Cone(gl, shaderProgramBasicId, mode, 32, 0.5f, 1.3f, 1.0f, colorGreen);
		models[index].setPos(0.2f, 1.6f, -3.8f);
		loadModel(gl, models[index], index, 9);
		index++;
		// Baum 3 Blätter Mitte
		models[index] = new Cone(gl, shaderProgramBasicId, mode, 32, 0.2f, 1.0f, 1.0f, colorGreen);
		models[index].setPos(0.2f, 2.6f, -3.8f);
		loadModel(gl, models[index], index, 9);
		index++;
		// Baum 3 Blätter Oben
		models[index] = new Cone(gl, shaderProgramBasicId, mode, 32, 0.01f, 0.7f, 1.0f, colorGreen);
		models[index].setPos(0.2f, 3.6f, -3.8f);
		loadModel(gl, models[index], index, 9);
		index++;

		// Baum 3 Stamm
		models[index] = new Cone(gl, shaderProgramBasicId, mode, 32, 0.2f, 0.2f, 0.6f, colorBrown);
		models[index].setPos(-1.5f, 0.3f, -2.5f);
		loadModel(gl, models[index], index, 9);
		index++;
		// Baum 3 Blätter Unten
		models[index] = new Cone(gl, shaderProgramBasicId, mode, 32, 0.4f, 0.8f, 0.5f, colorGreen);
		models[index].setPos(-1.5f, 0.8f, -2.5f);
		loadModel(gl, models[index], index, 9);
		index++;
		// Baum 3 Blätter Mitte
		models[index] = new Cone(gl, shaderProgramBasicId, mode, 32, 0.2f, 0.6f, 0.5f, colorGreen);
		models[index].setPos(-1.5f, 1.3f, -2.5f);
		loadModel(gl, models[index], index, 9);
		index++;
		// Baum 3 Blätter Oben
		models[index] = new Cone(gl, shaderProgramBasicId, mode, 32, 0.01f, 0.3f, 0.5f, colorGreen);
		models[index].setPos(-1.5f, 1.8f, -2.5f);
		loadModel(gl, models[index], index, 9);
		index++;

		// Baum 4 Stamm
		models[index] = new Cone(gl, shaderProgramBasicId, mode, 32, 0.2f, 0.2f, 0.8f, colorBrown);
		models[index].setPos(1.8f, 0.4f, -2.7f);
		loadModel(gl, models[index], index, 9);
		index++;
		// Baum 4 Blätter Unten
		models[index] = new Cone(gl, shaderProgramBasicId, mode, 32, 0.4f, 0.8f, 0.5f, colorGreen);
		models[index].setPos(1.8f, 0.9f, -2.7f);
		loadModel(gl, models[index], index, 9);
		index++;
		// Baum 4 Blätter Mitte
		models[index] = new Cone(gl, shaderProgramBasicId, mode, 32, 0.2f, 0.6f, 0.5f, colorGreen);
		models[index].setPos(1.8f, 1.4f, -2.7f);
		loadModel(gl, models[index], index, 9);
		index++;
		// Baum 4 Blätter Oben
		models[index] = new Cone(gl, shaderProgramBasicId, mode, 32, 0.01f, 0.3f, 0.5f, colorGreen);
		models[index].setPos(1.8f, 1.9f, -2.7f);
		loadModel(gl, models[index], index, 9);
		index++;

		// Present 1
		models[index] = new Box(gl, shaderProgramBasicId, mode, 0.8f, 0.8f, 0.8f, colorRed);
		models[index].setPos(2.0f, 0.4f, -1.0f);
		models[index].setRot(30, 0, 1, 0);
		loadModel(gl, models[index], index, 9);
		index++;

		// Present 2
		models[index] = new Box(gl, shaderProgramBasicId, mode, 0.6f, 0.6f, 0.6f, colorBlue);
		models[index].setPos(2.3f, 0.3f, -0.2f);
		models[index].setRot(40, 0, 1, 0);
		loadModel(gl, models[index], index, 9);
		index++;

		// Present 3
		models[index] = new Box(gl, shaderProgramBasicId, mode, 0.6f, 0.6f, 0.6f, colorRed);
		models[index].setPos(-2.3f, 0.3f, -0.2f);
		models[index].setRot(70, 0, 1, 0);
		loadModel(gl, models[index], index, 9);
		index++;
	}
}
