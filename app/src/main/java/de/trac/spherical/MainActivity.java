package de.trac.spherical;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
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
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.IOException;

import de.trac.spherical.parser.PhotoSphereMetadata;
import de.trac.spherical.parser.PhotoSphereParser;
import de.trac.spherical.rendering.PhotoSphereSurfaceView;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "Spherical";
    public static final String MIME_PHOTO_SPHERE = "application/vnd.google.panorama360+jpg";
    private static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 387;

    private FragmentManager fm;

    //UI
    private FloatingActionButton fab;
    private Toolbar toolbar;
    private GestureDetectorCompat gestureDetector;

    private ProgressFragment progressFragment = new ProgressFragment();
    private FlatFragment flatFragment = new FlatFragment();
    private SphereFragment sphereFragment = new SphereFragment();
    private ImageFragment currentlyShownImageFragment;

    //Cache
    private Intent cachedIntent;
    private Bitmap bitmap;
    private PhotoSphereMetadata metadata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupUI();

        fm = getSupportFragmentManager();

        handleIntent(getIntent());
    }

    private void showProgressFragment() {
        fm.beginTransaction().replace(R.id.container_fragment, progressFragment, "prog").commit();
        this.currentlyShownImageFragment = null;
    }

    private void showFlatImageFragment() {
        fm.beginTransaction().replace(R.id.container_fragment, flatFragment, "flat").commit();
        this.currentlyShownImageFragment = flatFragment;
    }

    private void showSphereFragment() {
        fm.beginTransaction().replace(R.id.container_fragment, sphereFragment, "sphere").commit();
        this.currentlyShownImageFragment = sphereFragment;
    }

    private void setupUI() {
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
                PhotoSphereSurfaceView.USE_TOUCH = !PhotoSphereSurfaceView.USE_TOUCH;
                displayUI(false);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        // Detect gestures like single taps.
        gestureDetector = new GestureDetectorCompat(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent event) {
                displayUI(!fab.isShown());
                return true;
            }

        });
    }

    private void handleIntent(Intent intent) {
        switch (intent.getAction()) {
            //Image was sent into the app
            case Intent.ACTION_SEND:
                showProgressFragment();
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
            return;
        }

        // Cache intent and request permission
        this.cachedIntent = intent;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
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
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_about:
                Toast.makeText(this, R.string.toast_not_yet_implemented, Toast.LENGTH_SHORT).show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Distinguish type of sent bitmap. Images with the MIME type of a photosphere will be directly
     * displayed, while images with MIME type bitmap/* are being manually tested using {@link PhotoSphereParser}.
     * @param intent incoming intent.
     */
    void handleSentImageIntent(Intent intent) {
        if (intent == null) {
            throw new AssertionError("Intent is null!");
        }
        String type = intent.getType();
        if (type != null) {

            Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (imageUri == null) {
                Toast.makeText(this, R.string.toast_file_not_found, Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "START LOADING BITMAP");
            try {
                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                metadata = PhotoSphereParser.parse(getContentResolver().openInputStream(imageUri));
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "FINISHED LOADING BITMAP");

            switch (type) {
                case MIME_PHOTO_SPHERE:
                    displayPhotoSphere();
                    break;

                default:
                    if (metadata != null) {
                        displayPhotoSphere();
                    } else {
                        displayFlatImage();
                    }
                    break;
            }

        } else {
            Toast.makeText(this, "TODO: Figure out what to do :D", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Display a photo sphere.
     */
    public void displayPhotoSphere() {
        showSphereFragment();
        currentlyShownImageFragment.updateBitmap(bitmap);
    }

    /**
     * Display a flat bitmap.
     */
    public void displayFlatImage() {
        showFlatImageFragment();
        currentlyShownImageFragment.updateBitmap(bitmap);
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public GestureDetectorCompat getGestureDetector() {
        return gestureDetector;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    private class HandleSentImageTask extends AsyncTask<Intent, Void, Void> {

        @Override
        protected Void doInBackground(Intent... params) {
            handleSentImageIntent(params[0]);

            return null;
        }
    }
}
