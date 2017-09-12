package de.trac.spherical.rendering;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * This class is used to create native buffers holding vertices and
 * texture coordinates of a sphere with a given radius.
 */
public class Sphere {

    // The following attributes make up our sphere.
    private FloatBuffer vertexBuffer;
    private FloatBuffer textureCoordinatesBuffer;
    private ShortBuffer indexBuffer;

    public Sphere(float radius, int polyCountX, int polyCountY) {

        // Setup vertex buffer.
        ByteBuffer buffer = ByteBuffer.allocateDirect((polyCountX*polyCountY+2)*2*3*4);
        buffer.order(ByteOrder.nativeOrder());
        vertexBuffer = buffer.asFloatBuffer();

        // Setup texture coordinate buffer.
        buffer = ByteBuffer.allocateDirect((polyCountX*polyCountY+2)*2*2*4);
        buffer.order(ByteOrder.nativeOrder());
        textureCoordinatesBuffer = buffer.asFloatBuffer();

        // Setup index buffer.
        buffer = ByteBuffer.allocateDirect(polyCountX*polyCountY*6*2);
        buffer.order(ByteOrder.nativeOrder());
        indexBuffer = buffer.asShortBuffer();
        
        int polyCountXPitch = polyCountX+1; // get to same vertex on next level
        
        int level = 0;

        for (int p1 = 0; p1 < polyCountY-1; p1++) {
            //main quads, top to bottom
            for (int p2 = 0; p2 < polyCountX - 1; p2++)
            {
			    final int curr = level + p2;
                indexBuffer.put((short)(curr + polyCountXPitch));
                indexBuffer.put((short)(curr));
                indexBuffer.put((short)(curr + 1));
                indexBuffer.put((short)(curr + polyCountXPitch));
                indexBuffer.put((short)(curr+1));
                indexBuffer.put((short)(curr + 1 + polyCountXPitch));
            }

            // the connectors from front to end
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

        for (int p2 = 0; p2 < polyCountX - 1; p2++) {
            // create triangles which are at the top of the sphere

            indexBuffer.put((short)(polyCountSq));
            indexBuffer.put((short)(p2 + 1));
            indexBuffer.put((short)(p2));

            // create triangles which are at the bottom of the sphere

            indexBuffer.put((short)(polyCountSqM1 + p2));
            indexBuffer.put((short)(polyCountSqM1 + p2 + 1));
            indexBuffer.put((short)(polyCountSq1));
        }

        // create final triangle which is at the top of the sphere

        indexBuffer.put((short)(polyCountSq));
        indexBuffer.put((short)(polyCountX));
        indexBuffer.put((short)(polyCountX-1));

        // create final triangle which is at the bottom of the sphere

        indexBuffer.put((short)(polyCountSqM1 + polyCountX - 1));
        indexBuffer.put((short)(polyCountSqM1));
        indexBuffer.put((short)(polyCountSq1));

        // calculate the angle which separates all points in a circle
        final double AngleX = 2.0 * Math.PI / polyCountX;
        final double AngleY = Math.PI / polyCountY;

        int i=0;
        double axz;

        // we don't start at 0.
        double ay = 0;//AngleY / 2;
        for (int y = 0; y < polyCountY; y++) {
            ay += AngleY;
		    final double sinay = Math.sin(ay);
            axz = 0;

            // calculate the necessary vertices without the doubled one
            for (int xz = 0; xz < polyCountX; xz++)
            {
                float rx = (float) (radius * Math.cos(axz) * sinay);
                float ry = (float) (radius * Math.cos(ay));
                float rz = (float) (radius * Math.sin(axz) * sinay);

                // calculate texture coordinates via sphere mapping
                // tu is the same on each level, so only calculate once
                float tu = 0.5f;
                if (y==0)
                {
                    if (ry != -1.0f && ry != 1.0f) {
                        float len = (float) Math.sqrt(rx*rx + ry*ry + rz*rz);
                        tu = (float) (Math.acos(Math.max(Math.min(rx / len / sinay, 1.0), -1.0)) * 0.5 / Math.PI);
                    }
                    if (rz < 0.0f)
                        tu=1-tu;
                }
                else
                    tu = textureCoordinatesBuffer.get((i-polyCountXPitch)*2);

                vertexBuffer.put(rx);
                vertexBuffer.put(ry);
                vertexBuffer.put(rz);
                textureCoordinatesBuffer.put(tu);
                textureCoordinatesBuffer.put((float)(ay/Math.PI));

                i++;
                axz += AngleX;
            }
            // This is the doubled vertex on the initial position
            vertexBuffer.put(vertexBuffer.get((i-polyCountX)*3 + 0));
            vertexBuffer.put(vertexBuffer.get((i-polyCountX)*3 + 1));
            vertexBuffer.put(vertexBuffer.get((i-polyCountX)*3 + 2));
            textureCoordinatesBuffer.put(1.0f);
            textureCoordinatesBuffer.put(0.0f);
            i++;
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
