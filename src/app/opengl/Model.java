package app.opengl;

import com.jogamp.opengl.GL3;

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

	private static ShaderProgram shaderProgram;
	private static String shaderPath = ".\\resources\\shaders\\";
	private static String vertexShaderFileName = "Basic.vert";
	private static String fragmentShaderFileName = "Basic.frag";

	public Model(GL3 gl, float[] vertices, int[] indices, int mode) {
		this.vertices = vertices;
		this.indices = indices;
		this.mode = mode;

		// Set default shaderProgram
		shaderProgram = new ShaderProgram(gl);
		ShaderProgram.loadShaderAndCreateProgram(shaderPath, vertexShaderFileName, fragmentShaderFileName);
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

	public static ShaderProgram getShaderProgram() {
		return Model.shaderProgram;
	}

	public static void setShaderProgram(ShaderProgram shaderProgram) {
		Model.shaderProgram = shaderProgram;
	}

	public void deleteShaderProgram() {
		shaderProgram.deleteShaderProgram();
	}

	public int getShaderProgramID() {
		return shaderProgram.getShaderProgramID();
	}
}
