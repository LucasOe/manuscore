package app.opengl;

import com.jogamp.opengl.GL3;

/**
 * Speichert die Vertices und Indices in einer Instanz zusammen mit der Position und Rotation.
 */
public class Model {
	private float[] vertices;
	private int[] indices;

	private int mode;
	private float posX;
	private float posY;
	private float posZ;
	private float ang;
	private float rotX;
	private float rotY;
	private float rotZ;

	private int shaderProgramId;

	/**
	 * Konstruktor intialisiert die Instanzvariablen.
	 * @param gl				OpenGL Graphics Context
	 * @param shaderProgramId	ID vom Shader Programm
	 * @param vertices			Liste mit Vertices
	 * @param indices			Liste mit Indices
	 * @param mode				GL_TRIANGLES oder GL_TRIANGLE_STRIP
	 */
	public Model(GL3 gl, int shaderProgramId, float[] vertices, int[] indices, int mode) {
		this.vertices = vertices;
		this.indices = indices;
		this.mode = mode;
		this.shaderProgramId = shaderProgramId;
	}

	public float[] getVertices() {
		return this.vertices;
	}

	public void setVertices(float[] vertices) {
		this.vertices = vertices;
	}

	public int[] getIndices() {
		return this.indices;
	}

	public void setIndices(int[] indices) {
		this.indices = indices;
	}

	public int getMode() {
		return this.mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public float getPosX() {
		return this.posX;
	}

	public float getPosY() {
		return this.posY;
	}

	public float getPosZ() {
		return this.posZ;
	}

	public void setPos(float x, float y, float z) {
		this.posX = x;
		this.posY = y;
		this.posZ = z;
	}

	public float getAng() {
		return this.ang;
	}

	public float getRotX() {
		return this.rotX;
	}

	public float getRotY() {
		return this.rotY;
	}

	public float getRotZ() {
		return this.rotZ;
	}

	public void setRot(float ang, float x, float y, float z) {
		this.ang = ang;
		this.rotX = x;
		this.rotY = y;
		this.rotZ = z;
	}

	public void setShaderProgramId(int shaderProgramId) {
		this.shaderProgramId = shaderProgramId;
	}

	public int getShaderProgramId() {
		return this.shaderProgramId;
	}
}
