package com.orcller.app.orcller.model.album;

import android.text.TextUtils;

import pisces.psfoundation.model.Model;

/**
 * Created by pisces on 11/16/15.
 */
public class Image extends Model {
    public int width;
    public int height;
    public String url;

    public Image() {
    }

    public Image(int width, int height, String url) {
        this.width = width;
        this.height = height;
        this.url = url;
    }

    public boolean isEmpty() {
        return TextUtils.isEmpty(url);
    }
}
