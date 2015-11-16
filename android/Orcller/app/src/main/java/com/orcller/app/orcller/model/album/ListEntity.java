package com.orcller.app.orcller.model.album;

import java.util.Date;

import pisces.psfoundation.model.Model;

/**
 * Created by pisces on 11/16/15.
 */
public class ListEntity extends Model {
    public int count;
    public int total_count;
    public String after;
    public String prev;
    public Date time;
}
