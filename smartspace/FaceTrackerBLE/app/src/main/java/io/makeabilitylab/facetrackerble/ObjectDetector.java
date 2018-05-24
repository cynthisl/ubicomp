package io.makeabilitylab.facetrackerble;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;

import java.io.ByteArrayOutputStream;

public class ObjectDetector extends Detector {
    @Override
    public SparseArray detect(Frame frame) {

        int width = frame.getMetadata().getWidth();
        int height = frame.getMetadata().getHeight();

        // get bitmap from frame; getBitmap doesn't always work
        // https://stackoverflow.com/a/46891894
        YuvImage yuvImage = new YuvImage(frame.getGrayscaleImageData().array(), ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, os);
        byte[] jpegArray = os.toByteArray();
        Bitmap bm = BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.length);



        return null;
    }
}
