package com.orcller.app.orcller.common;

import com.orcller.app.orcller.R;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.URLUtils;

/**
 * Created by pisces on 11/4/15.
 */
public class SharedObject {
    public static String toFullMediaUrl(String url) {
        if (URLUtils.checkURL(url))
            return url;
        return Application.applicationContext().getString(R.string.s3_domain) + "/" + url;
    }
}
