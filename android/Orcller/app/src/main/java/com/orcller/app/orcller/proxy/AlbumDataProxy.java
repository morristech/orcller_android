package com.orcller.app.orcller.proxy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.orcller.app.orcller.model.album.Album;
import com.orcller.app.orcller.model.album.ImageMedia;
import com.orcller.app.orcller.model.album.Media;
import com.orcller.app.orcller.model.album.Pages;
import com.orcller.app.orcller.model.album.VideoMedia;
import com.orcller.app.orcller.model.api.ApiAlbum;
import com.orcller.app.orcllermodules.model.APIResult;
import com.orcller.app.orcllermodules.proxy.AbstractDataProxy;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.Log;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Converter;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by pisces on 11/16/15.
 */
public class AlbumDataProxy extends AbstractDataProxy {
    private static AlbumDataProxy uniqueInstance;
    private Map<String, Pages> cachedPagesMap = new HashMap<String, Pages>();
    private Map<String, Album> remainPageRequestMap = new HashMap<String, Album>();

    // ================================================================================================
    //  Overridden: AbstractDataProxy
    // ================================================================================================

    @Override
    protected Converter.Factory createConverterFactory() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Media.class, new MediaDeserializer())
                .create();
        return GsonConverterFactory.create(gson);
    }

    @Override
    protected Class<Service> createServiceClass() {
        return Service.class;
    }

    @Override
    protected String createBaseUrl() {
        return Application.applicationContext().getString(com.orcller.app.orcllermodules.R.string.server_base_url) + "/album/";
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static AlbumDataProxy getDefault() {
        if(uniqueInstance == null) {
            synchronized(AlbumDataProxy.class) {
                if(uniqueInstance == null) {
                    uniqueInstance = new AlbumDataProxy();
                }
            }
        }
        return uniqueInstance;
    }

    public void clearCache(long albumId) {
        cachedPagesMap.remove(String.valueOf(albumId));
    }

    public void clearCaches() {
        cachedPagesMap.clear();
    }

    public void comments(long albumId, int limit, String prev, Callback<ApiAlbum.CommentsRes> callback) {
        enqueueCall(service().comments(albumId, limit, prev), callback);
    }

    public void commentsOfPage(long pageId, int limit, String prev, Callback<ApiAlbum.CommentsRes> callback) {
        enqueueCall(service().commentsOfPage(pageId, limit, prev), callback);
    }

    public void comment(long albumId, String message, Callback<APIResult> callback) {
        enqueueCall(service().comment(albumId, message), callback);
    }

    public void commentOfPage(long pageId, String message, Callback<APIResult> callback) {
        enqueueCall(service().commentOfPage(pageId, message), callback);
    }

    public void create(Album album, Callback<ApiAlbum.AlbumRes> callback) {
        enqueueCall(service().create(album), callback);
    }

    public void favorite(long albumId, Callback<APIResult> callback) {
        enqueueCall(service().favorite(albumId), callback);
    }

    public void favorites(long albumId, int limit, String after, Callback<APIResult> callback) {
        enqueueCall(service().favorites(albumId, limit, after), callback);
    }

    public void like(long albumId, Callback<APIResult> callback) {
        enqueueCall(service().like(albumId), callback);
    }

    public void likeOfPage(long pageId, Callback<APIResult> callback) {
        enqueueCall(service().likeOfPage(pageId), callback);
    }

    public void likes(long albumId, int limit, String after, Callback<ApiAlbum.LikesRes> callback) {
        enqueueCall(service().likes(albumId, limit, after), callback);
    }

    public void likesOfPage(long pageId, int limit, String after, Callback<ApiAlbum.LikesRes> callback) {
        enqueueCall(service().likesOfPage(pageId, limit, after), callback);
    }

    public void pages(long albumId, int limit, String after, Callback<ApiAlbum.PagesRes> callback) {
        enqueueCall(service().pages(albumId, limit, after), callback);
    }

    public void remainPages(final Album album, final CompleteHandler completeHandler) {
        final String key = String.valueOf(album.id);
        final Runnable onComplete = new Runnable() {
            @Override
            public void run() {
                if (completeHandler != null)
                    completeHandler.onComplete(true);
            }
        };

        if (cachedPagesMap.containsKey(key)) {
            appendPages(album, cachedPagesMap.get(key), onComplete);
            return;
        }

        if (remainPageRequestMap.containsKey(key) || album.pages.count >= album.pages.total_count) {
            if (completeHandler != null)
                completeHandler.onComplete(false);
            return;
        }

        remainPageRequestMap.put(key, album);

        final Call<ApiAlbum.PagesRes> call = service().pages(album.id, 0, album.pages.after);
        enqueueCall(call, new Callback<ApiAlbum.PagesRes>() {
            @Override
            public void onResponse(Response<ApiAlbum.PagesRes> response, Retrofit retrofit) {
                remainPageRequestMap.remove(key);

                if (response.isSuccess()) {
                    ApiAlbum.PagesRes result = response.body();

                    if (response.isSuccess()) {
                        cachedPagesMap.put(key, result.entity);
                        appendPages(album, result.entity, onComplete);
                        return;
                    }
                }

                if (completeHandler != null)
                    completeHandler.onComplete(false);
            }

            @Override
            public void onFailure(Throwable t) {
                remainPageRequestMap.remove(key);

                if (completeHandler != null)
                    completeHandler.onComplete(false);
            }
        });
    }

    public void report(long albumId, Album.ReportType reportType, Callback<APIResult> callback) {
        enqueueCall(service().report(albumId, reportType.getValue()), callback);
    }

    public void view(long albumId, Callback<ApiAlbum.AlbumRes> callback) {
        enqueueCall(service().view(albumId), callback);
    }

    public void viewByPageId(long pageId, Callback<ApiAlbum.AlbumRes> callback) {
        enqueueCall(service().viewByPageId(pageId), callback);
    }

    public void uncomment(long albumId, long commentId, Callback<APIResult> callback) {
        enqueueCall(service().uncomment(albumId, commentId), callback);
    }

    public void uncommentOfPage(long pageId, long commentId, Callback<APIResult> callback) {
        enqueueCall(service().uncommentOfPage(pageId, commentId), callback);
    }

    public void unfavorite(long albumId, Callback<APIResult> callback) {
        enqueueCall(service().unfavorite(albumId), callback);
    }

    public void unlike(long albumId, Callback<APIResult> callback) {
        enqueueCall(service().unlike(albumId), callback);
    }

    public void unlikePage(long pageId, Callback<APIResult> callback) {
        enqueueCall(service().unlikePage(pageId), callback);
    }

    public void update(Album album, Callback<ApiAlbum.AlbumRes> callback) {
        clearCache(album.id);
        enqueueCall(service().update(album.id, album), callback);
    }

    public void updatePage(long pageId, String desc, Callback<APIResult> callback) {
        enqueueCall(service().updatePage(pageId, desc), callback);
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void appendPages(final Album album, final Pages pages, final Runnable runnable) {
        if (pages == null)
            return;

        Application.runOnBackgroundThread(new Runnable() {
            @Override
            public void run() {
                try {
                    album.pages.count += pages.count;
                    album.pages.total_count = album.pages.count;
                    album.pages.data.addAll(pages.data);
                    album.pages.didChangeProperties();

                    Application.runOnMainThread(runnable);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Service service() {
        return (Service) getCurrentService();
    }

    // ================================================================================================
    //  Class: MediaDeserializer
    // ================================================================================================

    private static class MediaDeserializer implements JsonDeserializer<Media> {
        @Override
        public Media deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
                throws JsonParseException {
            int _type = je.getAsJsonObject().get("type").getAsInt();

            if (_type == Media.Type.Video.getValue())
                return new Gson().fromJson(je, VideoMedia.class);
            else if (_type == Media.Type.Image.getValue())
                return new Gson().fromJson(je, ImageMedia.class);
            return null;
        }
    }

    // ================================================================================================
    //  Interface: Service
    // ================================================================================================

    public interface Service {
        @GET("{albumId}/comments")
        Call<ApiAlbum.CommentsRes> comments(@Path("albumId") long albumId, int limit, String prev);

        @GET("page/{pageId}/comments")
        Call<ApiAlbum.CommentsRes> commentsOfPage(@Path("pageId") long pageId, int limit, String prev);

        @FormUrlEncoded
        @POST("{albumId}/comment")
        Call<APIResult> comment(@Path("albumId") long albumId, String message);

        @FormUrlEncoded
        @POST("page/{pageId}/comment")
        Call<APIResult> commentOfPage(@Path("pageId") long pageId, String message);

        @FormUrlEncoded
        @POST("create")
        Call<ApiAlbum.AlbumRes> create(@Body Album album);

        @POST("{albumId}/favorite")
        Call<APIResult> favorite(@Path("albumId") long albumId);

        @GET("{albumId}/favorites")
        Call<ApiAlbum.FavoritesRes> favorites(@Path("albumId") long albumId, int limit, String after);

        @POST("{albumId}/like")
        Call<APIResult> like(@Path("albumId") long albumId);

        @POST("page/{pageId}/like")
        Call<APIResult> likeOfPage(@Path("pageId") long pageId);

        @GET("{albumId}/likes")
        Call<ApiAlbum.LikesRes> likes(@Path("albumId") long albumId, int limit, String after);

        @GET("page/{pageId}/likes")
        Call<ApiAlbum.LikesRes> likesOfPage(@Path("pageId") long pageId, int limit, String after);

        @GET("{albumId}/pages")
        Call<ApiAlbum.PagesRes> pages(@Path("albumId") long albumId, int limit, String after);

        @POST("{albumId}/report")
        Call<APIResult> report(@Path("albumId") long albumId, @Query("report_type") int reportType);

        @GET("{albumId}")
        Call<ApiAlbum.AlbumRes> view(@Path("albumId") long albumId);

        @GET("view_by_page")
        Call<ApiAlbum.AlbumRes> viewByPageId(@Query("page_id") long pageId);

        @DELETE("{albumId}/comment")
        Call<APIResult> uncomment(@Path("albumId") long albumId, @Path("comment_id") long commentId);

        @DELETE("page/{pageId}/comment")
        Call<APIResult> uncommentOfPage(@Path("pageId") long pageId, @Path("comment_id") long commentId);

        @DELETE("{albumId}/favorite")
        Call<APIResult> unfavorite(@Path("albumId") long albumId);

        @DELETE("{albumId}/like")
        Call<APIResult> unlike(@Path("albumId") long albumId);

        @DELETE("page/{pageId}/like")
        Call<APIResult> unlikePage(@Path("pageId") long pageId);

        @FormUrlEncoded
        @POST("update")
        Call<ApiAlbum.AlbumRes> update(@Query("album_id") long albumId, @Body Album album);

        @FormUrlEncoded
        @POST("page/{pageId}")
        Call<APIResult> updatePage(@Path("pageId") long pageId, @Query("desc") String desc);
    }

    // ================================================================================================
    //  Interface: CompleteHandler
    // ================================================================================================

    public interface CompleteHandler {
        void onComplete(boolean isSuccess);
    }
}
