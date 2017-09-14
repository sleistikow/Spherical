package de.trac.spherical;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import de.trac.spherical.parser.PhotoSphereMetadata;
import de.trac.spherical.parser.PhotoSphereParser;
import de.trac.spherical.rendering.Renderer;
import de.trac.spherical.rendering.SphereSurfaceView;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "Spherical";

    public static final String MIME_PHOTO_SPHERE = "application/vnd.google.panorama360+jpg";
    public static final String MIME_IMAGE = "image/*";

    private static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 387;

    private SphereSurfaceView surfaceView;
    private Renderer renderer;
    private FloatingActionButton fab;
    private Toolbar toolbar;

    private Intent cachedIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Prepare UI
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) toolbar.getLayoutParams();
        lp.topMargin += getStatusBarHeight();
        toolbar.bringToFront();
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SphereSurfaceView.USE_TOUCH = !SphereSurfaceView.USE_TOUCH;
                displayUI(false);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        // Initialize renderer and setup surface view.
        LinearLayout container = (LinearLayout) findViewById(R.id.container);
        surfaceView = new SphereSurfaceView(this);
        container.addView(surfaceView);
        renderer = new Renderer(surfaceView);

        // Detect gestures like single taps.
        final GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent event) {
                displayUI(!fab.isShown());
                return true;
            }

        });

        surfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        switch (intent.getAction()) {
            //Image was sent into the app
            case Intent.ACTION_SEND:
                checkPermissionAndHandleSentImage(intent);
                break;

            //App was launched via launcher icon
            //TODO: Remove later together with launcher intent filter
            default:
                Toast.makeText(this, R.string.toast_prompt_share_image, Toast.LENGTH_LONG).show();
        }
    }

    private void checkPermissionAndHandleSentImage(Intent intent) {
        int status = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (status == PackageManager.PERMISSION_GRANTED) {
            handleSentImageIntent(intent);
        }

        // Cache intent and request permission
        this.cachedIntent = intent;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    handleSentImageIntent(cachedIntent);
                } else {
                    Toast.makeText(this, R.string.toast_missing_permission, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void displayUI(boolean display) {
        if (display) {
            fab.show();
            toolbar.setVisibility(View.VISIBLE);
        } else {
            fab.setVisibility(View.INVISIBLE);
            toolbar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        displayUI(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_force_sphere:
                Toast.makeText(this, R.string.toast_not_yet_implemented, Toast.LENGTH_SHORT).show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Distinguish type of sent image. Images with the MIME type of a photosphere will be directly
     * displayed, while images with MIME type image/* are being manually tested using {@link PhotoSphereParser}.
     * @param intent incoming intent.
     */
    private void handleSentImageIntent(Intent intent) {
        String type = intent.getType();
        if (type != null) {

            Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (imageUri == null) {
                Toast.makeText(this, R.string.toast_file_not_found, Toast.LENGTH_SHORT).show();
                return;
            }

            switch (type) {
                case MIME_PHOTO_SPHERE:
                    displayPhotoSphere(imageUri);
                    break;

                default:
                    displayMaybePhotoSphere(imageUri);
                    break;
            }

        } else {
            Toast.makeText(this, "TODO: Figure out what to do :D", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Check, whether the sent photo is a photo sphere and display either a sphere, or a plain image.
     * @param uri
     */
    private void displayMaybePhotoSphere(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            String xml = PhotoSphereParser.getXMLContent(inputStream);
            PhotoSphereMetadata metadata = PhotoSphereParser.parse(xml);

            if (metadata == null || !metadata.isUsePanoramaViewer()) {
                displayFlatImage(getContentResolver().openInputStream(uri));
            } else {
                displayPhotoSphere(getContentResolver().openInputStream(uri), metadata);
            }

        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found.", e);
            Toast.makeText(this, R.string.toast_file_not_found, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "IOException: ", e);
            Toast.makeText(this, R.string.toast_io_error, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Display a photo sphere.
     * @param uri
     */
    private void displayPhotoSphere(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            String xml = PhotoSphereParser.getXMLContent(inputStream);
            PhotoSphereMetadata metadata = PhotoSphereParser.parse(xml);

            if (metadata == null) {
                Log.e(TAG, "Metadata is null. Fall back to flat image.");
                displayFlatImage(getContentResolver().openInputStream(uri));
            }

            displayPhotoSphere(getContentResolver().openInputStream(uri), metadata);

        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found.", e);
            Toast.makeText(this, R.string.toast_file_not_found, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "IOException: ", e);
            Toast.makeText(this, R.string.toast_io_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void displayPhotoSphere(InputStream inputStream, PhotoSphereMetadata metadata) {
        renderer.setBitmap(BitmapFactory.decodeStream(inputStream));
        Log.d(TAG, "Display Photo Sphere!");
    }

    /**
     * Display a flat image.
     * @param inputStream
     */
    private void displayFlatImage(InputStream inputStream) {
        Log.d(TAG, "Display Flat Image!");
        displayPhotoSphere(inputStream, new PhotoSphereMetadata());
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

}
