package app.opengl;

public class Model {
	float[] vertices;
	int[] indices;

	public Model(float[] vertices, int[] indices) {
		this.vertices = vertices;
		this.indices = indices;
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
}
