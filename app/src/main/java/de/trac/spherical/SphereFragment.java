package de.trac.spherical;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import de.trac.spherical.rendering.PhotoSphereSurfaceView;

/**
 * Fragment containing a PhotoSphereSurfaceView which displays the image projected on a sphere.
 */
public class SphereFragment extends ImageFragment implements View.OnTouchListener {

    private static final String TAG = "SphericalSFrag";

    private PhotoSphereSurfaceView surfaceView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        return inflater.inflate(R.layout.fragment_sphere, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated");
        setHasOptionsMenu(true);
        FrameLayout fragmentRoot = (FrameLayout) view.findViewById(R.id.container_sphere);
        surfaceView = new PhotoSphereSurfaceView(getContext());

        // Initialize renderer and setup surface view.
        fragmentRoot.addView(surfaceView);

        surfaceView.setOnTouchListener(this);
        updateBitmap(getMainActivity().getBitmap());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return getMainActivity().getGestureDetector().onTouchEvent(event);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_sphere, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_force_flat:
                getMainActivity().displayFlatImage();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    @Override
    public void updateBitmap(Bitmap bitmap) {
        if (surfaceView == null) {
            return;
        }
        surfaceView.setBitmap(bitmap);
    }

    public void toggleUseTouchInput() {
        surfaceView.setUseTouchInput(!surfaceView.getUseTouchInput());
    }
}
