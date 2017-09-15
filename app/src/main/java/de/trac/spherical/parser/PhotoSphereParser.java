package de.trac.spherical.parser;

import android.util.Log;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import static de.trac.spherical.parser.PhotoSphereMetadata.CAPTURE_SOFTWARE;
import static de.trac.spherical.parser.PhotoSphereMetadata.CROPPED_AREA_IMAGE_HEIGHT_PIXELS;
import static de.trac.spherical.parser.PhotoSphereMetadata.CROPPED_AREA_IMAGE_WIDTH_PIXELS;
import static de.trac.spherical.parser.PhotoSphereMetadata.CROPPED_AREA_LEFT_PIXELS;
import static de.trac.spherical.parser.PhotoSphereMetadata.CROPPED_AREA_TOP_PIXELS;
import static de.trac.spherical.parser.PhotoSphereMetadata.EXPOSURE_LOCK_USED;
import static de.trac.spherical.parser.PhotoSphereMetadata.FIRST_PHOTO_DATE;
import static de.trac.spherical.parser.PhotoSphereMetadata.FULL_PANO_HEIGHT_PIXELS;
import static de.trac.spherical.parser.PhotoSphereMetadata.FULL_PANO_WIDTH_PIXELS;
import static de.trac.spherical.parser.PhotoSphereMetadata.INITIAL_CAMERA_DOLLY;
import static de.trac.spherical.parser.PhotoSphereMetadata.INITIAL_HORIZONTAL_POV_DEGREES;
import static de.trac.spherical.parser.PhotoSphereMetadata.INITIAL_VIEW_HEADING_DEGREES;
import static de.trac.spherical.parser.PhotoSphereMetadata.INITIAL_VIEW_PITCH_DEGREES;
import static de.trac.spherical.parser.PhotoSphereMetadata.INITIAL_VIEW_ROLL_DEGREES;
import static de.trac.spherical.parser.PhotoSphereMetadata.LAST_PHOTO_DATE;
import static de.trac.spherical.parser.PhotoSphereMetadata.POSE_HEADING_DEGREES;
import static de.trac.spherical.parser.PhotoSphereMetadata.POSE_PITCH_DEGREES;
import static de.trac.spherical.parser.PhotoSphereMetadata.POSE_ROLL_DEGREES;
import static de.trac.spherical.parser.PhotoSphereMetadata.PROJECTION_TYPE;
import static de.trac.spherical.parser.PhotoSphereMetadata.SOURCE_PHOTOS_COUNT;
import static de.trac.spherical.parser.PhotoSphereMetadata.STITCHING_SOFTWARE;
import static de.trac.spherical.parser.PhotoSphereMetadata.ProjectionType;
import static de.trac.spherical.parser.PhotoSphereMetadata.USE_PANORAMA_VIEWER;

/**
 * ParserUtil that does stuff.
 */
public class PhotoSphereParser {

    private static final String TAG = "PhoSphePars";

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'", Locale.US);

    /*
     * 0    FF
     * 1    D8
     * 2    FF
     * 3    E1
     * 4    Length EXIF (n)
     * 5    Length EXIF (n)
     * 6    <exif> (length n-2)
     * n+4  </exif>
     * n+5  FF
     * n+6  E1
     * n+7  Length XML (m)
     * n+7  Length XML (m)
     * n+8  <xml> (length m-2)
     * n+8+m</xml>
     * n+5  <xml>
     * ?    </xml>
     */

    private static final byte[] FFE1 = new byte[] {
            (byte) 0xFF, (byte) 0xE1
    };

    private static final byte[] FFD8FFE1 = new byte[] {
            (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE1
    };



    public static String getXMLContent(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("File MUST NOT be null!");
        }

        //HEADER
        byte[] r = new byte[FFD8FFE1.length];
        int i = inputStream.read(r);
        throwIfUnexpectedEOF(i, r.length);
        if (!Arrays.equals(FFD8FFE1, r)) {
            Log.d(TAG, "Unexpected Image header: " + hex(r) + " (" + hex(FFD8FFE1) + " expected)");
            return null;
        }

        //EXIF Length
        r = new byte[2];
        i = inputStream.read(r);
        throwIfUnexpectedEOF(i, r.length);
        int exifLen = integer(r);

        //Skip EXIF header
        r = new byte[exifLen - 2];
        i = inputStream.read(r);
        throwIfUnexpectedEOF(i, r.length);

        //XML Header
        r = new byte[2];
        i = inputStream.read(r);
        throwIfUnexpectedEOF(i, r.length);
        if (!Arrays.equals(FFE1, r)) {
            Log.d(TAG, "Image does not contain XML data.");
            return null;
        }

        r = new byte[2];
        i = inputStream.read(r);
        throwIfUnexpectedEOF(i, r.length);
        int xmlLen = integer(r);

        byte[] xml = new byte[xmlLen - 2];
        i = inputStream.read(xml);
        throwIfUnexpectedEOF(i, xml.length);

        return new String(xml);
    }

