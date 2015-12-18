package com.orcller.app.orcller.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.common.SharedObject;
import com.orcller.app.orcller.model.Album;
import com.orcller.app.orcller.model.Image;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.BitmapUtils;
import pisces.psfoundation.utils.GraphicUtils;
import pisces.psfoundation.utils.Log;

/**
 * Created by pisces on 12/8/15.
 */
public class ImageGenerator {
    public static final int HORIZONTAL_GAP = GraphicUtils.convertDpToPixel(1);
    public static final int VERTICAL_GAP = GraphicUtils.convertDpToPixel(1);
    public static final int IMAGE_WIDTH = 1080;
    public static final int IMAGE_HEIGHT = 569;
    public static final int MAX_COLUMN_COUNT = 4;
    public static final int MAX_IMAGE_COUNT = 10;

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static void generateShareImage(final Album album, final CompleteHandler completeHandler) {
        Application.runOnBackgroundThread(new Runnable() {
            @Override
            public void run() {
                final Point processPoint = new Point(0, Math.min(album.pages.data.size(), MAX_IMAGE_COUNT));
                final Rows rows = createShareImageRows(album);
                final Bitmap canvasBitmap = Bitmap.createBitmap(IMAGE_WIDTH, rows.height, Bitmap.Config.ARGB_8888);
                final Canvas canvas = new Canvas(canvasBitmap);
                final Runnable check = new Runnable() {
                    @Override
                    public void run() {
                        if (++processPoint.x >= processPoint.y) {
                            final Bitmap bitmap = Bitmap.createBitmap(canvasBitmap, 0, Math.max(0, rows.height - IMAGE_HEIGHT), IMAGE_WIDTH, IMAGE_HEIGHT);
                            canvasBitmap.recycle();

                            Application.runOnMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    completeHandler.onComplete(bitmap);
                                }
                            });
                        }
                    }
                };
                int y = 0;
                int index = 0;

                canvas.drawColor(Color.WHITE);

                for (final Row row : rows.data) {
                    for (int j = 0; j < row.columnCount; j++) {
                        int hap = HORIZONTAL_GAP * j;
                        final Rect rect = new Rect(row.columnWidth * j + hap, y, row.columnWidth, row.columnWidth);
                        Image image = album.pages.getPageAtIndex(index++).media.images.standard_resolution;

                        Glide.with(Application.applicationContext())
                                .load(SharedObject.toFullMediaUrl(image.url))
                                .listener(new RequestListener<Object, GlideDrawable>() {
                                    @Override
                                    public boolean onException(Exception e, Object model, Target<GlideDrawable> target, boolean isFirstResource) {
                                        if (BuildConfig.DEBUG)
                                            Log.e(e.getMessage(), e);

                                        check.run();
                                        return true;
                                    }

                                    @Override
                                    public boolean onResourceReady(GlideDrawable resource, Object model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                        Bitmap bitmap = ((GlideBitmapDrawable) resource).getBitmap();
                                        bitmap = BitmapUtils.createRectBitmap(
                                                new Point(rect.right, rect.bottom), bitmap);
                                        canvas.drawBitmap(bitmap, rect.left, rect.top, null);
                                        bitmap.recycle();

                                        if (processPoint.x+1 >= processPoint.y && processPoint.y < album.pages.total_count) {
                                            String text = "+" + String.valueOf(album.pages.total_count - processPoint.y);
                                            drawTextView(canvas, text, row.columnWidth, row.columnWidth);
                                        }

                                        check.run();
                                        return true;
                                    }
                                })
                                .into(image.width, image.height);
                    }

                    y += (row.columnWidth + VERTICAL_GAP);
                }
            }
        });
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private static Rows createShareImageRows(Album model) {
        int i = Math.min(model.pages.data.size(), MAX_IMAGE_COUNT);
        int height = 0;
        double column = Math.min(MAX_COLUMN_COUNT, Math.ceil(Math.sqrt(i)));
        List<Row> data = new ArrayList<>();

        while (i > 0) {
            int columnCount = i >= column ? (int) column : i;
            int addend = HORIZONTAL_GAP * (columnCount - 1);
            int columnWidth = Math.round((float) (IMAGE_WIDTH - addend) / columnCount);
            i -= column;
            column = i > column ? Math.min(MAX_COLUMN_COUNT, Math.ceil(Math.sqrt(i))) : column;
            height += columnWidth;

            data.add(new Row(data.size(), columnCount, columnWidth));
        }

        height = height + (VERTICAL_GAP * (data.size() - 1));

        Collections.reverse(data);

        return new Rows(data, height);
    }

    private static void drawTextView(Canvas canvas, String text, int width, int height) {
        Context context = Application.applicationContext();
        TextView textView = new TextView(context);
        textView.setDrawingCacheEnabled(true);
        textView.setText(text);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(25);
        textView.setShadowLayer(15, 3, 3, Color.argb(55, 0, 0, 0));
        textView.measure(0, 0);
        textView.layout(0, 0, textView.getMeasuredWidth(), textView.getMeasuredHeight());

        int left = canvas.getWidth() - width + (width - textView.getWidth())/2;
        int top = canvas.getHeight() - height + (height - textView.getHeight())/2;

        canvas.drawBitmap(textView.getDrawingCache(), left, top, null);
    }

    private static class Row {
        public int index;
        public int columnCount;
        public int columnWidth;

        public Row(int index, int columnCount, int columnWidth) {
            this.columnCount = columnCount;
            this.columnWidth = columnWidth;
        }
    }

    private static class Rows {
        List<Row> data;
        public int height;

        public Rows(List<Row> data, int height) {
            this.data = data;
            this.height = height;
        }
    }

    public static interface CompleteHandler {
        void onComplete(Bitmap bitmap);
    }
}
