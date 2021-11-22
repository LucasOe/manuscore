package app;

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
    final static String shaderPath = ".\\resources\\shaders\\";
    final static String modelPath = ".\\resources\\models\\";

    final String vertexShaderFileName = "Basic.vert";
    final String fragmentShaderFileName = "Basic.frag";

    private static Path[] objectPaths = { Paths.get(modelPath + "suzanne.obj"), Paths.get(modelPath + "heart.obj") };

    // Object for loading shaders and creating a shader program
    private ShaderProgram shaderProgram;

    // OpenGL buffer names for data allocation and handling on GPU
    int[] vaoName; // List of names (integer pointers) of vertex array objects
    int[] vboName; // List of names (integer pointers) of vertex buffer objects
    int[] iboName; // List of names (integer pointers) of index buffer objects

    // Declaration of an object for handling keyboard and mouse interactions
    InteractionHandler interactionHandler;

    // Declaration for using the projection-model-view matrix tool
    PMVMatrix pmvMatrix;

    // contains the geometry of our OBJ file
    static Model[] objectData = new Model[objectPaths.length];

    public StartRenderer() {
        // Create the OpenGL canvas with default capabilities
        super();
        // Add this object as OpenGL event listener to the canvas
        this.addGLEventListener(this);
        createAndRegisterInteractionHandler();
    }

    public StartRenderer(GLCapabilities capabilities) {
        // Create the OpenGL canvas with the requested OpenGL capabilities
        super(capabilities);
        // Add this object as an OpenGL event listener to the canvas
        this.addGLEventListener(this);
        createAndRegisterInteractionHandler();
    }

    private void createAndRegisterInteractionHandler() {
        // The constructor call of the interaction handler generates meaningful default values.
        // The start parameters can also be set via setters
        // (see class definition of the interaction handler).
        interactionHandler = new InteractionHandler();
        this.addKeyListener(interactionHandler);
        this.addMouseListener(interactionHandler);
        this.addMouseMotionListener(interactionHandler);
        this.addMouseWheelListener(interactionHandler);
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        // Retrieve the OpenGL graphics context
        GL3 gl = drawable.getGL().getGL3();
        // Outputs information about the available and chosen profile
        System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
        System.err.println("INIT GL IS: " + gl.getClass().getName());
        System.err.println("GL_VENDOR: " + gl.glGetString(GL.GL_VENDOR));
        System.err.println("GL_RENDERER: " + gl.glGetString(GL.GL_RENDERER));
        System.err.println("GL_VERSION: " + gl.glGetString(GL.GL_VERSION));

        // Loading the vertex and fragment shaders and creation of the shader program.
        shaderProgram = new ShaderProgram(gl);
        ShaderProgram.loadShaderAndCreateProgram(shaderPath, vertexShaderFileName, fragmentShaderFileName);

        // Create object for projection-model-view matrix calculation.
        pmvMatrix = new PMVMatrix();

        // Vertices for drawing a triangle.
        // To be transferred to a vertex buffer object on the GPU.
        // Interleaved data layout: position, color
        for (int i = 0; i < objectPaths.length; i++) {
            try {
                Mesh mesh = new OBJLoader().setLoadNormals(true) // tell the loader to also load normal data
                        .setGenerateIndexedMeshes(true) // tell the loader to output separate index arrays
                        .loadMesh(Resource.file(objectPaths[i])); // actually load the file

                objectData[i] = new Model(mesh.getVertices(), mesh.getIndices());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Enable alpha transparency
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        // Set start parameter(s) for the interaction handler.
        interactionHandler.setEyeZ(2);

        // Switch on back face culling
        // gl.glEnable(GL.GL_CULL_FACE);
        //gl.glCullFace(GL.GL_BACK);

        // Switch on depth test.
        gl.glEnable(GL.GL_DEPTH_TEST);

        // Set background color of the GLCanvas.
        gl.glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        // Retrieve the OpenGL graphics context
        GL3 gl = drawable.getGL().getGL3();
        // Clear color and depth buffer
        gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

        // Controlling the interaction settings
        /*        System.out.println("Camera: z = " + interactionHandler.getEyeZ() + ", " +
                "x-Rot: " + interactionHandler.getAngleXaxis() +
                ", y-Rot: " + interactionHandler.getAngleYaxis() +
                ", x-Translation: " + interactionHandler.getxPosition()+
                ", y-Translation: " + interactionHandler.getyPosition());// definition of translation of model (Model/Object Coordinates --> World Coordinates)
        */

        // Apply view transform using the PMV-Tool
        // Camera positioning is steered by the interaction handler
        pmvMatrix.glLoadIdentity();
        pmvMatrix.gluLookAt(0f, 0f, interactionHandler.getEyeZ(), 0f, 0f, 0f, 0f, 1.0f, 0f);
        pmvMatrix.glTranslatef(interactionHandler.getxPosition(), interactionHandler.getyPosition(), 0f);
        pmvMatrix.glRotatef(interactionHandler.getAngleXaxis(), 1f, 0f, 0f);
        pmvMatrix.glRotatef(interactionHandler.getAngleYaxis(), 0f, 1f, 0f);

        /*
            Begin
        */

        // Create and activate a vertex array object (VAO)
        // Useful for switching between data sets for object rendering.
        vaoName = new int[1];
        // Creating the buffer on GPU.
        gl.glGenVertexArrays(1, vaoName, 0);
        if (vaoName[0] < 1)
            System.err.println("Error allocating vertex array object (VAO) on GPU.");
        // Switch to this VAO.
        gl.glBindVertexArray(vaoName[0]);

        // Create, activate and initialize vertex buffer object (VBO)
        // Used to store vertex data on the GPU.
        vboName = new int[1];
        // Creating the buffer on GPU.
        gl.glGenBuffers(1, vboName, 0);
        if (vboName[0] < 1)
            System.err.println("Error allocating vertex buffer object (VBO) on GPU.");
        // Activating this buffer as vertex buffer object.
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboName[0]);
        // Transferring the vertex data (see above) to the VBO on GPU.
        // (floats use 4 bytes in Java)
        gl.glBufferData(GL.GL_ARRAY_BUFFER, objectData[activeObject].vertices.length * Float.BYTES,
                FloatBuffer.wrap(objectData[activeObject].vertices), GL.GL_STATIC_DRAW);

        iboName = new int[1];
        // Creating the buffer on GPU.
        gl.glGenBuffers(1, iboName, 0);
        if (iboName[0] < 1)
            System.err.println("Error allocating vertex buffer object (VBO) on GPU.");

        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, iboName[0]);
        gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, objectData[activeObject].indices.length * Integer.BYTES,
                IntBuffer.wrap(objectData[activeObject].indices), GL.GL_STATIC_DRAW);

        // Activate and map input for the vertex shader from VBO,
        // taking care of interleaved layout of vertex data (position and color),
        // Enable layout position 0
        gl.glEnableVertexAttribArray(0);
        // Map layout position 0 to the position information per vertex in the VBO.
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 6 * Float.BYTES, 0);
        // Enable layout position 1
        gl.glEnableVertexAttribArray(1);
        // Map layout position 1 to the color information per vertex in the VBO.
        gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);

        /*
            End
        */

        // Switch to this vertex buffer array for drawing.
        gl.glBindVertexArray(vaoName[0]);
        // Activating the compiled shader program.
        // Could be placed into the init-method for this simple example.
        gl.glUseProgram(shaderProgram.getShaderProgramID());

        // Transfer the PVM-Matrix (model-view and projection matrix) to the GPU
        // via uniforms
        // Transfer projection matrix via uniform layout position 0
        gl.glUniformMatrix4fv(0, 1, false, pmvMatrix.glGetPMatrixf());
        // Transfer model-view matrix via layout position 1
        gl.glUniformMatrix4fv(1, 1, false, pmvMatrix.glGetMvMatrixf());

        // Draw the triangles using the indices array
        gl.glDrawElements(GL.GL_TRIANGLES, // mode
                objectData[activeObject].indices.length, // count
                GL.GL_UNSIGNED_INT, // type
                0 // element array buffer offset
        );
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        // Retrieve the OpenGL graphics context
        GL3 gl = drawable.getGL().getGL3();

        // Set the viewport to the entire window
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

    @Override
    public void dispose(GLAutoDrawable drawable) {
        // Retrieve the OpenGL graphics context
        GL3 gl = drawable.getGL().getGL3();
        System.out.println("Deleting allocated objects, incl. the shader program.");

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

    public static void clickButton(int active) {
        activeObject = active;
    }
}
