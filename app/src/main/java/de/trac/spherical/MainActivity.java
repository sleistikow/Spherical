package de.trac.spherical;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.trac.spherical.parser.PhotoSphereParser;
import de.trac.spherical.rendering.PhotoSphereSurfaceView;
import de.trac.spherical.util.LoadImageTask;


public class MainActivity extends AppCompatActivity implements LoadImageTask.FinishedCallback {

    public static final String TAG = "Spherical";
    public static final String MIME_PHOTO_SPHERE = "application/vnd.google.panorama360+jpg";
    private static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 387;

    // UI
    @BindView(R.id.fab)
    FloatingActionButton actionButton;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private GestureDetectorCompat gestureDetector;

    // Cache
    private Intent cachedIntent;
    private LoadImageTask.Result image;

    @Override
    public void onImageLoadingFinished(LoadImageTask.Result result) {
        this.image = result;
        if (result.getMetadata() == null) {
            showFragment(FragmentType.FLAT);
        } else {
            showFragment(FragmentType.SPHERE);
        }
        ((ImageFragment) currentFragment).updateBitmap(result.getBitmap());
    }

    // Fragments
    private enum FragmentType {
        PROGRESS("progress"),
        SPHERE("sphere"),
        FLAT("flat");

        /// Optional tag.
        String tag;

        FragmentType(String tag) {
            this.tag = tag;
        }
    }

    private FragmentManager fragmentManager;
    private Map<FragmentType, Fragment> fragments;
    private Fragment currentFragment;

    // Broadcast handling.
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (BroadcastHelper.getBroadcastType(intent)) {
                case PROGRESS_START:
                    fragmentManager.beginTransaction().add(R.id.container_fragment, fragments.get(FragmentType.PROGRESS)).commitAllowingStateLoss();
                    break;
                case PROGRESS_FINISHED:
                    fragmentManager.beginTransaction().remove(fragments.get(FragmentType.PROGRESS)).commitAllowingStateLoss();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setupUI();

        // Init fragments.
        fragmentManager = getSupportFragmentManager();

        fragments = new HashMap<>();
        fragments.put(FragmentType.PROGRESS, new ProgressFragment());
        fragments.put(FragmentType.SPHERE, new SphereFragment());
        fragments.put(FragmentType.FLAT, new FlatFragment());

        // Intent handling.
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, BroadcastHelper.INTENT_FILTER);
        handleIntent(getIntent());
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    /**
     * Initialize the user interface.
     */
    private void setupUI() {
        // Prepare UI
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Dirty hacks
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) toolbar.getLayoutParams();
        lp.topMargin += getStatusBarHeight();
        toolbar.bringToFront();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoSphereSurfaceView photoSphereSurfaceView =
                        ((SphereFragment)fragments.get(FragmentType.SPHERE)).getSurfaceView();
                photoSphereSurfaceView.setUseTouchInput(!photoSphereSurfaceView.getUseTouchInput());
                setUIVisibility(false);
            }
        });

        // Detect gestures like single taps.
        gestureDetector = new GestureDetectorCompat(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent event) {
                setUIVisibility(!actionButton.isShown());
                return true;
            }

        });
    }

    /**
     * Handle an incoming intent. Distinguish between actions and pass the intent down to respective methods.
     * @param intent incoming intent.
     */
    private void handleIntent(Intent intent) {
        String action = intent.getAction() != null ? intent.getAction() : "";
        switch (action) {
            //Image was sent into the app
            case Intent.ACTION_SEND:
                showFragment(FragmentType.SPHERE);
                checkPermissionAndHandleSentImage(intent);
                break;

            //App was launched via launcher icon
            //TODO: Remove later together with launcher intent filter
            default:
                Toast.makeText(this, R.string.toast_prompt_share_image, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Check, if we are allowed to access external storage. If we are, then handle the intent.
     * Otherwise cache the intent and prompt the user to grant us access.
     * @param intent incoming intent.
     */
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
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

    /**
     * Distinguish type of sent bitmap. Images with the MIME type of a photosphere will be directly
     * displayed, while images with MIME type bitmap/* are being manually tested using {@link PhotoSphereParser}.
     * @param intent incoming intent.
     */
    void handleSentImageIntent(Intent intent) {
        if (intent == null) {
            throw new AssertionError("Intent is null!");
        }

        final String type = intent.getType();
        if (type == null) {
            Toast.makeText(this, "TODO: Figure out what to do :D", Toast.LENGTH_SHORT).show();
            return;
        }

        final Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri == null) {
            Toast.makeText(this, R.string.toast_file_not_found, Toast.LENGTH_SHORT).show();
            return;
        }

        // process image asynchronous.
        BroadcastHelper.broadcast(this, BroadcastHelper.BroadcastType.PROGRESS_START);
        new LoadImageTask(getContentResolver(), imageUri, type, this).execute();
    }

    /**
     * Show/hide the FAB and toolbar.
     * @param visible show/hide
     */
    private void setUIVisibility(boolean visible) {
        if (visible) {
            actionButton.show();
            toolbar.setVisibility(View.VISIBLE);
        } else {
            actionButton.setVisibility(View.INVISIBLE);
            toolbar.setVisibility(View.GONE);
        }
    }

    /**
     * Will show the fragment of the given type.
     * @param type fragment to be shown
     */
    private void showFragment(FragmentType type) {
        currentFragment = fragments.get(type);
        fragmentManager.beginTransaction().replace(R.id.container_fragment, currentFragment, type.tag).commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        setUIVisibility(true);
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
            case R.id.menu_force_sphere:
                showFragment(FragmentType.SPHERE);
                ((ImageFragment) currentFragment).updateBitmap(image.getBitmap());
                return true;

            case R.id.menu_force_flat:
                showFragment(FragmentType.FLAT);
                ((ImageFragment) currentFragment).updateBitmap(image.getBitmap());
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Convenience method because android sux.
     * Returns the height of the status bar in dp.
     * @return height of status bar.
     */
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

}
