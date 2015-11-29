package com.orcller.app.orcller.common;

import android.text.TextUtils;

import com.orcller.app.orcller.R;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.URLUtils;

/**
 * Created by pisces on 11/4/15.
 */
public class SharedObject {
    public enum SizeType {
        Small,
        Medium,
        Large
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static int convertPositionToPageIndex(int position) {
        return Math.round((float) position/2);
    }

    public static int convertPageIndexToPosition(int pageIndex) {
        return Math.max(0, (pageIndex * 2) - 1);
    }

    public static List<Integer> getListPositions(int position, int total) {
        List<Integer> positions = new ArrayList<>();
        int index = getListPosition(position + (position % 2 == 1 ? 1 : -1), total);

        if (index > -1)
            positions.add(index);

        index = getListPosition(position, total);

        if (index > -1)
            positions.add(index);

        return positions;
    }

    public static int getListPosition(int position, int total) {
        if (position > -1 && position < total)
            return position;
        return -1;
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    public static String toFullMediaUrl(String url) {
        if (URLUtils.isWebURL(url))
            return url;
        return Application.applicationContext().getString(R.string.s3_domain) + "/" + url;
    }

    public static String toUserPictureUrl(String filename, SizeType sizeType) {
        if (TextUtils.isEmpty(filename))
            return null;

        if (URLUtils.isWebURL(filename))
            return filename;

        Pattern pattern = Pattern.compile("/(.*).(jpg|png)$");
        Matcher matcher = pattern.matcher(filename);

        while (matcher.find()) {
            String replaced = matcher.replaceAll(getUserPicturePrefix(sizeType) + matcher.group(0));
            return Application.applicationContext().getString(R.string.s3_domain) + "/" + replaced;
        }

        return null;
    }

    private static String getUserPicturePrefix(SharedObject.SizeType sizeType) {
        if (SharedObject.SizeType.Small.equals(sizeType))
            return "/p88x88";
        if (SharedObject.SizeType.Medium.equals(sizeType))
            return "/p108x108";
        if (SharedObject.SizeType.Large.equals(sizeType))
            return "/p640x640";
        return "";
    }
}
