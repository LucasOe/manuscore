package app.opengl;

import com.jogamp.opengl.GL3;

import jogamp.common.os.elf.Shdr;

public class Model {
	private float[] vertices;
	private int[] indices;

	private ShaderProgram shaderProgram;
	private String shaderPath = ".\\resources\\shaders\\";
	private String vertexShaderFileName = "Basic.vert";
	private String fragmentShaderFileName = "Basic.frag";

	public Model(GL3 gl, float[] vertices, int[] indices) {
		this.vertices = vertices;
		this.indices = indices;

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

	public ShaderProgram getShaderProgram() {
		return this.shaderProgram;
	}

	public void setShaderProgram(ShaderProgram shaderProgram) {
		this.shaderProgram = shaderProgram;
	}

	public void deleteShaderProgram() {
		shaderProgram.deleteShaderProgram();
	}

	public int getShaderProgramID() {
		return shaderProgram.getShaderProgramID();
	}
}
