package de.trac.spherical;

import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import de.trac.spherical.rendering.Renderer;

public class MainActivity extends AppCompatActivity {

    private GLSurfaceView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        surfaceView = (GLSurfaceView) findViewById(R.id.surface_view);
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setRenderer(new Renderer());
    }
}
