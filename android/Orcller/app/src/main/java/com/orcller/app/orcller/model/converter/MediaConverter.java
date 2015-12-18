package com.orcller.app.orcller.model.converter;

import com.orcller.app.orcller.model.Image;
import com.orcller.app.orcller.model.ImageMedia;
import com.orcller.app.orcller.model.Images;
import com.orcller.app.orcller.model.Media;
import com.orcller.app.orcller.model.Video;
import com.orcller.app.orcller.model.VideoMedia;
import com.orcller.app.orcller.model.Videos;
import com.orcller.app.orcllermodules.managers.AuthenticationCenter;
import com.orcller.app.orcllermodules.model.facebook.FBPhoto;
import com.orcller.app.orcllermodules.model.facebook.FBPhotoImage;
import com.orcller.app.orcllermodules.model.facebook.FBVideo;
import com.orcller.app.orcllermodules.model.facebook.FBVideoThumbnail;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import pisces.instagram.sdk.model.ApiInstagram;
import pisces.psfoundation.utils.DateUtil;
import pisces.psfoundation.utils.GsonUtil;

/**
 * Created by pisces on 11/26/15.
 */
public class MediaConverter {
    private static int MEDIA_UNIQUE_ID = 0;

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static Media convert(Object object) {
        if (object instanceof FBPhoto)
            return convert((FBPhoto) object);
        if (object instanceof FBVideo)
            return convert((FBVideo) object);
        if (object instanceof ApiInstagram.Media)
            return convert((ApiInstagram.Media) object);
        if (object instanceof pisces.psuikit.imagepicker.Media)
            return convert((pisces.psuikit.imagepicker.Media) object);
        if (object instanceof Media)
            return (Media) object;
        return null;
    }

    public static Media convert(FBPhoto photo) {
        List<FBPhotoImage> sorted = (List<FBPhotoImage>) photo.images.clone();

        Collections.sort(sorted, new Comparator<FBPhotoImage>() {
            @Override
            public int compare(FBPhotoImage lhs, FBPhotoImage rhs) {
                if (lhs.width > rhs.width)
                    return 1;
                if (lhs.width < rhs.width)
                    return -1;
                return 0;
            }
        });

        Images images = new Images();
        images.thumbnail = createImage(sorted.get(0));
        images.low_resolution = createImage(sorted.size() > 2 ? sorted.get(2) : sorted.get(sorted.size() - 1));
        images.standard_resolution = createImage(sorted.size() > 5 ? sorted.get(5) : sorted.get(sorted.size() - 1));

        ImageMedia media = new ImageMedia();
        media.origin_type = Media.OriginType.Facebook.value();
        media.origin_id = Long.valueOf(photo.id);
        media.images = images;

        return media;
    }

    public static Media convert(FBVideo video) {
        List<FBVideoThumbnail> sorted = (List<FBVideoThumbnail>) video.thumbnails.data.clone();

        Collections.sort(sorted, new Comparator<FBVideoThumbnail>() {
            @Override
            public int compare(FBVideoThumbnail lhs, FBVideoThumbnail rhs) {
                if (lhs.width > rhs.width)
                    return 1;
                if (lhs.width < rhs.width)
                    return -1;
                return 0;
            }
        });

        Images images = new Images();
        images.thumbnail = createImage(sorted.get(0));
        images.low_resolution = createImage(sorted.size() > 2 ? sorted.get(2) : sorted.get(sorted.size() - 1));
        images.standard_resolution = createImage(sorted.size() > 5 ? sorted.get(5) : sorted.get(sorted.size() - 1));

        Videos videos = new Videos();
        videos.low_resolution = videos.standard_resolution = createVideo(video);

        VideoMedia media = new VideoMedia();
        media.origin_type = Media.OriginType.Facebook.value();
        media.origin_id = Long.valueOf(video.id);
        media.images = images;
        media.videos = videos;

        return media;
    }

    public static Media convert(ApiInstagram.Media media) {
        Media result;

        if (media.isVideo()) {
            result = new VideoMedia();
            ((VideoMedia) result).videos = GsonUtil.fromJson(GsonUtil.toGsonString(media.videos), Videos.class);
        } else {
            result = new ImageMedia();
        }

        result.images = GsonUtil.fromJson(GsonUtil.toGsonString(media.images), Images.class);
        result.origin_type = Media.OriginType.Instagram.value();
        result.origin_id = Long.valueOf(media.id.split("_")[0]);

        return result;
    }

    public static Media convert(pisces.psuikit.imagepicker.Media media) {
        Media result = new ImageMedia();
        result.origin_type = Media.OriginType.Local.value();
        String originIdString = String.valueOf(AuthenticationCenter.getDefault().getUser().user_uid) +
                String.valueOf(DateUtil.toUnixtimestamp(new Date())) +
                (++MEDIA_UNIQUE_ID);

        result.images = new Images();
        result.images.standard_resolution = new Image(media.width, media.height, media.path);
        result.origin_id = Long.valueOf(originIdString);

        return result;
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private static Image createImage(FBPhotoImage photoImage) {
        Image image = new Image();
        image.width = photoImage.width;
        image.height = photoImage.height;
        image.url = photoImage.source;
        return image;
    }

    private static Image createImage(FBVideoThumbnail thumbnail) {
        Image image = new Image();
        image.width = thumbnail.width;
        image.height = thumbnail.height;
        image.url = thumbnail.uri;
        return image;
    }

    private static Video createVideo(FBVideo fbvideo) {
        Video video = new Video();
        video.url = fbvideo.source;
        return video;
    }
}
