package de.trac.spherical.util;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.IOException;

import de.trac.spherical.parser.PhotoSphereMetadata;
import de.trac.spherical.parser.PhotoSphereParser;

/**
 * Dedicated async tasks to load an image.
 * Takes a progress reporter and informs it about the changes.
 */
public class LoadImageTask extends AsyncTask<Void, Void, LoadImageTask.Result> {

    private ContentResolver contentResolver;
    private Uri uri;

    private Bitmap bitmap;
    private PhotoSphereMetadata metadata;
    private final String type;
    private final FinishedCallback callback;

    public LoadImageTask(ContentResolver contentResolver, Uri uri, String type, FinishedCallback callback) {
        this.callback = callback;
        this.contentResolver = contentResolver;
        this.uri = uri;
        this.type = type;
    }

    @Override
    protected Result doInBackground(Void... params) {

        try {
            bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri));
            metadata = PhotoSphereParser.parse(contentResolver.openInputStream(uri));
            return new Result(bitmap, metadata);
        } catch (IOException | OutOfMemoryError e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Result result) {
        callback.onImageLoadingFinished(result);
    }

    public interface FinishedCallback {
        void onImageLoadingFinished(Result result);
    }

    public static class Result {

        private Bitmap bitmap;
        private PhotoSphereMetadata metadata;

        public Result(Bitmap bitmap, PhotoSphereMetadata metadata) {
            this.bitmap = bitmap;
            this.metadata = metadata;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public PhotoSphereMetadata getMetadata() {
            return metadata;
        }
    }
}
