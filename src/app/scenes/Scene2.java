package app.scenes;

import app.InteractionHandler;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;

/*
    Green Triangle
*/
public class Scene2 extends GLCanvas implements GLEventListener {

    private InteractionHandler interactionHandler;
    private GLU glu;

    public Scene2(GLCapabilities capabilities) {
        // Create the canvas with the requested OpenGL capabilities
        super(capabilities);
        // Add this object as event listener
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
        GL2 gl = drawable.getGL().getGL2();
        glu = new GLU();
        // Background color of the GLCanvas
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        // Retrieve the OpenGL graphics context
        GL2 gl = drawable.getGL().getGL2();
        // Clear color and depth buffer
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

        // Reset matrix for geometric transformations
        gl.glLoadIdentity();
        // Apply view transform including camera positioning steered by the interaction handler
        glu.gluLookAt(0f, 0f, interactionHandler.getEyeZ(), 0f, 0f, 0f, 0f, 1.0f, 0f);
        gl.glTranslatef(interactionHandler.getxPosition(), interactionHandler.getyPosition(), 0f);
        gl.glRotatef(interactionHandler.getAngleXaxis(), 1f, 0f, 0f);
        gl.glRotatef(interactionHandler.getAngleYaxis(), 0f, 1f, 0f);

        // BEGIN: definition of scene content (i.e. objects, models)
        gl.glColor4f(0.00f, 1.00f, 0.00f, 1.0f);
        gl.glBegin(GL.GL_TRIANGLES);
        gl.glVertex3f(-0.5f, -0.5f, 0.0f);
        gl.glVertex3f(0.5f, -0.5f, 0.0f);
        gl.glVertex3f(0.0f, 0.5f, 0.0f);
        gl.glEnd();
        // END: definition of scene content
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        if (height <= 0) {
            height = 1;
        }
        final float ratio = (float) width / (float) height;
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(45.0f, ratio, 1.0, 20.0);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {

    }
}
