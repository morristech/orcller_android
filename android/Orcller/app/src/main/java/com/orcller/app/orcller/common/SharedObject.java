package com.orcller.app.orcller.common;

import android.content.SharedPreferences;
import android.graphics.Point;
import android.text.TextUtils;

import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.R;
import com.orcller.app.orcller.event.CoeditEvent;
import com.orcller.app.orcller.event.RelationshipsEvent;
import com.orcller.app.orcller.manager.MediaManager;
import com.orcller.app.orcller.model.Album;
import com.orcller.app.orcller.model.AlbumAdditionalListEntity;
import com.orcller.app.orcller.model.ApplicationOptions;
import com.orcller.app.orcller.model.Comments;
import com.orcller.app.orcller.model.Favorites;
import com.orcller.app.orcller.model.Likes;
import com.orcller.app.orcller.model.NewsCount;
import com.orcller.app.orcller.model.api.ApiCount;
import com.orcller.app.orcller.proxy.CountDataProxy;
import com.orcller.app.orcller.widget.FollowButton;
import com.orcller.app.orcllermodules.managers.ApplicationLauncher;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter.LoginEvent;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.greenrobot.event.EventBus;
import pisces.psfoundation.ext.Application;
import pisces.psfoundation.ext.PSObject;
import pisces.psfoundation.model.Resources;
import pisces.psfoundation.utils.GsonUtil;
import pisces.psfoundation.utils.Log;
import pisces.psfoundation.utils.URLUtils;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by pisces on 11/4/15.
 */
public class SharedObject extends PSObject {
    public static final File CACHE_DIR = Application.applicationContext().getCacheDir();
    public static final File FILES_DIR = Application.applicationContext().getFilesDir();
    public static final File DATA_DIR = new File(CACHE_DIR.getAbsolutePath() + File.separator + "data");
    public static final File TEMP_IMAGE_DIR = new File(CACHE_DIR.getAbsolutePath() + File.separator + "images");
    private static final int NEWS_COUNT_RETRY_DURATION_MINUTES = 1;
    private static final String CACHED_APPLICATION_OPTIONS_KEY = "cachedApplicationOptionsKey";
    private static SharedObject uniqueInstance;
    private long lastNewsCountViewDate;
    private NewsCount newsCount = new NewsCount();
    private ApplicationOptions applicationOptions;

    public enum SizeType {
        Small,
        Medium,
        Large
    }

    public SharedObject() {
        loadCachedOptions();
        EventBus.getDefault().register(this);
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    /**
     * Class methods
     */
    public static SharedObject get() {
        if(uniqueInstance == null) {
            synchronized(SharedObject.class) {
                if(uniqueInstance == null) {
                    uniqueInstance = new SharedObject();
                }
            }
        }
        return uniqueInstance;
    }

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

    public static String getPermissionText(int permission) {
        return Resources.getString(getPermissionTextResId(permission));
    }

    public static int getPermissionTextResId(int permission) {
        if (permission == Album.Permission.Public.value())
            return R.string.w_public;
        if (permission == Album.Permission.Followers.value())
            return R.string.w_open_to_followers;
        if (permission == Album.Permission.Private.value())
            return R.string.w_private;
        return 0;
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
        if (url == null || URLUtils.isWebURL(url) || URLUtils.isLocal(url))
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

    /**
     * Instance methods
     */
    public boolean isAllowsAutoSlide() {
        return applicationOptions.allowsAutoSlide;
    }

    public void setAllowsAutoSlide(boolean allowsAutoSlide) {
        if (allowsAutoSlide == applicationOptions.allowsAutoSlide)
            return;

        applicationOptions.allowsAutoSlide = allowsAutoSlide;

        SharedPreferences.Editor editor = ApplicationLauncher.getDefault().getSharedPreference().edit();
        editor.putString(CACHED_APPLICATION_OPTIONS_KEY, GsonUtil.toGsonString(applicationOptions));
        editor.commit();
    }

    public int getActivityCount() {
        return newsCount.notification;
    }

    public void setActivityCount(int count) {
        if (count == newsCount.notification)
            return;

        newsCount.notification = count;
        postChangeNewsCountEvent();
    }

    public int getCoeditCount() {
        return newsCount.coediting;
    }

    public void setCoeditCount(int count) {
        if (count == newsCount.coediting)
            return;

        newsCount.coediting = count;
        postChangeNewsCountEvent();
    }

    public int getOptionsCount() {
        return Application.isLowerAppVersion(ApplicationLauncher.getDefault().getCurrentVersion()) ? 1 : 0;
    }

    public int getRecommendCount() {
        return newsCount.recommend;
    }

    public void setRecommendCount(int count) {
        if (count == newsCount.recommend)
            return;

        newsCount.recommend = count;
        postChangeNewsCountEvent();
    }

    public int getTimelineCount() {
        return newsCount.newsfeed;
    }

    public void setTimelineCount(int count) {
        if (count == newsCount.newsfeed)
            return;

        newsCount.newsfeed = count;
        postChangeNewsCountEvent();
    }

    public void loadNewsCount() {
        Log.d("lastNewsCountViewDate", lastNewsCountViewDate);
        if (lastNewsCountViewDate < 1) {
            loadNewsCountDireclty();
        } else {
            long different = new Date().getTime() - lastNewsCountViewDate;
            long elapsedMinutes = different / (1000 * 60);

            if (elapsedMinutes > NEWS_COUNT_RETRY_DURATION_MINUTES)
                loadNewsCountDireclty();
        }
    }

    public void loadNewsCountDireclty() {
        if (invalidDataLoading())
            return;

        CountDataProxy.getDefault().news(new Callback<ApiCount.NewsCountRes>() {
            @Override
            public void onResponse(Response<ApiCount.NewsCountRes> response, Retrofit retrofit) {
                if (response.isSuccess() && response.body().isSuccess()) {
                    newsCount = response.body().entity;
                    postChangeNewsCountEvent();
                    lastNewsCountViewDate = new Date().getTime();
                } else {
                    if (BuildConfig.DEBUG)
                        Log.e("Api Error", response.body());
                }

                endDataLoading();
            }

            @Override
            public void onFailure(Throwable t) {
                if (BuildConfig.DEBUG)
                    Log.e("onFailure", t);

                endDataLoading();
            }
        });
    }

    // ================================================================================================
    //  Interface Implementation
    // ================================================================================================

    /**
     * EventBus listener
     */
    public void onEventMainThread(final Object event) {
        if (event instanceof RelationshipsEvent ||
                (event instanceof LoginEvent && LoginEvent.LOGIN.equals(((LoginEvent) event).getType())) ||
                event instanceof CoeditEvent) {
            loadNewsCountDireclty();
        }
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    /**
     * Class methods
     */
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

    /**
     * Instance methods
     */
    private void loadCachedOptions() {
        String string = ApplicationLauncher.getDefault().getSharedPreference().getString(CACHED_APPLICATION_OPTIONS_KEY, null);
        applicationOptions = string != null ? GsonUtil.fromJson(string, ApplicationOptions.class) : new ApplicationOptions();
    }

    private void postChangeNewsCountEvent() {
        EventBus.getDefault().post(new Event(Event.CHANGE_NEWS_COUNT, this));
    }

    // ================================================================================================
    //  Class: Event
    // ================================================================================================

    public static class Event extends pisces.psfoundation.event.Event {
        public static final String CHANGE_NEWS_COUNT = "changeNewsCount";

        public Event(String type, Object target) {
            super(type, target);
        }
    }
}
