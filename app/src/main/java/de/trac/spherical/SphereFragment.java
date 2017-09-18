package de.trac.spherical;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import de.trac.spherical.rendering.PhotoSphereSurfaceView;

/**
 * Created by vanitas on 17.09.17.
 */
public class SphereFragment extends Fragment implements View.OnTouchListener {

    private PhotoSphereSurfaceView surfaceView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sphere, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        FrameLayout fragmentRoot = (FrameLayout) view.findViewById(R.id.container_sphere);
        surfaceView = new PhotoSphereSurfaceView(getContext());

        // Initialize renderer and setup surface view.
        fragmentRoot.addView(surfaceView);

        surfaceView.setOnTouchListener(this);
        surfaceView.setBitmap(getMainActivity().getBitmap());
    }

    public void displayPhotoSphere(Bitmap bitmap) {
        //surfaceView.setBitmap(bitmap);
        Log.d(MainActivity.TAG, "Display Photo Sphere!");
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return getMainActivity().getGestureDetector().onTouchEvent(event);
    }

    private MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }
}
