package de.trac.spherical;

import android.content.Intent;
import android.os.AsyncTask;

/**
 * Created by vanitas on 15.09.17.
 */

public class HandleImageTask extends AsyncTask<Intent, Void, Void> {

    private MainActivity mainActivity;

    public HandleImageTask(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    protected Void doInBackground(Intent... params) {
        mainActivity.handleSentImageIntent(params[0]);
        return null;
    }
}
