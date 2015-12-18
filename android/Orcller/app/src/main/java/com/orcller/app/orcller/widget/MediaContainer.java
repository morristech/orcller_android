package com.orcller.app.orcller.widget;

import com.orcller.app.orcller.model.Media;

/**
 * Created by pisces on 11/24/15.
 */
public interface MediaContainer {
    Media getModel();
    MediaView getMediaView();
    VideoMediaView getVideoMediaView();
}