    public static PhotoSphereMetadata parse(String xmp) {
        if (xmp == null) {
            return null;
        }
        PhotoSphereMetadata meta = new PhotoSphereMetadata();
        meta.setUsePanoramaViewer(parseBoolean(USE_PANORAMA_VIEWER, xmp, true));
        meta.setCaptureSoftware(parseString(CAPTURE_SOFTWARE, xmp));
        meta.setStitchingSoftware(parseString(STITCHING_SOFTWARE, xmp));
        meta.setProjectionType(parseType(PROJECTION_TYPE, xmp, ProjectionType.equirectangular));
        meta.setPoseHeadingDegrees(parseFloat(POSE_HEADING_DEGREES, xmp, null));
        meta.setPosePitchDegrees(parseFloat(POSE_PITCH_DEGREES, xmp, 0f));
        meta.setPoseRollDegrees(parseFloat(POSE_ROLL_DEGREES, xmp, 0f));
        meta.setInitialViewHeadingDegrees(parseInteger(INITIAL_VIEW_HEADING_DEGREES, xmp, 0));
        meta.setInitialViewPitchDegrees(parseInteger(INITIAL_VIEW_PITCH_DEGREES, xmp, 0));
        meta.setInitialViewRollDegrees(parseInteger(INITIAL_VIEW_ROLL_DEGREES, xmp, 0));
        meta.setInitialHorizontalFOVDegrees(parseFloat(INITIAL_HORIZONTAL_POV_DEGREES, xmp, null));
        meta.setFirstPhotoDate(parseDate(FIRST_PHOTO_DATE, xmp, null));
        meta.setLastPhotoDate(parseDate(LAST_PHOTO_DATE, xmp, null));
        meta.setSourcePhotosCount(parseInteger(SOURCE_PHOTOS_COUNT, xmp, null));
        meta.setExposureLockUsed(parseBoolean(EXPOSURE_LOCK_USED, xmp, false));
        meta.setCroppedAreaImageWidthPixels(parseInteger(CROPPED_AREA_IMAGE_WIDTH_PIXELS, xmp, null));
        meta.setCroppedAreaImageHeightPixels(parseInteger(CROPPED_AREA_IMAGE_HEIGHT_PIXELS, xmp, null));
        meta.setFullPanoWidthPixels(parseInteger(FULL_PANO_WIDTH_PIXELS, xmp, null));
        meta.setFullPanoHeightPixels(parseInteger(FULL_PANO_HEIGHT_PIXELS, xmp, null));
        meta.setCroppedAreaLeftPixels(parseInteger(CROPPED_AREA_LEFT_PIXELS, xmp, null));
        meta.setCroppedAreaTopPixels(parseInteger(CROPPED_AREA_TOP_PIXELS, xmp, null));
        meta.setInitialCameraDolly(parseFloat(INITIAL_CAMERA_DOLLY, xmp, 0f));
        return meta;
    }

    private static void throwIfUnexpectedEOF(int actual, int expected) throws EOFException {
        if (actual != expected) {
            throw new EOFException("Unexpected EOF!");
        }
    }

    private static String parseString(String key, String xmp) {
        if (!xmp.contains(key)) {
            return null;
        }

        String query = key + "=\"";
        String value = xmp.substring(xmp.indexOf(query) + query.length());
        value = value.substring(0, value.indexOf("\""));
        return value;
    }

    private static Integer parseInteger(String key, String xmp, Integer defaultValue) {
        String value = parseString(key, xmp);
        if (value == null) {
            return defaultValue;
        }
        return Integer.parseInt(value);

    }

    private static Float parseFloat(String key, String xmp, Float defaultValue) {
        String value = parseString(key, xmp);
        if (value == null) {
            return defaultValue;
        }
        return Float.parseFloat(value);

    }

    private static Boolean parseBoolean(String key, String xmp, Boolean defaultValue) {
        String value = parseString(key, xmp);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    private static ProjectionType parseType(String key, String xmp, ProjectionType defaultValue) {
        String value = parseString(key, xmp);
        if (value == null) {
            return defaultValue;
        }
        return ProjectionType.equirectangular;
    }

    private static Date parseDate(String key, String xmp, Date defaultValue) {
        String value = parseString(key, xmp);
        try {
            if (value == null) {
                return defaultValue;
            }
            return dateFormat.parse(value);
        } catch (ParseException e) {
            return defaultValue;
        }
    }

    private static String hex(byte[] b) {
        return new BigInteger(b).toString(16);
    }

    private static int integer(byte[] b) {
        return new BigInteger(b).intValue();
    }
}
