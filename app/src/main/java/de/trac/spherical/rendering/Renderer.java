package de.trac.spherical.rendering;


import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;
import static android.opengl.GLUtils.getEGLErrorString;

public class Renderer implements GLSurfaceView.Renderer {

    /**
     * Default vertex shader.
     *
     * In:  pos
     *      uvw
     * Out: uv
     */
    private static final String DEFAULT_VERTEX_SHADER =
                    "uniform mat4 mvpMatrix;\n" +
                    "attribute vec3 position;\n" +
                    "attribute vec2 textureCoordinates;\n" +
                    "varying vec2 uv;\n" +
                    "void main() {\n" +
                    "  gl_Position = mvpMatrix * vec4(position, 1);\n" +
                    "  uv = textureCoordinates;\n" +
                    "}\n";

    /**
     * Default fragment shader.
     *
     * In:  uv
     * Out: sets fragment color
     */
    private static final String DEFAULT_FRAGMENT_SHADER =
                    "varying vec2 uv;\n" +
                    "uniform sampler2D tex;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture2D(tex, uv);\n" +
                    "}\n";

    // Store a sphere geometry as framework for the photo texture.
    private Sphere sphere = null;

    // Store projection matrix.
    private float projMatrix [] = new float [16];

    // Store modelview matrix.
    private float modlMatrix [] = new float [16];

    // Store view matrix.
    private float viewMatrix [] = new float [16];

    // Store the model view projection matrix.
    private float mvpMatrix [] = new float [16];

    // Store shader name.
    private int programID;

    // Store shader locations.
    private int positionLocation;
    private int textureCoordinatesLocation;
    private int mvpLocation;
    private int texLocation;

    // Store texture.
    private final int textureID [] = new int[1];

    // Store bitmap for lazy loading.
    private Bitmap bitmap = null;

    // Store input handler instance to determine transformation.
    private SphereSurfaceView surfaceView;

    /**
     * Constructor. Will be set as renderer of the specified surface view.
     * @param surfaceView SurfaceView which will own the renderer
     */
    public Renderer(SphereSurfaceView surfaceView) {
        if(surfaceView == null)
            throw new NullPointerException("SurfaceView must not be null");

        this.surfaceView = surfaceView;
        this.surfaceView.setEGLContextClientVersion(2);
        this.surfaceView.setRenderer(this);
    }

    /**
     * Draws the frame.
     * @param unused unused
     */
    public void onDrawFrame(GL10 unused) {

        // Upload texture, if necessary.
        if(bitmap != null) {
            glBindTexture(GL_TEXTURE_2D, textureID[0]);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
            glBindTexture(GL_TEXTURE_2D, 0);

            // Release bitmap for garbage collection.
            bitmap = null;
        }

        // Update transformation matrix.
        Matrix.multiplyMM(mvpMatrix, 0, surfaceView.getRotationMatrix(), 0, modlMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, projMatrix, 0, mvpMatrix, 0);

        // Draw the frame.
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glUseProgram(programID);

        glEnableVertexAttribArray(positionLocation);
        glVertexAttribPointer(positionLocation, 3, GL_FLOAT, false, 3*4, sphere.getVertexBuffer());

        glEnableVertexAttribArray(textureCoordinatesLocation);
        glVertexAttribPointer(textureCoordinatesLocation, 2, GL_FLOAT, false, 2*4, sphere.getTextureCoordinatesBuffer());

        glUniformMatrix4fv(mvpLocation, 1, false, mvpMatrix, 0);
        glUniform1i(texLocation, 0);
        glBindTexture(GL_TEXTURE_2D, textureID[0]);
        glDrawElements(GL_TRIANGLES, sphere.getIndexBuffer().capacity(), GL_UNSIGNED_SHORT, sphere.getIndexBuffer());
        glBindTexture(GL_TEXTURE_2D, 0);

        glDisableVertexAttribArray(textureCoordinatesLocation);
        glDisableVertexAttribArray(positionLocation);

        glUseProgram(0);
    }

    /**
     * Callback called if surface changed.
     *
     * @param unused unused
     * @param width new width of the surface
     * @param height new height of the surface
     */
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        Matrix.perspectiveM(projMatrix, 0, 45.0f, ratio, 0.25f, 128.0f);
    }

    /**
     * Callback called if surface has been created.
     *
     * @param unused unused
     * @param config surface configuration
     */
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        // Initialize sphere.
        sphere = new Sphere(10.0f, 32, 32); // TODO: choose useful parameters.

        // Set OpenGL state.
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glFrontFace(GL_CW);
        glActiveTexture(GL_TEXTURE0);

        // Build shader program.
        programID = buildProgram(DEFAULT_VERTEX_SHADER, DEFAULT_FRAGMENT_SHADER);

        // Generate texture.
        glGenTextures(1, textureID, 0);

        // Initialize matrices.
        Matrix.setRotateM(modlMatrix, 0, 90, 1.0f, 0.0f, 0.0f);
        Matrix.setLookAtM(viewMatrix, 0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f);
    }

    /**
     * Uploads the image data of the given bitmap into the internal texture.
     * @param bitmap Bitmap to be set as texture
     */
    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    /**
     * Builds a shader program given vertex and fragment shader soruce.
     * @param vertexSource The vertex shader source
     * @param fragmentSource The fragment shader source
     * @return shader program
     */
    private int buildProgram(String vertexSource, String fragmentSource) {

        int vertexShader = buildShader(GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            throw new RuntimeException("vertex shader could not be loaded");
        }

        int fragmentShader = buildShader(GL_FRAGMENT_SHADER, fragmentSource);
        if (fragmentShader == 0) {
            throw new RuntimeException("fragment shader could not be loaded");
        }

        int program = glCreateProgram();
        if (program != 0) {
            glAttachShader(program, vertexShader);
            glAttachShader(program, fragmentShader);
            glLinkProgram(program);
            int[] linkStatus = new int[1];
            glGetProgramiv(program, GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GL_TRUE) {
                Log.e("Shader", "Could not link program: ");
                Log.e("Shader", glGetProgramInfoLog(program));
                glDeleteProgram(program);
                throw new RuntimeException("Could not link program");
            }
        }

        positionLocation = glGetAttribLocation(program, "position");
        if (positionLocation == -1) {
            throw new RuntimeException("Could not get attribute location for 'position'");
        }
        textureCoordinatesLocation = glGetAttribLocation(program, "textureCoordinates");
        if (textureCoordinatesLocation == -1) {
            throw new RuntimeException("Could not get attribute location for 'textureCoordinates'");
        }
        mvpLocation = glGetUniformLocation(program, "mvpMatrix");
        if (mvpLocation == -1) {
            throw new RuntimeException("Could not get uniform location for 'mvpMatrix'");
        }
        texLocation = glGetUniformLocation(program, "tex");
        if(texLocation == -1) {
            throw new RuntimeException("Could not get uniform location for 'tex'");
        }

        return program;
    }

    /**
     * Builds a shader of a specified type from a given source.
     * @param type The shader type.
     * @param source The shader source
     * @return shader name
     */
    private int buildShader(int type, String source) {
        int shader = glCreateShader(type);
        if (shader != 0) {
            glShaderSource(shader, source);
            glCompileShader(shader);
            int[] compiled = new int[1];
            glGetShaderiv(shader, GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e("Shader", "Could not compile shader " + type + ":");
                Log.e("Shader", glGetShaderInfoLog(shader));
                glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }
}
