package de.trac.spherical.rendering;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * This class is used to create native buffers holding vertices and
 * texture coordinates of a sphere with a given radius.
 */
public class PhotoSphereGeometry {

    // The following attributes make up our sphere.
    private FloatBuffer vertexBuffer;
    private FloatBuffer textureCoordinatesBuffer;
    private ShortBuffer indexBuffer;

    /**
     * Initializes the native buffers with a sphere based on the specified parameters.
     * @param radius the sphere's radius
     * @param polyCountX the number of polygons around the sphere in x direction
     * @param polyCountY the number of polygons around the sphere in y direction
     */
    public PhotoSphereGeometry(float radius, int polyCountX, int polyCountY) {

        final int polyCountXPitch = polyCountX + 1;

        // Setup vertex buffer.
        ByteBuffer buffer = ByteBuffer.allocateDirect((polyCountXPitch*polyCountY+2)*3*4);
        buffer.order(ByteOrder.nativeOrder());
        vertexBuffer = buffer.asFloatBuffer();

        // Setup texture coordinate buffer.
        buffer = ByteBuffer.allocateDirect((polyCountXPitch*polyCountY+2)*2*4);
        buffer.order(ByteOrder.nativeOrder());
        textureCoordinatesBuffer = buffer.asFloatBuffer();

        // Setup index buffer.
        buffer = ByteBuffer.allocateDirect(polyCountX*polyCountY*6*2);
        buffer.order(ByteOrder.nativeOrder());
        indexBuffer = buffer.asShortBuffer();

        for (int p1 = 0, level = 0; p1 < polyCountY-1; p1++) {
            for (int p2 = 0; p2 < polyCountX-1; p2++) {
			    final int curr = level + p2;
                indexBuffer.put((short)(curr + polyCountXPitch));
                indexBuffer.put((short)(curr));
                indexBuffer.put((short)(curr + 1));

                indexBuffer.put((short)(curr + polyCountXPitch));
                indexBuffer.put((short)(curr + 1));
                indexBuffer.put((short)(curr + 1 + polyCountXPitch));
            }

            indexBuffer.put((short)(level + polyCountX - 1 + polyCountXPitch));
            indexBuffer.put((short)(level + polyCountX - 1));
            indexBuffer.put((short)(level + polyCountX));

            indexBuffer.put((short)(level + polyCountX - 1 + polyCountXPitch));
            indexBuffer.put((short)(level + polyCountX));
            indexBuffer.put((short)(level + polyCountX + polyCountXPitch));
            level += polyCountXPitch;
        }

	    final int polyCountSq = polyCountXPitch * polyCountY; // top point
	    final int polyCountSq1 = polyCountSq + 1; // bottom point
	    final int polyCountSqM1 = (polyCountY - 1) * polyCountXPitch; // last row's first vertex

        for (int p2 = 0; p2 < polyCountX-1; p2++) {
            indexBuffer.put((short)(polyCountSq));
            indexBuffer.put((short)(p2 + 1));
            indexBuffer.put((short)(p2));

            indexBuffer.put((short)(polyCountSqM1 + p2));
            indexBuffer.put((short)(polyCountSqM1 + p2 + 1));
            indexBuffer.put((short)(polyCountSq1));
        }

        indexBuffer.put((short)(polyCountSq));
        indexBuffer.put((short)(polyCountX));
        indexBuffer.put((short)(polyCountX-1));

        indexBuffer.put((short)(polyCountSqM1 + polyCountX - 1));
        indexBuffer.put((short)(polyCountSqM1));
        indexBuffer.put((short)(polyCountSq1));

        // calculate the angle which separates all points in a circle
        final double AngleX = 2.0 * Math.PI / polyCountX;
        final double AngleY = Math.PI / polyCountY;
        final double InvPI = 1.0 / Math.PI;

        double ay = 0;//AngleY / 2;
        for (int y = 0; y < polyCountY; y++) {

            ay += AngleY;
            double axz = 0;
            final double sinay = Math.sin(ay);
            final float tv = (float) (ay*InvPI);

            for (int xz = 0; xz < polyCountX; xz++) {

                double nx = Math.cos(axz) * sinay;
                double ny = Math.cos(ay);
                double nz = Math.sin(axz) * sinay;

                // calculate texture coordinates via sphere mapping
                // tu is the same on each level, so only calculate once
                float tu = 0.5f;
                if (y == 0) {
                    if (ny != -1.0 && ny != 1.0)
                        tu = (float) (Math.acos(Math.max(Math.min(nx / sinay, 1.0), -1.0)) * 0.5 * InvPI);
                    if (nz < 0.0)
                        tu = 1.0f - tu;
                } else
                    tu = textureCoordinatesBuffer.get(xz*2);

                vertexBuffer.put((float) (radius * nx));
                vertexBuffer.put((float) (radius * ny));
                vertexBuffer.put((float) (radius * nz));
                textureCoordinatesBuffer.put(tu);
                textureCoordinatesBuffer.put(tv);

                axz += AngleX;
            }

            vertexBuffer.put(vertexBuffer.get((y*polyCountXPitch)*3 + 0));
            vertexBuffer.put(vertexBuffer.get((y*polyCountXPitch)*3 + 1));
            vertexBuffer.put(vertexBuffer.get((y*polyCountXPitch)*3 + 2));
            textureCoordinatesBuffer.put(1.0f);
            textureCoordinatesBuffer.put(tv);
        }

        // Add the vertex at the top of the sphere.
        vertexBuffer.put(0.0f);
        vertexBuffer.put(radius);
        vertexBuffer.put(0.0f);
        textureCoordinatesBuffer.put(0.5f);
        textureCoordinatesBuffer.put(0.0f);

        // Add the vertex at the bottom of the sphere.
        vertexBuffer.put(0.0f);
        vertexBuffer.put(-radius);
        vertexBuffer.put(0.0f);
        textureCoordinatesBuffer.put(0.5f);
        textureCoordinatesBuffer.put(1.0f);

        // Rewind buffers.
        vertexBuffer.position(0);
        textureCoordinatesBuffer.position(0);
        indexBuffer.position(0);
    }

    public FloatBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    public FloatBuffer getTextureCoordinatesBuffer() {
        return textureCoordinatesBuffer;
    }

    public ShortBuffer getIndexBuffer() {
        return indexBuffer;
    }

}
