package de.trac.spherical.rendering;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.view.MotionEvent;

/**
 * This SurfaceView implementation is the glue between the Renderer and any input event.
 */
public class SphereSurfaceView extends GLSurfaceView implements SensorEventListener {

    public static boolean USE_TOUCH = false; // TODO: determine dynamically

    private final float TOUCH_SCALE_FACTOR = 180.0f / 1080;
    private float previousX;
    private float previousY;

    // The actual rotation matrix determined by user input.
    private final float rotationMatrix [] = new float[16];

    // This matrix is used to compensate sensor coordinate system rotation.
    private final float offsetMatrix [] = new float[16];

    public SphereSurfaceView(Context context) {
        super(context);

        Matrix.setIdentityM(rotationMatrix, 0);
        Matrix.setRotateM(offsetMatrix, 0, 90.0f, 1.0f, 0.0f, 0.0f);

        SensorManager manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = manager.getSensorList(Sensor.TYPE_ROTATION_VECTOR).get(0);
        manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(!USE_TOUCH)
            return true;

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = x - previousX;
                float dy = y - previousY;

                if (y > getHeight() / 2)
                    dx = dx * -1 ;

                if (x < getWidth() / 2)
                    dy = dy * -1 ;

                synchronized (rotationMatrix) {
                    Matrix.rotateM(rotationMatrix, 0, dy * TOUCH_SCALE_FACTOR, 1.0f, 0.0f, 0.0f);
                    Matrix.rotateM(rotationMatrix, 0, dx * TOUCH_SCALE_FACTOR, 0.0f, 1.0f, 0.0f);
                }
        }

        previousX = x;
        previousY = y;
        return true;

    }

    @Override
    public void onAccuracyChanged(Sensor s, int arg1) {
        // unused
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(USE_TOUCH)
            return;

        synchronized (rotationMatrix) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            Matrix.multiplyMM(rotationMatrix, 0, rotationMatrix, 0, offsetMatrix, 0);
        }
    }

    /**
     * Returns a matrix representing the devices rotation.
     * This function is thread safe.
     * @return rotation matrix according to device rotation
     */
    public float [] getRotationMatrix() {
        synchronized (rotationMatrix) {
            return rotationMatrix;
        }
    }
}
