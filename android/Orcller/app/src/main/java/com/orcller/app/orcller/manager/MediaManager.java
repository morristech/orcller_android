package com.orcller.app.orcller.manager;

import com.orcller.app.orcller.model.album.Media;

/**
 * Created by pisces on 11/16/15.
 */
public class MediaManager {

    public class DidChangeImages {
        private Media media;

        public DidChangeImages(Media media) {
            this.media = media;
        }

        public Media getMedia() {
            return media;
        }
    }
}
