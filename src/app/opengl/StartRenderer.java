package app.opengl;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.PMVMatrix;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.hshl.obj.loader.OBJLoader;
import de.hshl.obj.loader.Resource;
import de.hshl.obj.loader.objects.Mesh;

public class StartRenderer extends GLCanvas implements GLEventListener {
	private String modelPath = ".\\resources\\models\\";
	private Model model;

	// OpenGL buffer names for data allocation and handling on GPU
	private int[] vaoName;
	private int[] vboName;
	private int[] iboName;

	// Declaration of an object for handling keyboard and mouse interactions
	private InteractionHandler interactionHandler;

	// Declaration for using the projection-model-view matrix tool
	private PMVMatrix pmvMatrix;

	private GL3 gl;

	// The Object that gets displayed
	private int activeObject;
	// List of Object paths pointing to the .obj file
	private Path[] objectPaths = { Paths.get(modelPath + "suzanne.obj"), Paths.get(modelPath + "heart.obj") };
	// List of Objects with the length of objectPaths
	private Model[] objectList = new Model[objectPaths.length];

	public StartRenderer(int activeObject) {
		// Create the OpenGL canvas with default capabilities
		super();

		// Add this object as OpenGL event listener to the canvas
		this.addGLEventListener(this);
		createAndRegisterInteractionHandler();

		// Set active Object
		this.activeObject = activeObject;
	}

	private void createAndRegisterInteractionHandler() {
		interactionHandler = new InteractionHandler();
		this.addKeyListener(interactionHandler);
		this.addMouseListener(interactionHandler);
		this.addMouseMotionListener(interactionHandler);
		this.addMouseWheelListener(interactionHandler);
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		// Get the OpenGL graphics context
		gl = drawable.getGL().getGL3();

		// Outputs information about the available and chosen profile
		System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
		System.err.println("GL_VENDOR: " + gl.glGetString(GL.GL_VENDOR));
		System.err.println("GL_RENDERER: " + gl.glGetString(GL.GL_RENDERER));
		System.err.println("GL_VERSION: " + gl.glGetString(GL.GL_VERSION));

		// BEGIN: Preparing scene
		// BEGIN: Allocating vertex array objects and buffers for each object
		int noOfObjects = 1;

		// Create the Vertex Array Object on the GPU
		vaoName = new int[noOfObjects];
		gl.glGenVertexArrays(noOfObjects, vaoName, 0);
		if (vaoName[0] < 1)
			System.err.println("Error allocating vertex array object (VAO).");

		// Creating the Vertex Buffer Objects on the GPU
		vboName = new int[noOfObjects];
		gl.glGenBuffers(noOfObjects, vboName, 0);
		if (vboName[0] < 1)
			System.err.println("Error allocating vertex buffer object (VBO).");

		// Creating the Index Buffer Objects on the GPU
		iboName = new int[noOfObjects];
		gl.glGenBuffers(noOfObjects, iboName, 0);
		if (iboName[0] < 1)
			System.err.println("Error allocating index buffer object.");
		// END: Allocating vertex array objects and buffers for each object

		// Load activeObject as Model
		loadModel(gl);
		// END: Preparing scene

		// Enable alpha transparency
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

		// Switch on back face culling
		gl.glEnable(GL.GL_CULL_FACE);
		gl.glCullFace(GL.GL_BACK);

		// Switch on depth test.
		gl.glEnable(GL.GL_DEPTH_TEST);

		// Set background color of the GLCanvas.
		gl.glClearColor(0.2f, 0.2f, 0.2f, 1.0f);

		// Create object for projection-model-view matrix calculation.
		pmvMatrix = new PMVMatrix();

		// Set start parameter(s) for the interaction handler.
		interactionHandler.setEyeZ(2);
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		// Clear color and depth buffer
		gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

		// Apply view transform using the PMV-Tool
		// Camera positioning is steered by the interaction handler
		pmvMatrix.glLoadIdentity();
		pmvMatrix.gluLookAt(0f, 0f, interactionHandler.getEyeZ(), 0f, 0f, 0f, 0f, 1.0f, 0f);
		pmvMatrix.glTranslatef(interactionHandler.getxPosition(), interactionHandler.getyPosition(), 0f);
		pmvMatrix.glRotatef(interactionHandler.getAngleXaxis(), 1f, 0f, 0f);
		pmvMatrix.glRotatef(interactionHandler.getAngleYaxis(), 0f, 1f, 0f);

		// Transfer the PVM-Matrix (model-view and projection matrix) to the GPU
		// via uniforms
		// Transfer projection matrix via uniform layout position 0
		gl.glUniformMatrix4fv(0, 1, false, pmvMatrix.glGetPMatrixf());
		// Transfer model-view matrix via layout position 1
		gl.glUniformMatrix4fv(1, 1, false, pmvMatrix.glGetMvMatrixf());

		pmvMatrix.glPushMatrix();
		pmvMatrix.glTranslatef(0f, 0f, 0f);
		displayModel(gl);
		pmvMatrix.glPopMatrix();
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		// Set the viewport width and height to the entire window
		gl.glViewport(0, 0, width, height);

		// Switch the pmv-tool to perspective projection
		pmvMatrix.glMatrixMode(PMVMatrix.GL_PROJECTION);
		// Reset projection matrix to identity
		pmvMatrix.glLoadIdentity();
		// Calculate projection matrix
		//      Parameters:
		//          fovy (field of view), aspect ratio,
		//          zNear (near clipping plane), zFar (far clipping plane)
		pmvMatrix.gluPerspective(45f, (float) width / (float) height, 0.1f, 10000f);
		// Switch to model-view transform
		pmvMatrix.glMatrixMode(PMVMatrix.GL_MODELVIEW);
	}

