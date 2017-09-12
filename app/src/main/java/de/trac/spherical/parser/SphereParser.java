package de.trac.spherical.parser;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * ParserUtil that does stuff.
 */
public class SphereParser {

    public static final String TAG = "SphereParser";

    public static final String USE_PANORAMA_VIEWER = "GPano:UsePanoramaViewer=\"True\"";

    /*
     * 0    FF
     * 1    D8
     * 2    FF
     * 3    E1
     * 4    Length EXIF (n)
     * 5    Length EXIF (n)
     * 6    <exif>
     * n+4  </exif>
     * n+5  FF
     * n+6  E1
     * n+7  Length XML (m)
     * n+7  Length XML (m)
     * n+8  <xml>
     * n+8+m</xml>
     * n+5  <xml>
     * ?    </xml>
     */

    public static final byte[] FFE1 = new byte[] {
            (byte) 0xFF, (byte) 0xE1
    };

    public static final byte[] FFD8FFE1 = new byte[] {
            (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE1
    };



    public static String getXMLContent(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("File MUST NOT be null!");
        }

        //HEADER
        byte[] r = new byte[FFD8FFE1.length];
        int i = inputStream.read(r);
        if (i != FFD8FFE1.length || !Arrays.equals(FFD8FFE1, r)) {
            System.out.println("Unexpected Image header: " + hex(r) + " (" + hex(FFD8FFE1) + " expected)");
            return null;
        }

        //EXIF Length
        r = new byte[2];
        i = inputStream.read(r);
        if (i != 2) {
            throw new EOFException("Unexpected EOF!");
        }
        int exifLen = integer(r);

        //Skip EXIF header
        r = new byte[exifLen - 2];
        i = inputStream.read(r);

        //XML Header
        r = new byte[2];
        i = inputStream.read(r);
        if (!Arrays.equals(FFE1, r)) {
            System.out.println("Image does not contain XML data.");
            return null;
        }

        r = new byte[2];
        i = inputStream.read(r);
        if (i != 2) {
            throw new EOFException("Unexpected EOF!");
        }
        int xmlLen = integer(r);

        byte[] xml = new byte[xmlLen - 2];
        i = inputStream.read(xml);
        if (i != xml.length) {
            throw new EOFException("Unexpected EOF!");
        }

        return new String(xml);
    }

    public static void main(String[] args) throws IOException {
        File file = new File(args[0]);
        System.out.println(getXMLContent(new FileInputStream(file)));
    }

    private static void unread(ArrayList<Byte> list, PushbackInputStream pb) throws IOException {
        for (int i = list.size() - 1; i >= 0; i--) {
            pb.unread(list.get(i));
        }
    }

    public static void append(ArrayList<Byte> list, byte[] array, int r) {
        for (int i = 0; i < r; i++) {
            list.add(array[i]);
        }
    }

    private static String hex(byte[] b) {
        return new BigInteger(b).toString(16);
    }

    private static int integer(byte[] b) {
        return new BigInteger(b).intValue();
    }
}
