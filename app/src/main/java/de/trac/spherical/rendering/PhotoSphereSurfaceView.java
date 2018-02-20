package de.trac.spherical.rendering;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Build;
import android.view.MotionEvent;

/**
 * This SurfaceView implementation is the glue between the PhotoSphereRenderer and any input event.
 */
public class PhotoSphereSurfaceView extends GLSurfaceView implements SensorEventListener {

    // Decision variable for either using touch or device rotation input.
    private boolean useTouchInput = false;

    // The actual rotation matrix determined by user input.
    private final float rotationMatrix [] = new float[16];

    // The temporary rotation delta matrix.
    private final float tempMatrix [] = new float[16];

    // These vectors Are used for ray determination.
    private final float rayStart [] = new float[4];
    private final float rayDirection [] = new float[4];

    //
    private float oldAngleXZ, oldAngleY;

    // The renderer used by this view.
    private PhotoSphereRenderer renderer;

    /**
     * Constructor. Initializes Renderer.
     * @param context application context
     */
    public PhotoSphereSurfaceView(Context context) {
        super(context);

        // Initialize transformation matrix.
        Matrix.setIdentityM(rotationMatrix, 0);

        // Initialize sensors.
        SensorManager manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor;
        if (Build.VERSION.SDK_INT >= 18) {
            sensor = manager.getSensorList(Sensor.TYPE_GAME_ROTATION_VECTOR).get(0);
        } else {
            sensor = manager.getSensorList(Sensor.TYPE_ROTATION_VECTOR).get(0);
        }
        manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);

        // Initialize renderer.
        renderer = new PhotoSphereRenderer(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(!useTouchInput)
            return true;
/*
        // Retrieve ray in world space.
        renderer.getRay(event.getX(), event.getY(), rayStart, rayDirection);

        // Solve quadric equation.
        float a = 0.0f, b = 0.0f, c = 0.0f;
        for(int i=0; i<3; i++) {
            a += rayDirection[i] * rayDirection[i];
            b += rayDirection[i] * 2.0f * (rayStart[i]); // Sphere center at origin.
            c += rayStart[i]*rayStart[i];
        }
        c -= PhotoSphereRenderer.SPHERE_RADIUS*PhotoSphereRenderer.SPHERE_RADIUS;
        float D = b*b-4.0f*a*c;

        // Since the conditions are
        if(D < 0) {
            throw new RuntimeException("Ray must intersect with sphere, check camera position");
        }

        D = (float) Math.sqrt(D);

        // Calculate intersection point p.
        float t = -0.5f*(b+D)/a;
        float px = rayStart[0] + t*rayDirection[0];
        float py = rayStart[1] + t*rayDirection[1];
        float pz = rayStart[2] + t*rayDirection[2];

        synchronized (rotationMatrix) {
            Matrix.translateM(rotationMatrix, 0, px, py, pz);
        }
*/
        /*
        // Calculate angles.
        float angleY = (float) Math.toDegrees(Math.atan2(pz, px));
        float angleXZ = (float) Math.toDegrees(Math.acos(py));

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                oldAngleY = angleY;
                oldAngleXZ = angleXZ;
                System.arraycopy(getRotationMatrix(), 0, tempMatrix, 0, 16);
                break;
            case MotionEvent.ACTION_MOVE:

                synchronized (rotationMatrix) {
                    System.arraycopy(tempMatrix, 0, rotationMatrix, 0, 16);
                    //Matrix.rotateM(rotationMatrix, 0, oldAngleY-angleY, 0.0f, 1.0f, 0.0f);
                    Matrix.rotateM(rotationMatrix, 0, oldAngleXZ-angleXZ, 1.0f, 0.0f, 0.0f);
                    //Matrix.setLookAtM(rotationMatrix, 0, 0.0f, 0.0f, 0.0f, px, py, pz, 1.0f, 0.0f, 0.0f);
                }
                break;
        }
        */
        return true;
    }

    @Override
    public void onAccuracyChanged(Sensor s, int arg1) {
        // unused
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(useTouchInput)
            return;

        synchronized (rotationMatrix) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
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

    /**
     * Sets the bitmap to be rendered by the internal renderer.
     * @param bitmap bitmap to be rendered
     */
    public void setBitmap(Bitmap bitmap) {
        renderer.requestBitmapUpload(bitmap);
    }

    /**
     * Sets input to be used for transformation calculation.
     * @param useTouchInput true, if touch input should be used
     */
    public void setUseTouchInput(boolean useTouchInput) {
        this.useTouchInput = useTouchInput;
    }

    /**
     * Returns if touch input is used for transformation calculations.
     * @return true, if touch input is used, false
     */
    public boolean getUseTouchInput() {
        return useTouchInput;
    }
}