	// Called by the drawable before the OpenGL context is destroyed by an external event
	@Override
	public void dispose(GLAutoDrawable drawable) {
		// Detach and delete shader program
		gl.glUseProgram(0);
		model.deleteShaderProgram();

		// Deactivate VAO and VBO
		gl.glBindVertexArray(0);
		gl.glDisableVertexAttribArray(0);
		gl.glDisableVertexAttribArray(1);
		gl.glDeleteVertexArrays(1, vaoName, 0);
		gl.glDeleteBuffers(1, vboName, 0);
	}

	private void loadModel(GL3 gl) {
		try {
			// Create new Model from Mesh data
			Mesh mesh = new OBJLoader()
					.setLoadNormals(true)
					.setGenerateIndexedMeshes(true)
					.loadMesh(Resource.file(objectPaths[activeObject]));
			model = new Model(gl, mesh.getVertices(), mesh.getIndices());

			// Create sphere data for rendering a sphere using an index array into a vertex array
			gl.glBindVertexArray(vaoName[0]);

			// Activate and initialize vertex buffer object (VBO)
			gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboName[0]);
			gl.glBufferData(GL.GL_ARRAY_BUFFER, model.getVertices().length * Float.BYTES,
					FloatBuffer.wrap(model.getVertices()), GL.GL_STATIC_DRAW);

			// Activate and initialize index buffer object (IBO)
			gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, iboName[0]);
			gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, model.getIndices().length * Integer.BYTES,
					IntBuffer.wrap(model.getIndices()), GL.GL_STATIC_DRAW);

			// Activate and order vertex buffer object data for the vertex shader
			// Defining input variables for vertex shader
			// Pointer for the vertex shader to the position information per vertex
			gl.glEnableVertexAttribArray(0);
			gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 6 * Float.BYTES, 0);
			// Pointer for the vertex shader to the color information per vertex
			gl.glEnableVertexAttribArray(1);
			gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void displayModel(GL3 gl) {
		gl.glUseProgram(model.getShaderProgramID());
		// Transfer the PVM-Matrix (model-view and projection matrix to the vertex shader
		gl.glUniformMatrix4fv(0, 1, false, pmvMatrix.glGetPMatrixf());
		gl.glUniformMatrix4fv(1, 1, false, pmvMatrix.glGetMvMatrixf());
		gl.glBindVertexArray(vaoName[0]);
		// Draws the elements in the order defined by the index buffer object (IBO)
		gl.glDrawElements(GL.GL_TRIANGLES, model.getIndices().length, GL.GL_UNSIGNED_INT, 0);
	}
}
