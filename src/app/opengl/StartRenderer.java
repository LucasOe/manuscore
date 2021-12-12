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

    static int activeObject;

    // Defining shader source code file paths and names
    String shaderPath = ".\\resources\\shaders\\";
    String modelPath = ".\\resources\\models\\";

    String vertexShaderFileName = "Basic.vert";
    String fragmentShaderFileName = "Basic.frag";

    // List of obj files
    Path[] objectPaths = { Paths.get(modelPath + "suzanne.obj"), Paths.get(modelPath + "heart.obj") };

    // contains the geometry of the obj file
    Model[] objectData = new Model[objectPaths.length];

    // Object for loading shaders and creating a shader program
    ShaderProgram shaderProgram;

    // OpenGL buffer names for data allocation and handling on GPU
    int[] vaoName;
    int[] vboName;
    int[] iboName;

    // Declaration of an object for handling keyboard and mouse interactions
    InteractionHandler interactionHandler;

    // Declaration for using the projection-model-view matrix tool
    PMVMatrix pmvMatrix;

    GL3 gl;

    public StartRenderer() {
        // Create the OpenGL canvas with default capabilities
        super();

        // Add this object as OpenGL event listener to the canvas
        this.addGLEventListener(this);
        createAndRegisterInteractionHandler();
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

        // Loading the vertex and fragment shaders and creation of the shader program.
        shaderProgram = new ShaderProgram(gl);
        ShaderProgram.loadShaderAndCreateProgram(shaderPath, vertexShaderFileName, fragmentShaderFileName);
        // Use the compiled shaderProgram
        gl.glUseProgram(shaderProgram.getShaderProgramID());

        // Create object for projection-model-view matrix calculation.
        pmvMatrix = new PMVMatrix();

        // Create a Model object and load the vertices and indicies from the obj file
        for (int i = 0; i < objectPaths.length; i++) {
            try {
                Mesh mesh = new OBJLoader().setLoadNormals(true) // tell the loader to also load normal data
                        .setGenerateIndexedMeshes(true) // tell the loader to output separate index arrays
                        .loadMesh(Resource.file(objectPaths[i])); // load the file obj file

                objectData[i] = new Model(mesh.getVertices(), mesh.getIndices());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Create the Vertex Array Object on the GPU
        vaoName = new int[1];
        gl.glGenVertexArrays(1, vaoName, 0);
        gl.glBindVertexArray(vaoName[0]);

        // Creating the Vertex Buffer Objects on the GPU
        vboName = new int[1];
        gl.glGenBuffers(1, vboName, 0);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboName[0]);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, objectData[activeObject].getVertices().length * Float.BYTES,
                FloatBuffer.wrap(objectData[activeObject].getVertices()), GL.GL_STATIC_DRAW);

        // Creating the Index Buffer Objects on the GPU
        iboName = new int[1];
        gl.glGenBuffers(1, iboName, 0);
        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, iboName[0]);
        gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, objectData[activeObject].getIndices().length * Integer.BYTES,
                IntBuffer.wrap(objectData[activeObject].getIndices()), GL.GL_STATIC_DRAW);

        // Enable alpha transparency
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        // Enable layout position 0
        gl.glEnableVertexAttribArray(0);
        // Map layout position 0 to the position information per vertex in the VBO.
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 6 * Float.BYTES, 0);
        // Enable layout position 1
        gl.glEnableVertexAttribArray(1);
        // Map layout position 1 to the color information per vertex in the VBO.
        gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);

        // Set start parameter(s) for the interaction handler.
        interactionHandler.setEyeZ(2);

        // Switch on depth test.
        gl.glEnable(GL.GL_DEPTH_TEST);

        // Set background color of the GLCanvas.
        gl.glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
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

        // Create the BufferData every single frame to the current active object
        gl.glBufferData(GL.GL_ARRAY_BUFFER, objectData[activeObject].getVertices().length * Float.BYTES,
                FloatBuffer.wrap(objectData[activeObject].getVertices()), GL.GL_STATIC_DRAW);
        gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, objectData[activeObject].getIndices().length * Integer.BYTES,
                IntBuffer.wrap(objectData[activeObject].getIndices()), GL.GL_STATIC_DRAW);

        // Transfer the PVM-Matrix (model-view and projection matrix) to the GPU
        // via uniforms
        // Transfer projection matrix via uniform layout position 0
        gl.glUniformMatrix4fv(0, 1, false, pmvMatrix.glGetPMatrixf());
        // Transfer model-view matrix via layout position 1
        gl.glUniformMatrix4fv(1, 1, false, pmvMatrix.glGetMvMatrixf());

        // Draw the triangles using the indices array
        gl.glDrawElements(GL.GL_TRIANGLES, objectData[activeObject].indices.length, GL.GL_UNSIGNED_INT, 0);
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
        shaderProgram.deleteShaderProgram();

        // deactivate VAO and VBO
        gl.glBindVertexArray(0);
        gl.glDisableVertexAttribArray(0);
        gl.glDisableVertexAttribArray(1);
        gl.glDeleteVertexArrays(1, vaoName, 0);
        gl.glDeleteBuffers(1, vboName, 0);

        System.exit(0);
    }

    // Called by the EvenHandler in MainWindow.java
    public void clickButton(int active) {
        activeObject = active;
        System.out.println("Loading: " + objectPaths[activeObject]);
    }
}
