package com.orcller.app.orcllermodules.model.facebook;

import java.util.List;

import pisces.psfoundation.model.Model;

/**
 * Created by pisces on 11/26/15.
 */
public class FBPhotos extends FBMediaList {
    public List<FBPhoto> data;

    @Override
    public List<FBPhoto> getData() {
        return data;
    }
}
