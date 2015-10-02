package com.sbd.downloadmanagerfordropbox.helper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by StangaBogdan on 8/28/2015.
 */
public class MediaHelper {

    public static Bitmap decodeThumbnailFromFile(File f) {
        int size = 60;
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            FileInputStream fileInputStream = new FileInputStream(f);
            BitmapFactory.decodeStream(fileInputStream, null, o);

            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < size && height_tmp / 2 < size)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }
            boolean supported = fileInputStream.markSupported();
            if (supported) {
                fileInputStream.reset();
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            Bitmap bmp = BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
            if (bmp.getWidth() >= bmp.getHeight()) {
                bmp = Bitmap.createBitmap(
                        bmp,
                        bmp.getWidth() / 2 - bmp.getHeight() / 2,
                        0,
                        bmp.getHeight(),
                        bmp.getHeight()
                );
            } else {
                bmp = Bitmap.createBitmap(
                        bmp,
                        0,
                        bmp.getHeight() / 2 - bmp.getWidth() / 2,
                        bmp.getWidth(),
                        bmp.getWidth()
                );
            }
            return bmp;
        } catch (Exception e) {
        }
        return null;
    }

}
