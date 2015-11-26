package com.orcller.app.orcller.model.album;

import java.io.Serializable;

/**
 * Created by pisces on 11/16/15.
 */
public class ImageMedia extends Media {
    public ImageMedia() {
        type = Type.Image.getValue();
    }
}
