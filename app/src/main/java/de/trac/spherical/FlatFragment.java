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

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Fragment containing an ImageView which displays the unfolded image.
 */
public class FlatFragment extends ImageFragment {

    private static final String TAG = "SphericalFFrag";

    @BindView(R.id.image_view)
    SubsamplingScaleImageView imageView;

    private Bitmap bitmap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_flat, parent, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated");
        updateBitmap(bitmap);

        BroadcastHelper.broadcast(getContext(), BroadcastHelper.BroadcastType.PROGRESS_FINISHED);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_flat, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void updateBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        if (imageView != null && bitmap != null) {
            imageView.setImage(ImageSource.cachedBitmap(bitmap));
        }
    }
}
