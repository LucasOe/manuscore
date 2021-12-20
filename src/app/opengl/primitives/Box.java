package app.opengl.primitives;

import com.jogamp.opengl.GL3;

import app.opengl.Model;

public class Box extends Model {

    public Box(GL3 gl, int mode, float width, float height, float depth, float[] color) {
        super(
                gl,
                makeBoxVertices(width, height, depth, color),
                makeBoxIndices(),
                mode);
    }

    /**
    * Creates 24 vertices for a cuboid (box) with one single color
    * and correctly placed normal vectors.
    * The center of gravity of this shape is placed in the origin.
    * To be used together with makeBoxIndicesForTriangleStrip()
    * @param width with of box (x direction)
    * @param height height of box (y direction)
    * @param depth depth of box (z direction)
    * @param color three dimensional color vector for each vertex
    * @return list of vertices
    */
    private static float[] makeBoxVertices(float width, float height, float depth, float[] color) {
        float halfOfWidth = width / 2;
        float halfOfHeight = height / 2;
        float halfOfDepth = depth / 2;

        // Definition of positions of vertices for a cuboid
        float[] p0 = { -halfOfWidth, +halfOfHeight, +halfOfDepth }; // 0 front
        float[] p1 = { +halfOfWidth, +halfOfHeight, +halfOfDepth }; // 1 front
        float[] p2 = { +halfOfWidth, -halfOfHeight, +halfOfDepth }; // 2 front
        float[] p3 = { -halfOfWidth, -halfOfHeight, +halfOfDepth }; // 3 front
        float[] p4 = { -halfOfWidth, +halfOfHeight, -halfOfDepth }; // 4 back
        float[] p5 = { +halfOfWidth, +halfOfHeight, -halfOfDepth }; // 5 back
        float[] p6 = { +halfOfWidth, -halfOfHeight, -halfOfDepth }; // 6 back
        float[] p7 = { -halfOfWidth, -halfOfHeight, -halfOfDepth }; // 7 back

        // color vector
        float[] c = color;

        // Definition of normal vectors for cuboid surfaces
        float[] nf = { 0, 0, 1 }; // 0 front
        float[] nb = { 0, 0, -1 }; // 0 back
        float[] nl = { -1, 0, 0 }; // 0 left
        float[] nr = { 1, 0, 0 }; // 0 right
        float[] nu = { 0, 1, 1 }; // 0 up (top)
        float[] nd = { 0, -1, 1 }; // 0 down (bottom)

        // Cuboid vertices to be drawn as triangle stripes
        // Interlaces with color information and normal vectors
        float[] verticies = {
                // front surface
                // index: 0
                p0[0], p0[1], p0[2], // position
                c[0], c[1], c[2], // color
                nf[0], nf[1], nf[2], // normal
                // index: 1
                p3[0], p3[1], p3[2], // position
                c[0], c[1], c[2], // color
                nf[0], nf[1], nf[2], // normal
                // index: 2
                p1[0], p1[1], p1[2], // position
                c[0], c[1], c[2], // color
                nf[0], nf[1], nf[2], // normal
                // index: 3
                p2[0], p2[1], p2[2], // position
                c[0], c[1], c[2], // color
                nf[0], nf[1], nf[2], // normal

                // back surface
                // index: 4
                p5[0], p5[1], p5[2], // position
                c[0], c[1], c[2], // color
                nb[0], nb[1], nb[2], // normal
                // index: 5
                p6[0], p6[1], p6[2], // position
                c[0], c[1], c[2], // color
                nb[0], nb[1], nb[2], // normal
                // index: 6
                p4[0], p4[1], p4[2], // position
                c[0], c[1], c[2], // color
                nb[0], nb[1], nb[2], // normal
                // index: 7
                p7[0], p7[1], p7[2], // position
                c[0], c[1], c[2], // color
                nb[0], nb[1], nb[2], // normal

                // left surface
                // index: 8
                p4[0], p4[1], p4[2], // position
                c[0], c[1], c[2], // color
                nl[0], nl[1], nl[2], // normal
                // index: 9
                p7[0], p7[1], p7[2], // position
                c[0], c[1], c[2], // color
                nl[0], nl[1], nl[2], // normal
                // index: 10
                p0[0], p0[1], p0[2], // position
                c[0], c[1], c[2], // color
                nl[0], nl[1], nl[2], // normal
                // index: 11
                p3[0], p3[1], p3[2], // position
                c[0], c[1], c[2], // color
                nl[0], nl[1], nl[2], // normal

                // right surface
                // index: 12
                p1[0], p1[1], p1[2], // position
                c[0], c[1], c[2], // color
                nr[0], nr[1], nr[2], // normal
                // index: 13
                p2[0], p2[1], p2[2], // position
                c[0], c[1], c[2], // color
                nr[0], nr[1], nr[2], // normal
                // index: 14
                p5[0], p5[1], p5[2], // position
                c[0], c[1], c[2], // color
                nr[0], nr[1], nr[2], // normal
                // index: 15
                p6[0], p6[1], p6[2], // position
                c[0], c[1], c[2], // color
                nr[0], nr[1], nr[2], // normal

                // top surface
                // index: 16
                p4[0], p4[1], p4[2], // position
                c[0], c[1], c[2], // color
                nu[0], nu[1], nu[2], // normal
                // index: 17
                p0[0], p0[1], p0[2], // position
                c[0], c[1], c[2], // color
                nu[0], nu[1], nu[2], // normal
                // index: 18
                p5[0], p5[1], p5[2], // position
                c[0], c[1], c[2], // color
                nu[0], nu[1], nu[2], // normal
                // index: 19
                p1[0], p1[1], p1[2], // position
                c[0], c[1], c[2], // color
                nu[0], nu[1], nu[2], // normal

                // bottom surface
                // index: 20
                p3[0], p3[1], p3[2], // position
                c[0], c[1], c[2], // color
                nd[0], nd[1], nd[2], // normal
                // index: 21
                p7[0], p7[1], p7[2], // position
                c[0], c[1], c[2], // color
                nd[0], nd[1], nd[2], // normal
                // index: 22
                p2[0], p2[1], p2[2], // position
                c[0], c[1], c[2], // color
                nd[0], nd[1], nd[2], // normal
                // index: 23
                p6[0], p6[1], p6[2], // position
                c[0], c[1], c[2], // color
                nd[0], nd[1], nd[2], // normal
        };
        return verticies;
    }

    /**
     * Creates 28 indices for drawing a cuboid (box).
     * To be used together with makeBoxVertices()
     * To be used with "glDrawElements" and "GL_TRIANGLE_STRIP".
     * @return indices into the vertex array of the cube (box)
     */
    private static int[] makeBoxIndices() {
        // Indices to reference the number of the box vertices
        // defined in makeBoxVertices()
        int[] indices = {
                // Note: back faces are drawn,
                // but drawing is faster than using "GL_TRIANGLES"
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
