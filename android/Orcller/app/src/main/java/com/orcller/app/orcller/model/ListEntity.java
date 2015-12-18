package com.orcller.app.orcller.model;

import java.util.ArrayList;
import java.util.List;

import pisces.psfoundation.model.Model;

/**
 * Created by pisces on 11/16/15.
 */
public class ListEntity<T> extends Model {
    public int count;
    public int total_count;
    public long time;
    public String after;
    public String prev;
    public List<T> data = new ArrayList<>();
}
