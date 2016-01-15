package com.orcller.app.orcller.model;

/**
 * Created by pisces on 11/16/15.
 */
public class VideoMedia extends Media {
    public Videos videos;

    public VideoMedia() {
        super();

        type = Type.Video.value();
    }
}
