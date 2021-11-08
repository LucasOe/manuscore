package app;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.glu.GLU;

import de.hshl.obj.loader.OBJLoader;
import de.hshl.obj.loader.Resource;

/*
    Red Triangle
*/
public class StartRenderer extends GLCanvas implements GLEventListener {

    private InteractionHandler interactionHandler;
    private GLU glu;

    private static final Path objFile = Paths.get("./resources/models/suzanne.obj");
    private float[] vertices;

    public StartRenderer() {
        System.out.println("Loaded Scene 1");
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
        // Retrieve the OpenGL graphics context
        GL2 gl = drawable.getGL().getGL2();
        // Creation of an GLU object, for using the OpenGL Utility Library
        glu = new GLU();
        // Outputs information about the available and chosen profile
        System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
        System.err.println("INIT GL IS: " + gl.getClass().getName());
        System.err.println("GL_VENDOR: " + gl.glGetString(GL.GL_VENDOR));
        System.err.println("GL_RENDERER: " + gl.glGetString(GL.GL_RENDERER));
        System.err.println("GL_VERSION: " + gl.glGetString(GL.GL_VERSION));

        try {
            vertices = new OBJLoader().setLoadNormals(true).loadMesh(Resource.file(objFile)).getVertices();
        } catch (IOException fileException) {
            fileException.printStackTrace();
            System.exit(1);
        }

        // A subroutine for light definition might be called here
        //setLight(gl);

        // Start parameter settings for the interaction handler might be called here
        // interactionHandler.setEyeZ(2);

        // Background color of the GLCanvas
        gl.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        // enable shading
        gl.glEnable(GLLightingFunc.GL_LIGHTING);
        gl.glEnable(GLLightingFunc.GL_LIGHT0);

        gl.glEnable(GL.GL_CULL_FACE);
        gl.glEnable(GL.GL_DEPTH_TEST);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        // Retrieve the OpenGL graphics context
        GL2 gl = drawable.getGL().getGL2();
        // Clear color and depth buffer
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

        // Switch on back face culling
        //gl.glEnable(GL.GL_CULL_FACE);
        //gl.glCullFace(GL.GL_BACK);

        // Reset matrix for geometric transformations
        gl.glLoadIdentity();
        // Apply view transform including camera positioning steered by the interaction handler
        glu.gluLookAt(0f, 0f, interactionHandler.getEyeZ(), 0f, 0f, 0f, 0f, 1.0f, 0f);
        gl.glTranslatef(interactionHandler.getxPosition(), interactionHandler.getyPosition(), 0f);
        gl.glRotatef(interactionHandler.getAngleXaxis(), 1f, 0f, 0f);
        gl.glRotatef(interactionHandler.getAngleYaxis(), 0f, 1f, 0f);

        // Controlling the interaction settings
        /*        System.out.println("Camera: z = " + interactionHandler.getEyeZ() + ", " +
         "x-Rot: " + interactionHandler.getAngleXaxis() +
         ", y-Rot: " + interactionHandler.getAngleYaxis() +
         ", x-Translation: " + interactionHandler.getxPosition()+
         ", y-Translation: " + interactionHandler.getyPosition());// definition of translation of model (Model/Object Coordinates --> World Coordinates)
        */
        // BEGIN: definition of scene content (i.e. objects, models)
        gl.glColor4f(0.96f, 0.56f, 0.04f, 1.0f);
        gl.glBegin(GL.GL_TRIANGLES);
        {

            for (int vertexIndex = 0; vertexIndex + 5 < vertices.length; vertexIndex += 6) {
                float x = vertices[vertexIndex] + 0.5f;
                float y = vertices[vertexIndex + 1];
                float z = vertices[vertexIndex + 2];

                float nx = vertices[vertexIndex + 3];
                float ny = vertices[vertexIndex + 4];
                float nz = vertices[vertexIndex + 5];

                gl.glNormal3f(nx, ny, nz);
                gl.glVertex3f(x, y, z);
            }

        }
        gl.glEnd();
        // END: definition of scene content
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        // Retrieve the OpenGL graphics context
        GL2 gl = drawable.getGL().getGL2();

        // Avoiding division by zero
        if (height == 0)
            height = 1;
        // Set the viewport to the entire window
        gl.glViewport(0, 0, width, height);
        // Switch to perspective projection
        gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
        // Reset projection matrix to identity
        gl.glLoadIdentity();
        // Determine the aspect ratio of the viewport
        float aspectRatio = (float) width / (float) height;
        // Calculate projection matrix
        //      Parameters for  glu-call:
        //          fovy (field of view), aspect ratio,
        //          zNear (near clipping plane), zFar (far clipping plane)
        glu.gluPerspective(45.0, aspectRatio, 0.1, 10000.0);
        // Switch to model-view transform
        gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {

    }
}
