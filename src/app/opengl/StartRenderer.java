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

public class StartRenderer extends GLCanvas implements GLEventListener {
	// Path to the models directory
	private String modelPath = ".\\resources\\models\\";
	// The Object that gets displayed
	private int activeObject;
	// List of Object paths pointing to the .obj file
	private Path[] objectPaths = {
			Paths.get(modelPath + "suzanne.obj"),
			Paths.get(modelPath + "heart.obj")
	};

	private Model[] models = new Model[50];

	// OpenGL buffer names for data allocation and handling on GPU
	private int[] vaoName;
	private int[] vboName;
	private int[] iboName;

	// Declaration of an object for handling keyboard and mouse interactions
	private InteractionHandler interactionHandler;

	// Declaration for using the projection-model-view matrix tool
	private PMVMatrix pmvMatrix;

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
		GL3 gl = drawable.getGL().getGL3();

		// Outputs information about the available and chosen profile
		/*
		System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
		System.err.println("GL_VENDOR: " + gl.glGetString(GL.GL_VENDOR));
		System.err.println("GL_RENDERER: " + gl.glGetString(GL.GL_RENDERER));
		System.err.println("GL_VERSION: " + gl.glGetString(GL.GL_VERSION));
		*/

		// BEGIN: Preparing scene
		// BEGIN: Allocating vertex array objects and buffers for each object
		// Create the Vertex Array Object on the GPU
		vaoName = new int[models.length];
		gl.glGenVertexArrays(models.length, vaoName, 0);
		if (vaoName[0] < 1)
			System.err.println("Error allocating vertex array object (VAO).");

		// Creating the Vertex Buffer Objects on the GPU
		vboName = new int[models.length];
		gl.glGenBuffers(models.length, vboName, 0);
		if (vboName[0] < 1)
			System.err.println("Error allocating vertex buffer object (VBO).");

		// Creating the Index Buffer Objects on the GPU
		iboName = new int[models.length];
		gl.glGenBuffers(models.length, iboName, 0);
		if (iboName[0] < 1)
			System.err.println("Error allocating index buffer object.");
		// END: Allocating vertex array objects and buffers for each object

		createModels(gl);
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

		// Set camera position
		interactionHandler.setEyeZ(4.0f);
		interactionHandler.setyPosition(-1.5f);
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		// Get the OpenGL graphics context
		GL3 gl = drawable.getGL().getGL3();

		// Clear color and depth buffer
		gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

		// Apply view transform using the PMV-Tool
		// Camera positioning is steered by the interaction handler
		pmvMatrix.glLoadIdentity();
		pmvMatrix.gluLookAt(0f, 0f, interactionHandler.getEyeZ(), 0f, 0f, 0f, 0f, 1.0f, 0f);
		pmvMatrix.glTranslatef(interactionHandler.getxPosition(), interactionHandler.getyPosition(), 0f);
		pmvMatrix.glRotatef(interactionHandler.getAngleXaxis(), 1f, 0f, 0f);
		pmvMatrix.glRotatef(interactionHandler.getAngleYaxis(), 0f, 1f, 0f);

		// Transfer the PVM-Matrix (model-view and projection matrix) to the GPU via uniforms
		// Transfer projection matrix via uniform layout position 0
		gl.glUniformMatrix4fv(0, 1, false, pmvMatrix.glGetPMatrixf());
		// Transfer model-view matrix via layout position 1
		gl.glUniformMatrix4fv(1, 1, false, pmvMatrix.glGetMvMatrixf());

		// Display Geometry
		for (int i = 0; i < models.length; i++) {
			if (models[i] != null) {
				pmvMatrix.glPushMatrix();
				pmvMatrix.glTranslatef(models[i].getPosX(), models[i].getPosY(), models[i].getPosZ());
				pmvMatrix.glRotatef(models[i].getAng(), models[i].getRotX(), models[i].getRotY(), models[i].getRotZ());
				displayModel(gl, i, models[i].getMode());
				pmvMatrix.glPopMatrix();
			}
		}
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		// Get the OpenGL graphics context
		GL3 gl = drawable.getGL().getGL3();

