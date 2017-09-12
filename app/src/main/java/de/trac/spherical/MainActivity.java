package de.trac.spherical;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import de.trac.spherical.parser.SphereParser;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "Spherical";
    public static final String INTENT_SPHERE = "application/vnd.google.panorama360+jpg";

    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text = (TextView) findViewById(R.id.hello_world);

        Log.d(TAG, "STARTING");
        Intent intent = getIntent();
        Log.d(TAG, "Intent: " + intent.getAction() + " " + intent.getType());

        switch (intent.getAction()) {
            case Intent.ACTION_SEND:
                handleSentImage(intent);
            break;

            default:
                Toast.makeText(this, R.string.prompt_share_image, Toast.LENGTH_LONG).show();
        }
    }

    private void handleSentImage(Intent intent) {
        String type = intent.getType();
        if (type != null) {
            switch (type) {
                case INTENT_SPHERE:
                    Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                    if (imageUri != null) {
                        showImage(imageUri);
                    }

                Toast.makeText(this, R.string.wow, Toast.LENGTH_LONG).show();
                    break;
            }
        } else {
            Toast.makeText(this, "LOL", Toast.LENGTH_SHORT).show();
        }
    }

    private void showImage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            String xml = SphereParser.getXMLContent(inputStream);

            if (xml != null) {
                text.setText(xml);
            } else {
                text.setText("null");
            }

        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found.", e);
            Toast.makeText(this, R.string.file_not_found, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "IOException: ", e);
            Toast.makeText(this, R.string.ioerror, Toast.LENGTH_SHORT).show();
        }
    }
}
