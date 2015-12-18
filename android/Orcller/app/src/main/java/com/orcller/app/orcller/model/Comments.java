package com.orcller.app.orcller.model;

import java.util.Collections;
import java.util.Comparator;

/**
 * Created by pisces on 11/16/15.
 */
public class Comments extends AlbumAdditionalListEntity<Comment> {
    public void sort(boolean ascending) {
        if (data == null)
            return;

        final int left = ascending ? 1 : -1;
        final int right = ascending ? -1 : 1;

        Collections.sort(data, new Comparator<Comment>() {
            @Override
            public int compare(Comment lhs, Comment rhs) {
                if (lhs.id > rhs.id)
                    return left;
                if (lhs.id < rhs.id)
                    return right;
                return 0;
            }
        });
    }
}