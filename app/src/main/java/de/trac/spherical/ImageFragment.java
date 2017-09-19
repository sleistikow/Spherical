package de.trac.spherical;

import android.graphics.Bitmap;
import android.support.v4.app.Fragment;

/**
 * Created by vanitas on 19.09.17.
 */

public abstract class ImageFragment extends Fragment {

    public abstract void updateBitmap(Bitmap bitmap);
}
