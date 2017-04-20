package edu.stanford.me202.lw_me202;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.squareup.picasso.Transformation;

/**
 * Created by Luke on 4/18/2017.
 */

 //Custom Picasso transform for a centered circle crop
public class CircleConvert implements Transformation {
    @Override
    public Bitmap transform(Bitmap source) {
         //get image size
        int size = Math.min(source.getWidth(), source.getHeight());

         //get location of origin
        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

         //load bitmap
        Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
         //throw away original source if it was modified
        if (squaredBitmap != source) {
            source.recycle();
        }

         //initialize blank bitmap to take new image
        Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

         //initialize paint, link shader to source & set configurations
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
            //don't tile the bitmap image
        BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);

         //draw the circle from the source bitmap
        float r = size / 2f;
        canvas.drawCircle(r, r, r, paint);

         //throw away the square bitmap
        squaredBitmap.recycle();
        return bitmap;
    }

    @Override
    public String key() {
        return "circle()";
    }
}