		// Set the viewport width and height to the entire window
		gl.glViewport(0, 0, width, height);

		// Switch the pmv-tool to perspective projection
		pmvMatrix.glMatrixMode(PMVMatrix.GL_PROJECTION);
		// Reset projection matrix to identity
		pmvMatrix.glLoadIdentity();
		// Calculate projection matrix
		pmvMatrix.gluPerspective(45f, (float) width / (float) height, 0.1f, 10000f);
		// Switch to model-view transform
		pmvMatrix.glMatrixMode(PMVMatrix.GL_MODELVIEW);
	}

	// Called by the drawable before the OpenGL context is destroyed by an external event
	@Override
	public void dispose(GLAutoDrawable drawable) {
		// Get the OpenGL graphics context
		GL3 gl = drawable.getGL().getGL3();

		// Detach and delete shader program
		gl.glUseProgram(0);
		for (Model model : models) {
			if (model != null)
				model.deleteShaderProgram();
		}

		// Deactivate VAO and VBO
		gl.glBindVertexArray(0);
		gl.glDisableVertexAttribArray(0);
		gl.glDisableVertexAttribArray(1);
		gl.glDeleteVertexArrays(1, vaoName, 0);
		gl.glDeleteBuffers(1, vboName, 0);
	}

	private void loadModel(GL3 gl, Model model, int index, int stride) {
		if (index > models.length)
			System.err.println("Index is bigger than the allocated number of objects.");

		// Create sphere data for rendering a sphere using an index array into a vertex array
		gl.glBindVertexArray(vaoName[index]);

		// Activate and initialize vertex buffer object (VBO)
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboName[index]);
		gl.glBufferData(GL.GL_ARRAY_BUFFER, model.getVertices().length * Float.BYTES,
				FloatBuffer.wrap(model.getVertices()), GL.GL_STATIC_DRAW);

		// Activate and initialize index buffer object (IBO)
		gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, iboName[index]);
		gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, model.getIndices().length * Integer.BYTES,
				IntBuffer.wrap(model.getIndices()), GL.GL_STATIC_DRAW);

		// Activate and order vertex buffer object data for the vertex shader
		// Defining input variables for vertex shader
		// Pointer for the vertex shader to the position information per vertex
		if (stride >= 3) {
			gl.glEnableVertexAttribArray(0);
			gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, stride * Float.BYTES, 0 * Float.BYTES);
		}
		// Pointer for the vertex shader to the color information per vertex
		if (stride >= 6) {
			gl.glEnableVertexAttribArray(1);
			gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, stride * Float.BYTES, 3 * Float.BYTES);
		}
		// Pointer for the vertex shader to the normal information per vertex
		if (stride >= 9) {
			gl.glEnableVertexAttribArray(2);
			gl.glVertexAttribPointer(2, 3, GL.GL_FLOAT, false, stride * Float.BYTES, 6 * Float.BYTES);
		}
	}

	private void displayModel(GL3 gl, int index, int mode) {
		gl.glUseProgram(models[index].getShaderProgramID());
		// Transfer the PVM-Matrix (model-view and projection matrix to the vertex shader
		gl.glUniformMatrix4fv(0, 1, false, pmvMatrix.glGetPMatrixf());
		gl.glUniformMatrix4fv(1, 1, false, pmvMatrix.glGetMvMatrixf());
		gl.glBindVertexArray(vaoName[index]);
		// Draws the elements in the order defined by the index buffer object (IBO)
		gl.glDrawElements(mode, models[index].getIndices().length, GL.GL_UNSIGNED_INT, 0);
	}

	private void createModels(GL3 gl) {
		int index = 0;

		// Create and load new Model from Mesh data
		try {
			Mesh mesh = new OBJLoader()
					.setLoadNormals(true)
					.setGenerateIndexedMeshes(true)
					.loadMesh(Resource.file(objectPaths[activeObject]));
			models[index] = new Model(gl, mesh.getVertices(), mesh.getIndices(), GL.GL_TRIANGLES);
			models[index].setPos(0.0f, 1.5f, 0.0f);

			// Load activeObject as Model
			loadModel(gl, models[index], index, 6);
			index++;
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Primitives use TRIANGLE_STIP for faster drawing
		int mode = GL.GL_TRIANGLE_STRIP;

		// Define colors
		float[] colorGround = { 0.1f, 0.5f, 0.1f };
		float[] colorGreen = { 0.1f, 0.6f, 0.1f };
		float[] colorBrown = { 0.6f, 0.4f, 0.2f };
		float[] colorRed = { 0.8f, 0.1f, 0.1f };
		float[] colorBlue = { 0.1f, 0.1f, 0.8f };

		// Ground
		models[index] = new Cone(gl, mode, 32, 5.0f, 5.0f, 0.1f, colorGround);
		models[index].setPos(0.0f, 0.0f, 0.0f);
		loadModel(gl, models[index], index, 9);
		index++;

		// Tree 1 Trunk
		models[index] = new Cone(gl, mode, 32, 0.3f, 0.3f, 1.0f, colorBrown);
		models[index].setPos(-3.0f, 0.5f, -1.0f);
		loadModel(gl, models[index], index, 9);
		index++;
		// Tree 1 Leaves Bottom
		models[index] = new Cone(gl, mode, 32, 0.5f, 1.3f, 1.0f, colorGreen);
		models[index].setPos(-3.0f, 1.5f, -1.0f);
		loadModel(gl, models[index], index, 9);
		index++;
		// Tree 1 Leaves Middle
		models[index] = new Cone(gl, mode, 32, 0.2f, 1.0f, 1.0f, colorGreen);
		models[index].setPos(-3.0f, 2.5f, -1.0f);
		loadModel(gl, models[index], index, 9);
		index++;
		// Tree 1 Leaves Top
		models[index] = new Cone(gl, mode, 32, 0.0f, 0.7f, 1.0f, colorGreen);
		models[index].setPos(-3.0f, 3.5f, -1.0f);
		loadModel(gl, models[index], index, 9);
		index++;

		// Tree 2 Trunk
		models[index] = new Cone(gl, mode, 32, 0.3f, 0.3f, 1.2f, colorBrown);
		models[index].setPos(3.2f, 0.6f, -0.8f);
		loadModel(gl, models[index], index, 9);
		index++;
		// Tree 2 Leaves Bottom
		models[index] = new Cone(gl, mode, 32, 0.5f, 1.3f, 1.0f, colorGreen);
		models[index].setPos(3.2f, 1.6f, -0.8f);
		loadModel(gl, models[index], index, 9);
		index++;
		// Tree 2 Leaves Middle
		models[index] = new Cone(gl, mode, 32, 0.2f, 1.0f, 1.0f, colorGreen);
		models[index].setPos(3.2f, 2.6f, -0.8f);
		loadModel(gl, models[index], index, 9);
		index++;
		// Tree 2 Leaves Top
		models[index] = new Cone(gl, mode, 32, 0.0f, 0.7f, 1.0f, colorGreen);
		models[index].setPos(3.2f, 3.6f, -0.8f);
		loadModel(gl, models[index], index, 9);
		index++;

		// Tree 3 Trunk
		models[index] = new Cone(gl, mode, 32, 0.3f, 0.3f, 1.2f, colorBrown);
		models[index].setPos(0.2f, 0.6f, -3.8f);
		loadModel(gl, models[index], index, 9);
		index++;
		// Tree 3 Leaves Bottom
		models[index] = new Cone(gl, mode, 32, 0.5f, 1.3f, 1.0f, colorGreen);
		models[index].setPos(0.2f, 1.6f, -3.8f);
		loadModel(gl, models[index], index, 9);
		index++;
		// Tree 3 Leaves Middle
		models[index] = new Cone(gl, mode, 32, 0.2f, 1.0f, 1.0f, colorGreen);
		models[index].setPos(0.2f, 2.6f, -3.8f);
		loadModel(gl, models[index], index, 9);
		index++;
		// Tree 3 Leaves Top
		models[index] = new Cone(gl, mode, 32, 0.0f, 0.7f, 1.0f, colorGreen);
		models[index].setPos(0.2f, 3.6f, -3.8f);
		loadModel(gl, models[index], index, 9);
		index++;

		// Tree 3 Trunk
		models[index] = new Cone(gl, mode, 32, 0.2f, 0.2f, 0.6f, colorBrown);
		models[index].setPos(-1.5f, 0.3f, -2.5f);
		loadModel(gl, models[index], index, 9);
		index++;
		// Tree 3 Leaves Bottom
		models[index] = new Cone(gl, mode, 32, 0.4f, 0.8f, 0.5f, colorGreen);
		models[index].setPos(-1.5f, 0.8f, -2.5f);
		loadModel(gl, models[index], index, 9);
		index++;
		// Tree 3 Leaves Middle
		models[index] = new Cone(gl, mode, 32, 0.2f, 0.6f, 0.5f, colorGreen);
		models[index].setPos(-1.5f, 1.3f, -2.5f);
		loadModel(gl, models[index], index, 9);
		index++;
		// Tree 3 Leaves Top
		models[index] = new Cone(gl, mode, 32, 0.0f, 0.3f, 0.5f, colorGreen);
		models[index].setPos(-1.5f, 1.8f, -2.5f);
		loadModel(gl, models[index], index, 9);
		index++;

		// Tree 4 Trunk
		models[index] = new Cone(gl, mode, 32, 0.2f, 0.2f, 0.8f, colorBrown);
		models[index].setPos(1.8f, 0.4f, -2.7f);
		loadModel(gl, models[index], index, 9);
		index++;
		// Tree 4 Leaves Bottom
		models[index] = new Cone(gl, mode, 32, 0.4f, 0.8f, 0.5f, colorGreen);
		models[index].setPos(1.8f, 0.9f, -2.7f);
		loadModel(gl, models[index], index, 9);
		index++;
		// Tree 4 Leaves Middle
		models[index] = new Cone(gl, mode, 32, 0.2f, 0.6f, 0.5f, colorGreen);
		models[index].setPos(1.8f, 1.4f, -2.7f);
		loadModel(gl, models[index], index, 9);
		index++;
		// Tree 4 Leaves Top
		models[index] = new Cone(gl, mode, 32, 0.0f, 0.3f, 0.5f, colorGreen);
		models[index].setPos(1.8f, 1.9f, -2.7f);
		loadModel(gl, models[index], index, 9);
		index++;

		// Present 1
		models[index] = new Box(gl, mode, 0.8f, 0.8f, 0.8f, colorRed);
		models[index].setPos(2.0f, 0.4f, -1.0f);
		models[index].setRot(30, 0, 1, 0);
		loadModel(gl, models[index], index, 9);
		index++;

		// Present 2
		models[index] = new Box(gl, mode, 0.6f, 0.6f, 0.6f, colorBlue);
		models[index].setPos(2.3f, 0.3f, -0.2f);
		models[index].setRot(40, 0, 1, 0);
		loadModel(gl, models[index], index, 9);
		index++;

		// Present 3
		models[index] = new Box(gl, mode, 0.6f, 0.6f, 0.6f, colorRed);
		models[index].setPos(-2.3f, 0.3f, -0.2f);
		models[index].setRot(70, 0, 1, 0);
		loadModel(gl, models[index], index, 9);
		index++;
	}
}
