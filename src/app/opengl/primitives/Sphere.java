package app.opengl.primitives;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import app.opengl.Model;

/**
 * Erstellt eine UV-Kugel mit einer angegebenen horizontalen Auflösung, einer vertikalen Auflösung,
 * einem Radius und Farbe.
 * 
 * Der Code basiert auf den Beispielen auf Moodle von Karsten Lehn.
 */
public class Sphere extends Model {

    /**
     * Konstuktor erstellt ein Modell mit den Vertices und Indices für eine Kugel.
     * @param gl                    OpenGL Graphics Context
     * @param shaderProgramId       ID vom Shader Programm
     * @param verticalResolution    Vetikale Auflösung
     * @param horizontalResolution  Horizontale Auflösung
     * @param radius                Radius
     * @param color                 Farbe
     */
    public Sphere(GL3 gl, int shaderProgramId, int verticalResolution, int horizontalResolution,
            float radius, float[] color) {
        super(
                gl,
                shaderProgramId,
                makeVertices(verticalResolution, horizontalResolution, radius, color),
                makeIndices(verticalResolution, horizontalResolution),
                GL.GL_TRIANGLE_STRIP);
    }

    /**
     * Erstellt Vertices für eine UV-Kugel mit einer einzigen Farbe.
     * @param verticalResolution    Vertikale Auflösung
     * @param horizontalResolution  Horizontale Auflösung
     * @param radius                Radius
     * @param color                 Farbe
     * @return                      Liste an Vertices
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
     * Erstellt Indices für eine UV-Kugel.
     * @return Indices für das Vertex Array
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
    * Berechnet die Anzahl an Indices
    * @return   Anzahl an Indices
    */
    private static int noOfIndicesForSphere(int verticalResolution, int horizontalResolution) {
        return 2 * verticalResolution * horizontalResolution;
    }

}
