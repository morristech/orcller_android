package pisces.psfoundation.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * Created by pisces on 11/17/15.
 */
public class BitmapUtils {
    public static Bitmap createScaledBitmap(Point size, Bitmap source) {
        float scale = (float) Math.min(size.x, size.y) / Math.min(source.getWidth(), source.getHeight());
        int w = Math.round(source.getWidth() * scale);
        int h = Math.round(source.getHeight() * scale);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(source, w, h, true);
        Bitmap bitmap = Bitmap.createBitmap(scaledBitmap, (w - size.x)/2, (h - size.y)/2, size.x, size.y);
        return bitmap;
    }

    public static Bitmap createSquareBitmap(Point size, Bitmap source) {
        int limitValue = Math.min(size.x, size.y);
        int w = Math.min(limitValue, size.x);
        int h = Math.min(limitValue, size.y);
        int x = Math.max(0, (size.x - w) / 2);
        int y = Math.max(0, (size.y - h)/2);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(source, size.x, size.y, true);
        Bitmap bitmap = Bitmap.createBitmap(scaledBitmap, x, y, w, h);
        return bitmap;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
