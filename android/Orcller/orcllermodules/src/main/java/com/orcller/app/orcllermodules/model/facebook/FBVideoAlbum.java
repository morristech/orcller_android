package com.orcller.app.orcllermodules.model.facebook;

import android.os.Bundle;

/**
 * Created by pisces on 11/26/15.
 */
public class FBVideoAlbum extends FBAlbum {

    @Override
    public String getGraphPath() {
        return "me/videos/uploaded";
    }

    @Override
    public Bundle getParameters() {
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,description,picture,source,thumbnails{height,width,uri}");
        parameters.putInt("limit", ALBUM_CONTENT_LIMIT_COUNT);
        return parameters;
    }
}
