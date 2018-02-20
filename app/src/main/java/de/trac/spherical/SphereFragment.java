package de.trac.spherical;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.trac.spherical.rendering.PhotoSphereSurfaceView;

/**
 * Fragment containing a PhotoSphereSurfaceView which displays the image projected on a sphere.
 */
public class SphereFragment extends ImageFragment implements View.OnTouchListener {

    private static final String TAG = "SphericalSFrag";

    @BindView(R.id.container_sphere)
    FrameLayout frameLayout;

    private PhotoSphereSurfaceView surfaceView;
    private Bitmap bitmap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sphere, parent, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        surfaceView = new PhotoSphereSurfaceView(getContext());

        // Initialize renderer and setup surface view.
        frameLayout.addView(surfaceView);
        surfaceView.setOnTouchListener(this);
        updateBitmap(bitmap);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        MainActivity activity = ((MainActivity)getActivity());
        if (activity != null) {
            return activity.getGestureDetector().onTouchEvent(event);
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_sphere, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void updateBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        if (surfaceView != null && bitmap != null) {
            surfaceView.setBitmap(bitmap);
        }
    }

    public PhotoSphereSurfaceView getSurfaceView() {
        return surfaceView;
    }
}
