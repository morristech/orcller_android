package com.orcller.app.orcllermodules.model.facebook;

import java.util.ArrayList;
import java.util.List;

import pisces.psfoundation.model.Model;

/**
 * Created by pisces on 11/26/15.
 */
public class FBPhoto extends Model {
    public String id;
    public String created_time;
    public String updated_time;
    public ArrayList<FBPhotoImage> images;
}