package app.opengl.primitives;

import com.jogamp.opengl.GL3;

import app.opengl.Model;

public class Sphere extends Model {

    public Sphere(GL3 gl, int mode, int verticalResolution, int horizontalResolution, float radius, float[] color) {
        super(
                gl,
                makeVertices(verticalResolution, horizontalResolution, radius, color),
                makeIndices(verticalResolution, horizontalResolution),
                mode);
    }

    /**
     * Creates vertices for a (UV)-sphere with one single color and normal vectors.
     * To be used together with makeIndicesForTriangleStrip().
     * @param radius radius of the sphere
     * @param color three dimensional color vector for each vertex
     * @return list of vertices
     */
    private static float[] makeVertices(int verticalResolution, int horizontalResolution, float radius, float[] color) {
        // Using spherical coordinates to create the vertices
        int noOfComponents = 3 + 3 + 3; // 3 position coordinates, 3 color coordinates, 3 normal coordinates
        float[] vertices = new float[(verticalResolution + 1) * horizontalResolution * noOfComponents];
        int vertexNumberInc = 3 + 3 + 3; // three position coordinates, three color values, three normal coordinates
        int vertexNumber = 0;

        float elevation = 0;
        float elevationInc = (float) (Math.PI / verticalResolution);
        float azimuth = 0;
        float azimuthInc = (float) (2 * Math.PI / horizontalResolution);
        for (int elevationIndex = 0; elevationIndex <= verticalResolution; elevationIndex++) {
            azimuth = 0;
            for (int azimuthIndex = 0; azimuthIndex < horizontalResolution; azimuthIndex++) {
                // Position coordinates in spherical coordinates
                float xPos = radius * (float) (Math.sin(elevation) * Math.cos(azimuth));
                float yPos = radius * (float) (Math.sin(elevation) * Math.sin(azimuth));
                float zPos = radius * (float) Math.cos(elevation);
                vertices[vertexNumber + 0] = xPos;
                vertices[vertexNumber + 1] = yPos;
                vertices[vertexNumber + 2] = zPos;
                // Coordinates of normal vector for a sphere this vector is identical to the normalizes position vector
                float normalizationFactor = 1 / (float) Math.sqrt((xPos * xPos) + (yPos * yPos) + (zPos * zPos));
                vertices[vertexNumber + 3] = xPos * normalizationFactor;
                vertices[vertexNumber + 4] = yPos * normalizationFactor;
                vertices[vertexNumber + 5] = zPos * normalizationFactor;
                // Color coordinates (for all vertices the same)
                vertices[vertexNumber + 6] = color[0];
                vertices[vertexNumber + 7] = color[1];
                vertices[vertexNumber + 8] = color[2];

                vertexNumber += vertexNumberInc;
                azimuth += azimuthInc;
            }
            elevation += elevationInc;
        }
        return vertices;
    }

    /**
     * Creates indices for drawing a sphere with glDrawElements().
     * To be used together with makeVertices().
     * To be used with "glDrawElements" and "GL_TRIANGLE_STRIP".
     * @return indices into the vertex array of the sphere
     */
    private static int[] makeIndices(int verticalResolution, int horizontalResolution) {

        // Indices to refer to the number of the sphere vertices
        // defined in makeVertices()
        int[] indices = new int[noOfIndicesForSphere(verticalResolution, horizontalResolution)];
        int index = 0;
        for (int vIndex = 1; vIndex <= verticalResolution; vIndex++)
            for (int hIndex = 0; hIndex < horizontalResolution; hIndex++) {
                indices[index] = ((vIndex - 1) * horizontalResolution) + hIndex;
                index++;
                indices[index] = ((vIndex) * horizontalResolution) + hIndex;
                index++;
            }

        return indices;
    }

    /**
    * Computes the number of indices of a sphere for the draw call.
    * @return number of indices the index buffer
    */
    private static int noOfIndicesForSphere(int verticalResolution, int horizontalResolution) {
        return 2 * verticalResolution * horizontalResolution;
    }

}
