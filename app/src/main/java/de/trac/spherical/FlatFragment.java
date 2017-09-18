package de.trac.spherical;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

/**
 * Created by vanitas on 17.09.17.
 */

public class FlatFragment extends Fragment {

    private SubsamplingScaleImageView imageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_flat, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        imageView = (SubsamplingScaleImageView) view.findViewById(R.id.image_view);
    }
}
