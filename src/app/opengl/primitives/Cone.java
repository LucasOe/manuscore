package app.opengl.primitives;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import app.opengl.Model;

/**
 * Erstellt einen Kegel mit einer angegebenen horizontalen Auflösung, einem Radius für
 * den oberen Kreis, einem Radius für den unteren Kreis, einer Länge und Farbe.
 * 
 * Der Code basiert auf den Beispielen auf Moodle von Karsten Lehn.
 */
public class Cone extends Model {

    /**
     * Konstuktor erstellt ein Modell mit den Vertices und Indices für einen Kegel.
     * @param gl                    OpenGL Graphics Context
     * @param shaderProgramId       ID vom Shader Programm
     * @param horizontalResolution  Horizontale Auflösung
     * @param radiusTop             Radius Oben
     * @param radiusBottom          Radius Unten
     * @param length                Länge
     * @param color                 Farbe
     */
    public Cone(GL3 gl, int shaderProgramId, int horizontalResolution, float radiusTop,
            float radiusBottom, float length,
            float[] color) {
        super(
                gl,
                shaderProgramId,
                makeVertices(horizontalResolution, radiusTop, radiusBottom, length, color),
                makeIndices(horizontalResolution),
                GL.GL_TRIANGLE_STRIP);
    }

    /**
     * Erstellt 24 Vertices für einen Kegel mit einer einzigen Farbe.
     * @param radiusTop     Radius für den oberen Kreis
     * @param radiusBottom  Radius für den unteren Kreis
     * @param length        Distanz zwischen den zwei Kreisen
     * @param color         Farbe der Vertices
     * @return              Liste an Vertices
     */
    private static float[] makeVertices(int horizontalResolution, float radiusTop, float radiusBottom, float length,
            float[] color) {

        // vertices for the top and bottom circles are duplicated
        // for correct normal vector orientation
        int noOfComponents = 3 + 3 + 3; // 3 position coordinates, 3 color coordinates, 3 normal coordinates
        float[] vertices = new float[(1 + (4 * horizontalResolution) + 1) * noOfComponents];
        int vertexNumberInc = 3 + 3 + 3; // three position coordinates, three color values, three normal coordinates
        int vertexNumber = 0; // initialize vertex count

        // y Coordinate of top circle
        float yTop = length / 2;
        // y Coordinate of bottom circle
        float yBottom = -length / 2;

        // normal vector for top circle
        float[] topNormal = { 0, 1, 0 };
        // top center of circle
        vertices[vertexNumber + 0] = 0f;
        vertices[vertexNumber + 1] = yTop;
        vertices[vertexNumber + 2] = 0f;
        // normal vector coordinates
        vertices[vertexNumber + 3] = topNormal[0];
        vertices[vertexNumber + 4] = topNormal[1];
        vertices[vertexNumber + 5] = topNormal[2];
        // color coordinates (for all vertices the same)
        vertices[vertexNumber + 6] = color[0];
        vertices[vertexNumber + 7] = color[1];
        vertices[vertexNumber + 8] = color[2];

        vertexNumber += vertexNumberInc;

        // vertices for the top circle
        float angleTop = 0;
        float angleTopInc = (float) (2 * Math.PI / horizontalResolution);
        for (int angleIndex = 0; angleIndex < horizontalResolution; angleIndex++) {
            // position coordinates
            vertices[vertexNumber + 0] = radiusTop * (float) (Math.cos(angleTop));
            vertices[vertexNumber + 1] = yTop;
            vertices[vertexNumber + 2] = radiusTop * (float) Math.sin(angleTop);
            // normal vector coordinates
            vertices[vertexNumber + 3] = topNormal[0];
            vertices[vertexNumber + 4] = topNormal[1];
            vertices[vertexNumber + 5] = topNormal[2];
            // color coordinates (for all vertices the same)
            vertices[vertexNumber + 6] = color[0];
            vertices[vertexNumber + 7] = color[1];
            vertices[vertexNumber + 8] = color[2];

            vertexNumber += vertexNumberInc;
            angleTop += angleTopInc;
        }

        // vertices for the top edge of the surface
        angleTop = 0;
        angleTopInc = (float) (2 * Math.PI / horizontalResolution);
        // y component of normal vector coordinates for top edge of surface
        float yNormalTop = radiusTop * (radiusBottom - radiusTop) / length;
        for (int angleIndex = 0; angleIndex < horizontalResolution; angleIndex++) {
            // position coordinates
            float xPos = radiusTop * (float) (Math.cos(angleTop));
            float yPos = yTop;
            float zPos = radiusTop * (float) Math.sin(angleTop);
            vertices[vertexNumber + 0] = xPos;
            vertices[vertexNumber + 1] = yPos;
            vertices[vertexNumber + 2] = zPos;
            // normalize normal vector
            float normalizationFactor = 1
                    / (float) Math.sqrt((xPos * xPos) + (yNormalTop * yNormalTop) + (zPos * zPos));
            vertices[vertexNumber + 3] = xPos * normalizationFactor;
            vertices[vertexNumber + 4] = 0f;
            vertices[vertexNumber + 5] = zPos * normalizationFactor;
            // color coordinates (for all vertices the same)
            vertices[vertexNumber + 6] = color[0];
            vertices[vertexNumber + 7] = color[1];
            vertices[vertexNumber + 8] = color[2];

            vertexNumber += vertexNumberInc;
            angleTop += angleTopInc;
        }

        // vertices for the bottom edge of the surface
        float angleBottom = 0;
        float angleBottomInc = (float) (2 * Math.PI / horizontalResolution);
        // y component of normal vector coordinates for bottom edge of surface
        float yNormalBottom = radiusBottom * (radiusBottom - radiusTop) / length;
        for (int angleIndex = 0; angleIndex < horizontalResolution; angleIndex++) {
            // position coordinates
            float xPos = radiusBottom * (float) (Math.cos(angleBottom));
            float yPos = yBottom;
            float zPos = radiusBottom * (float) Math.sin(angleBottom);
            vertices[vertexNumber + 0] = xPos;
            vertices[vertexNumber + 1] = yPos;
            vertices[vertexNumber + 2] = zPos;
            // normalize normal vector
            float normalizationFactor = 1
                    / (float) Math.sqrt((xPos * xPos) + (yNormalBottom * yNormalBottom) + (zPos * zPos));
            vertices[vertexNumber + 3] = xPos * normalizationFactor;
            vertices[vertexNumber + 4] = 0f;
            vertices[vertexNumber + 5] = zPos * normalizationFactor;
            // color coordinates (for all vertices the same)
            vertices[vertexNumber + 6] = color[0];
            vertices[vertexNumber + 7] = color[1];
            vertices[vertexNumber + 8] = color[2];

            vertexNumber += vertexNumberInc;
            angleBottom += angleBottomInc;
        }

        // vertices for the bottom circle
        // normal vector for bottom circle
        float[] bottomNormal = { 0, -1, 0 };
        angleBottom = 0;
        angleBottomInc = (float) (2 * Math.PI / horizontalResolution);
        for (int angleIndex = 0; angleIndex < horizontalResolution; angleIndex++) {
            // position coordinates
            vertices[vertexNumber + 0] = radiusBottom * (float) (Math.cos(angleBottom));
            vertices[vertexNumber + 1] = yBottom;
            vertices[vertexNumber + 2] = radiusBottom * (float) Math.sin(angleBottom);
            // normal vector coordinates
            vertices[vertexNumber + 3] = bottomNormal[0];
            vertices[vertexNumber + 4] = bottomNormal[1];
            vertices[vertexNumber + 5] = bottomNormal[2];
            // color coordinates (for all vertices the same)
            vertices[vertexNumber + 6] = color[0];
            vertices[vertexNumber + 7] = color[1];
            vertices[vertexNumber + 8] = color[2];

            vertexNumber += vertexNumberInc;
            angleBottom += angleBottomInc;
        }

        // bottom center of circle
        vertices[vertexNumber + 0] = 0f;
        vertices[vertexNumber + 1] = yBottom;
        vertices[vertexNumber + 2] = 0f;
        // normal vector coordinates
        vertices[vertexNumber + 3] = bottomNormal[0];
        vertices[vertexNumber + 4] = bottomNormal[1];
        vertices[vertexNumber + 5] = bottomNormal[2];
        // color coordinates (for all vertices the same)
        vertices[vertexNumber + 6] = color[0];
        vertices[vertexNumber + 7] = color[1];
        vertices[vertexNumber + 8] = color[2];

        return vertices;
    }

