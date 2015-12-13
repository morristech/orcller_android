package com.orcller.app.orcller.common;

import android.graphics.Point;
import android.text.TextUtils;

import com.orcller.app.orcller.R;
import com.orcller.app.orcller.manager.MediaManager;
import com.orcller.app.orcller.model.album.AlbumAdditionalListEntity;
import com.orcller.app.orcller.model.album.Comments;
import com.orcller.app.orcller.model.album.Favorites;
import com.orcller.app.orcller.model.album.Likes;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.URLUtils;

/**
 * Created by pisces on 11/4/15.
 */
public class SharedObject {
    public static final File CACHE_DIR = Application.applicationContext().getCacheDir();
    public static final File FILES_DIR = Application.applicationContext().getFilesDir();
    public static final File DATA_DIR = new File(CACHE_DIR.getAbsolutePath() + File.separator + "data");
    public static final File TEMP_IMAGE_DIR = new File(CACHE_DIR.getAbsolutePath() + File.separator + "images");

    public enum SizeType {
        Small,
        Medium,
        Large
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static int convertPositionToPageIndex(int position) {
        return Math.round((float) position / 2);
    }

    public static int convertPageIndexToPosition(int pageIndex) {
        return Math.max(0, (pageIndex * 2) - 1);
    }

    public static int convertSizeTypeToPixel(SizeType sizeType) {
        if (SizeType.Large.equals(sizeType))
            return MediaManager.WIDTH_IMAGE_STANDARD_RESOLUTION;
        if (SizeType.Medium.equals(sizeType))
            return MediaManager.WIDTH_IMAGE_LOW_RESOLUTION;
        if (SizeType.Small.equals(sizeType))
            return MediaManager.WIDTH_IMAGE_THUMBNAIL;
        return 0;
    }

    public static String extractFilename(String filename, SizeType sizeType) {
        if (TextUtils.isEmpty(filename))
            return null;

        Pattern namePattern = Pattern.compile("[^/]+[0-9$]");
        Pattern exPattern = Pattern.compile("\\.[a-z]+");
        Matcher nameMatcher = namePattern.matcher(filename);
        Matcher exMatcher = exPattern.matcher(filename);

        nameMatcher.find();
        exMatcher.find();

        return nameMatcher.group(0) + "_" + getSizeTypeString(sizeType) + exMatcher.group(0);
    }

    public static String getAlbumInfoText(AlbumAdditionalListEntity entity) {
        if (entity instanceof Likes)
            return getAlbumInfoText(entity, (entity.total_count > 1 ? R.string.w_hearts : R.string.w_heart));
        if (entity instanceof Comments)
            return getAlbumInfoText(entity, (entity.total_count > 1 ? R.string.w_comments : R.string.w_comment));
        if (entity instanceof Favorites)
            return getAlbumInfoText(entity, (entity.total_count > 1 ? R.string.w_stars : R.string.w_star));
        return null;
    }

    public static String getImageUploadPath(String filename, Point size) {
        return "images/p" + String.valueOf(size.x) + "x" + String.valueOf(size.y) + "/" + filename;
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

    public static String getShareContentUrl(String encryptedAlbumId) {
        try {
            return Application.applicationContext().getString(R.string.domain) +
                    "/site/album/view?id=" + URLEncoder.encode(encryptedAlbumId, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static String toFullMediaUrl(String url) {
        if (URLUtils.isWebURL(url) || URLUtils.isLocal(url))
            return url;
        return Application.applicationContext().getString(R.string.s3_domain) + "/" + url;
    }

    public static String toUserPictureUrl(String filename, SizeType sizeType) {
        return toUserPictureUrl(filename, sizeType, false);
    }

    public static String toUserPictureUrl(String filename, SizeType sizeType, boolean excludeDomain) {
        if (TextUtils.isEmpty(filename))
            return null;

        if (URLUtils.isWebURL(filename))
            return filename;

        Pattern pattern = Pattern.compile("/(.*).(jpg|png)$");
        Matcher matcher = pattern.matcher(filename);

        while (matcher.find()) {
            String replaced = matcher.replaceAll(getUserPicturePrefix(sizeType) + matcher.group(0));
            return excludeDomain ? replaced : Application.applicationContext().getString(R.string.s3_domain) + "/" + replaced;
        }

        return null;
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private static String getAlbumInfoText(AlbumAdditionalListEntity entity, int stringId) {
        return Application.applicationContext().getString(stringId) + " " +
                (entity.total_count > 0 ? String.valueOf(entity.total_count) : "");
    }

    private static String getSizeTypeString(SizeType sizeType) {
        if (SizeType.Small.equals(sizeType))
            return "s";
        if (SizeType.Medium.equals(sizeType))
            return "l";
        if (SizeType.Large.equals(sizeType))
            return "s";
        return "";
    }

    private static String getUserPicturePrefix(SizeType sizeType) {
        if (SizeType.Small.equals(sizeType))
            return "/p88x88";
        if (SizeType.Medium.equals(sizeType))
            return "/p108x108";
        if (SizeType.Large.equals(sizeType))
            return "/p640x640";
        return "";
    }
}
