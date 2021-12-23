package app.opengl.primitives;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import app.opengl.Model;

/**
 * Erstellt eine Box mit der angegebenen Breite, Höhe, Tiefe und Farbe.
 * 
 * Der Code basiert auf den Beispielen auf Moodle von Karsten Lehn.
 */
public class Box extends Model {

    /**
     * Konstuktor erstellt ein Modell mit den Vertices und Indices für eine Box.
     * @param gl                OpenGL Graphics Context
     * @param shaderProgramId   ID vom Shader Programm
     * @param width             Breite
     * @param height            Höhe
     * @param depth             Tiefe
     * @param color             Farbe
     */
    public Box(GL3 gl, int shaderProgramId, float width, float height, float depth, float[] color) {
        super(
                gl,
                shaderProgramId,
                makeBoxVertices(width, height, depth, color),
                makeBoxIndices(),
                GL.GL_TRIANGLE_STRIP);
    }

    /**
     * Erstellt 24 Vertices für eine Box mit einer einzigen Farbe.
     * @param width     Breite der Box (x-Richtung)
     * @param height    Höhe der Box (y-Richtung)
     * @param depth     Tiefe der Box (z-Richtung)
     * @param color     Farbe der Vertices
     * @return          Liste an Vertices
     */
    private static float[] makeBoxVertices(float width, float height, float depth, float[] color) {
        float halfOfWidth = width / 2;
        float halfOfHeight = height / 2;
        float halfOfDepth = depth / 2;

        // Definition der Positionen der Vertices
        float[] p0 = { -halfOfWidth, +halfOfHeight, +halfOfDepth }; // 0 front
        float[] p1 = { +halfOfWidth, +halfOfHeight, +halfOfDepth }; // 1 front
        float[] p2 = { +halfOfWidth, -halfOfHeight, +halfOfDepth }; // 2 front
        float[] p3 = { -halfOfWidth, -halfOfHeight, +halfOfDepth }; // 3 front
        float[] p4 = { -halfOfWidth, +halfOfHeight, -halfOfDepth }; // 4 back
        float[] p5 = { +halfOfWidth, +halfOfHeight, -halfOfDepth }; // 5 back
        float[] p6 = { +halfOfWidth, -halfOfHeight, -halfOfDepth }; // 6 back
        float[] p7 = { -halfOfWidth, -halfOfHeight, -halfOfDepth }; // 7 back

        // Farbe
        float[] c = color;

        // Definition der Normals
        float[] nf = { 0, 0, 1 }; // 0 front
        float[] nb = { 0, 0, -1 }; // 0 back
        float[] nl = { -1, 0, 0 }; // 0 left
        float[] nr = { 1, 0, 0 }; // 0 right
        float[] nu = { 0, 1, 0 }; // 0 up (top)
        float[] nd = { 0, -1, 0 }; // 0 down (bottom)

        // Liste an Vertices
        float[] verticies = {
                // front surface
                // Index: 0
                p0[0], p0[1], p0[2], // Position
                nf[0], nf[1], nf[2], // normal
                c[0], c[1], c[2], // Farbe
                // Index: 1
                p3[0], p3[1], p3[2], // Position
                nf[0], nf[1], nf[2], // Normals
                c[0], c[1], c[2], // Farbe
                // Index: 2
                p1[0], p1[1], p1[2], // Position
                nf[0], nf[1], nf[2], // normal
                c[0], c[1], c[2], // Farbe
                // Index: 3
                p2[0], p2[1], p2[2], // Position
                nf[0], nf[1], nf[2], // normal
                c[0], c[1], c[2], // Farbe

                // back surface
                // Index: 4
                p5[0], p5[1], p5[2], // Position
                nb[0], nb[1], nb[2], // normal
                c[0], c[1], c[2], // Farbe
                // Index: 5
                p6[0], p6[1], p6[2], // Position
                nb[0], nb[1], nb[2], // normal
                c[0], c[1], c[2], // Farbe
                // Index: 6
                p4[0], p4[1], p4[2], // Position
                nb[0], nb[1], nb[2], // normal
                c[0], c[1], c[2], // Farbe
                // Index: 7
                p7[0], p7[1], p7[2], // Position
                nb[0], nb[1], nb[2], // normal
                c[0], c[1], c[2], // Farbe

                // left surface
                // Index: 8
                p4[0], p4[1], p4[2], // Position
                nl[0], nl[1], nl[2], // normal
                c[0], c[1], c[2], // Farbe
                // Index: 9
                p7[0], p7[1], p7[2], // Position
                nl[0], nl[1], nl[2], // normal
                c[0], c[1], c[2], // Farbe
                // Index: 10
                p0[0], p0[1], p0[2], // Position
                nl[0], nl[1], nl[2], // normal
                c[0], c[1], c[2], // Farbe
                // Index: 11
                p3[0], p3[1], p3[2], // Position
                nl[0], nl[1], nl[2], // normal
                c[0], c[1], c[2], // Farbe

                // right surface
                // Index: 12
                p1[0], p1[1], p1[2], // Position
                nr[0], nr[1], nr[2], // normal
                c[0], c[1], c[2], // Farbe
                // Index: 13
                p2[0], p2[1], p2[2], // Position
                nr[0], nr[1], nr[2], // normal
                c[0], c[1], c[2], // Farbe
                // Index: 14
                p5[0], p5[1], p5[2], // Position
                nr[0], nr[1], nr[2], // normal
                c[0], c[1], c[2], // Farbe
                // Index: 15
                p6[0], p6[1], p6[2], // Position
                nr[0], nr[1], nr[2], // normal
                c[0], c[1], c[2], // Farbe

                // top surface
                // Index: 16
                p4[0], p4[1], p4[2], // Position
                nu[0], nu[1], nu[2], // normal
                c[0], c[1], c[2], // Farbe
                // Index: 17
                p0[0], p0[1], p0[2], // Position
                nu[0], nu[1], nu[2], // normal
                c[0], c[1], c[2], // Farbe
                // Index: 18
                p5[0], p5[1], p5[2], // Position
                nu[0], nu[1], nu[2], // normal
                c[0], c[1], c[2], // Farbe
                // Index: 19
                p1[0], p1[1], p1[2], // Position
                nu[0], nu[1], nu[2], // normal
                c[0], c[1], c[2], // Farbe

                // bottom surface
                // Index: 20
                p3[0], p3[1], p3[2], // Position
                nd[0], nd[1], nd[2], // normal
                c[0], c[1], c[2], // Farbe
                // Index: 21
                p7[0], p7[1], p7[2], // Position
                nd[0], nd[1], nd[2], // normal
                c[0], c[1], c[2], // Farbe
                // Index: 22
                p2[0], p2[1], p2[2], // Position
                nd[0], nd[1], nd[2], // normal
                c[0], c[1], c[2], // Farbe
                // Index: 23
                p6[0], p6[1], p6[2], // Position
                nd[0], nd[1], nd[2], // normal
                c[0], c[1], c[2], // Farbe
        };
        return verticies;
    }

    /**
     * Erstellt 28 Indices für eine Box.
     * @return Indices für das Vertex Array
     */
    private static int[] makeBoxIndices() {
        int[] indices = {
                21, 23, 20, 22, // down (bottom)
                1, 3, 0, 2, 2, 3, // front
                12, 13, 14, 15, // right
                4, 5, 6, 7, // back
                8, 9, 10, 11, 10, 10, // left
                16, 17, 18, 19 // up (top)
        };
        return indices;
    }
}