    /**
     * Erstellt Indices für einen Kegel.
     * @param horizontalResolution  Horizontale Auflösung
     * @return                      Indices für das Vertex Array
     */
    private static int[] makeIndices(int horizontalResolution) {

        // Indices to refer to the number of the cone (frustum) vertices
        // defined in makeVertices()
        int[] indices = new int[noOfIndicesForCone(horizontalResolution)];

        // BEGIN: Indices for top circle
        int topCenterIndex = 0;
        int firstTopCircleEdgeIndex = 1;

        int index = 0;
        // first index, center of top circle
        // draw twice to get the front face orientation right
        indices[index] = topCenterIndex;
        index++;
        indices[index] = topCenterIndex;
        index++;

        for (int hIndex = 1; hIndex <= horizontalResolution; hIndex++) {
            indices[index] = hIndex;
            index++;
            indices[index] = topCenterIndex;
            index++;
        }
        // close the top circle with a final triangle
        indices[index] = firstTopCircleEdgeIndex;
        index++;
        // END: Indices for top circle

        // BEGIN: Indices for surface
        int firstSurfaceTopIndex = horizontalResolution + 1;
        int firstSurfaceBottomIndex = (2 * horizontalResolution) + 1;
        for (int hIndex = 0; hIndex < horizontalResolution; hIndex++) {
            indices[index] = firstSurfaceTopIndex + hIndex;
            index++;
            indices[index] = firstSurfaceBottomIndex + hIndex;
            index++;
        }
        // Close the surface
        indices[index] = firstSurfaceTopIndex;
        index++;
        indices[index] = firstSurfaceBottomIndex;
        index++;
        // END: Indices for surface

        // BEGIN: Indices for bottom circle
        int bottomCenterIndex = (4 * horizontalResolution) + 1;
        int firstBottomCircleEdgeIndex = (3 * horizontalResolution) + 1;

        // picking up from surface
        indices[index] = firstBottomCircleEdgeIndex;
        index++;
        // first index, center of top circle
        indices[index] = bottomCenterIndex;
        index++;

        for (int hIndex = 0; hIndex < horizontalResolution; hIndex++) {
            indices[index] = firstBottomCircleEdgeIndex + hIndex;
            index++;
            indices[index] = bottomCenterIndex;
            index++;
        }
        // close the top circle with a final triangle
        indices[index] = firstBottomCircleEdgeIndex;
        return indices;
    }

    /**
    * Berechnet die Anzhal an Indices
    * @return   Anzahl an Indices
    */
    private static int noOfIndicesForCone(int horizontalResolution) {
        int noOfIndicesForCircle = 1 + // center of the circle
                                       // additional vertices for drawing with TRIANGLE_STRIP instead of TRIANGLE_FAN
                (2 * horizontalResolution) +
                1; // closing the circle

        return 1 + // reverse back/front faces
                noOfIndicesForCircle + // top circle
                (2 * horizontalResolution) + // surface
                2 + // close the surface
                1 + // picking up the bottom circle
                noOfIndicesForCircle;// bottom circle
    }
}
