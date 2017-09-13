package de.trac.spherical;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import de.trac.spherical.parser.PhotoSphereMetadata;
import de.trac.spherical.parser.SphereParser;

import de.trac.spherical.rendering.Renderer;
import de.trac.spherical.rendering.SphereSurfaceView;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "Spherical";

    public static final String MIME_PHOTO_SPHERE = "application/vnd.google.panorama360+jpg";
    public static final String MIME_IMAGE = "image/*";

    private SphereSurfaceView surfaceView;
    private Renderer renderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize renderer and setup surface view.
        surfaceView = new SphereSurfaceView(this);
        renderer = new Renderer(surfaceView);
        setContentView(surfaceView);

        Intent intent = getIntent();
        switch (intent.getAction()) {
            //Image was sent into the app
            case Intent.ACTION_SEND:
                handleSentImageIntent(intent);
                break;

            //App was launched via launcher icon
            //TODO: Remove later together with launcher intent filter
            default:
                Toast.makeText(this, R.string.prompt_share_image, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Distinguish type of sent image. Images with the MIME type of a photosphere will be directly
     * displayed, while images with MIME type image/* are being manually tested using {@link SphereParser}.
     * @param intent incoming intent.
     */
    private void handleSentImageIntent(Intent intent) {
        String type = intent.getType();
        if (type != null) {

            Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (imageUri == null) {
                Toast.makeText(this, R.string.file_not_found, Toast.LENGTH_SHORT).show();
                return;
            }

            switch (type) {
                case MIME_PHOTO_SPHERE:
                    displayPhotoSphere(imageUri);
                    break;

                default:
                    displayMaybePhotoSphere(imageUri);
                    break;
            }

        } else {
            Toast.makeText(this, "TODO: Figure out what to do :D", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Check, whether the sent photo is a photo sphere and display either a sphere, or a plain image.
     * @param uri
     */
    private void displayMaybePhotoSphere(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            String xml = SphereParser.getXMLContent(inputStream);
            PhotoSphereMetadata metadata = SphereParser.parse(xml);

            if (metadata == null || !metadata.isUsePanoramaViewer()) {
                displayFlatImage(getContentResolver().openInputStream(uri));
            } else {
                displayPhotoSphere(getContentResolver().openInputStream(uri), metadata);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Display a photo sphere.
     * @param uri
     */
    private void displayPhotoSphere(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            String xml = SphereParser.getXMLContent(inputStream);
            PhotoSphereMetadata metadata = SphereParser.parse(xml);

            if (metadata == null) {
                Log.e(TAG, "Metadata is null. Fall back to flat image.");
                displayFlatImage(getContentResolver().openInputStream(uri));
            }

            displayPhotoSphere(getContentResolver().openInputStream(uri), metadata);

        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found.", e);
            Toast.makeText(this, R.string.file_not_found, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "IOException: ", e);
            Toast.makeText(this, R.string.ioerror, Toast.LENGTH_SHORT).show();
        }
    }

    private void displayPhotoSphere(InputStream inputStream, PhotoSphereMetadata metadata) {
        renderer.setBitmap(BitmapFactory.decodeStream(inputStream));
        Log.d(TAG, "Display Photo Sphere!");
    }

    /**
     * Display a flat image.
     * @param inputStream
     */
    private void displayFlatImage(InputStream inputStream) {
        Log.d(TAG, "Display Flat Image!");
    }
}
