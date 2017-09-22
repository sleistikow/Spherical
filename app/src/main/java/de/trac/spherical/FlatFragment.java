package de.trac.spherical;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

/**
 * Fragment containing an ImageView which displays the unfolded image.
 */
public class FlatFragment extends ImageFragment {

    private static final String TAG = "SphericalFFrag";

    private SubsamplingScaleImageView imageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        return inflater.inflate(R.layout.fragment_flat, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated");
        setHasOptionsMenu(true);
        imageView = (SubsamplingScaleImageView) view.findViewById(R.id.image_view);
        updateBitmap(getMainActivity().getBitmap());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_flat, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_force_sphere:
                getMainActivity().displayPhotoSphere();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    @Override
    public void updateBitmap(Bitmap bitmap) {
        if (imageView == null) {
            return;
        }
        imageView.setImage(ImageSource.cachedBitmap(bitmap));
    }
}
