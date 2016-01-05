package com.orcller.app.orcller.proxy;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.orcller.app.orcller.BuildConfig;
import com.orcller.app.orcller.model.Album;
import com.orcller.app.orcller.model.Media;
import com.orcller.app.orcller.model.Page;
import com.orcller.app.orcller.model.Pages;
import com.orcller.app.orcller.model.api.ApiAlbum;
import com.orcller.app.orcller.model.api.ApiUsers;
import com.orcller.app.orcllermodules.model.ApiResult;
import com.orcller.app.orcllermodules.proxy.AbstractDataProxy;

import java.util.HashMap;
import java.util.Map;

import pisces.psfoundation.ext.Application;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Converter;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.Field;
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
    private Map<String, CacheObject> cachedPagesMap = new HashMap<>();
    private Map<String, String> remainPageRequestMap = new HashMap<>();

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
        return BuildConfig.API_BASE_URL + "/album/";
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

    public void clearCacheAfterCompare(ApiUsers.AlbumList list) {
        for (Album album : list.data) {
            clearCacheAfterCompare(album);
        }
    }

    public void clearCacheAfterCompare(Album album) {
        String key = String.valueOf(album.id);

        if (cachedPagesMap.containsKey(key)) {
            CacheObject object = cachedPagesMap.get(key);

            if (object.album_updated_time != album.updated_time)
                clearCache(album.id);
        }
    }

    public void clearCaches() {
        cachedPagesMap.clear();
    }

    public void comments(long albumId, int limit, String prev, Callback<ApiAlbum.CommentsRes> callback) {
        enqueueCall(service().comments(albumId, limit, prev), callback);
    }

    public void comment(long albumId, String message, Callback<ApiAlbum.CommentsRes> callback) {
        enqueueCall(service().comment(albumId, message), callback);
    }

    public void create(Album album, Callback<ApiAlbum.AlbumRes> callback) {
        enqueueCall(service().create(album), callback);
    }

    public void delete(long albumId, Callback<ApiResult> callback) {
        enqueueCall(service().delete(albumId), callback);
    }

    public void favorite(Album model, Callback<ApiAlbum.FavoritesRes> callback) {
        if (model.favorites.isParticipated())
            enqueueCall(service().unfavorite(model.id), callback);
        else
            enqueueCall(service().favorite(model.id), callback);
    }

    public void favorites(long albumId, int limit, String after, Callback<ApiAlbum.FavoritesRes> callback) {
        enqueueCall(service().favorites(albumId, limit, after), callback);
    }

    public void like(Album model, Callback<ApiAlbum.LikesRes> callback) {
        if (model.likes.isParticipated())
            enqueueCall(service().unlike(model.id), callback);
        else
            enqueueCall(service().like(model.id), callback);
    }

    public void likeOfPage(Page model, Callback<ApiAlbum.LikesRes> callback) {
        if (model.likes.isParticipated())
            enqueueCall(service().unlikePage(model.id), callback);
        else
            enqueueCall(service().likeOfPage(model.id), callback);
    }

    public void likes(long albumId, int limit, String after, Callback<ApiAlbum.LikesRes> callback) {
        enqueueCall(service().likes(albumId, limit, after), callback);
    }

    public void pages(long albumId, int limit, String after, Callback<ApiAlbum.PagesRes> callback) {
        enqueueCall(service().pages(albumId, limit, after), callback);
    }

    public void remainPages(final Album album) {
        remainPages(album, null);
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

        if (cachedPagesMap.containsKey(key) && completeHandler != null) {
            appendPages(album, cachedPagesMap.get(key).pages, onComplete);
            return;
        }

        if (remainPageRequestMap.containsKey(key) || album.pages.count >= album.pages.total_count) {
            if (completeHandler != null)
                completeHandler.onComplete(false);
            return;
        }

        remainPageRequestMap.put(key, key);

        final Call<ApiAlbum.PagesRes> call = service().pages(album.id, 0, album.pages.after);
        enqueueCall(call, new Callback<ApiAlbum.PagesRes>() {
            @Override
            public void onResponse(final Response<ApiAlbum.PagesRes> response, Retrofit retrofit) {
                new AsyncTask<Void, Void, Pages>() {
                    @Override
                    protected Pages doInBackground(Void... params) {
                        remainPageRequestMap.remove(key);

                        if (response.isSuccess() && response.body().isSuccess()) {
                            ApiAlbum.PagesRes result = response.body();
                            cachedPagesMap.put(key, new CacheObject(album.updated_time, result.entity));
                            return result.entity;
                        }

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Pages result) {
                        super.onPostExecute(result);

                        if (completeHandler != null) {
                            if (result != null)
                                appendPages(album, result, onComplete);
                            else
                                completeHandler.onComplete(false);
                        }
                    }
                }.execute();
            }

            @Override
            public void onFailure(Throwable t) {
                remainPageRequestMap.remove(key);

                if (completeHandler != null)
                    completeHandler.onComplete(false);
            }
        });
    }

    public void report(long albumId, Album.ReportType reportType, Callback<ApiResult> callback) {
        enqueueCall(service().report(albumId, reportType.value()), callback);
    }

    public Service service() {
        return (Service) getCurrentService();
    }

    public void view(long albumId, Callback<ApiAlbum.AlbumRes> callback) {
        enqueueCall(service().view(albumId), callback);
    }

    public void viewByPageId(long pageId, Callback<ApiAlbum.AlbumRes> callback) {
        enqueueCall(service().viewByPageId(pageId), callback);
    }

    public void update(Album album, Callback<ApiAlbum.AlbumRes> callback) {
        clearCache(album.id);
        enqueueCall(service().update(album.id, album), callback);
    }

    public void updatePage(Page page, Callback<ApiResult> callback) {
        enqueueCall(service().updatePage(page.id, page), callback);
    }

    // ================================================================================================
    //  Private
    // ================================================================================================

    private void appendPages(final Album album, final Pages pages, final Runnable completion) {
        if (pages == null)
            return;

        Application.run(new Runnable() {
            @Override
            public void run() {
                album.pages.count += pages.count;
                album.pages.total_count = album.pages.count;
                album.pages.data.addAll(pages.data);
            }
        }, new Runnable() {
            @Override
            public void run() {
                album.didChangeProperties();
                completion.run();
            }
        });
    }

    // ================================================================================================
    //  Interface: Service
    // ================================================================================================

    public interface Service {
        @GET("{albumId}/comments")
        Call<ApiAlbum.CommentsRes> comments(
                @Path("albumId") long albumId,
                @Query("limit") int limit,
                @Query(value = "prev", encoded = true) String prev);

        @GET("page/{pageId}/comments")
        Call<ApiAlbum.CommentsRes> commentsOfPage(
                @Path("pageId") long pageId,
                @Query("limit") int limit,
                @Query(value = "prev", encoded = true) String prev);

        @FormUrlEncoded
        @POST("{albumId}/comment")
        Call<ApiAlbum.CommentsRes> comment(@Path("albumId") long albumId, @Field("message") String message);

        @FormUrlEncoded
        @POST("page/{pageId}/comment")
        Call<ApiAlbum.CommentsRes> commentOfPage(@Path("pageId") long pageId, @Field("message") String message);

        @POST("create")
        Call<ApiAlbum.AlbumRes> create(@Body Album album);

        @DELETE("{albumId}")
        Call<ApiResult> delete(@Path("albumId") long albumId);

        @POST("{albumId}/favorite")
        Call<ApiAlbum.FavoritesRes> favorite(@Path("albumId") long albumId);

        @GET("{albumId}/favorites")
        Call<ApiAlbum.FavoritesRes> favorites(
                @Path("albumId") long albumId,
                @Query("limit") int limit,
                @Query(value = "after", encoded = true) String after);

        @POST("{albumId}/like")
        Call<ApiAlbum.LikesRes> like(@Path("albumId") long albumId);

        @POST("page/{pageId}/like")
        Call<ApiAlbum.LikesRes> likeOfPage(@Path("pageId") long pageId);

        @GET("{albumId}/likes")
        Call<ApiAlbum.LikesRes> likes(
                @Path("albumId") long albumId,
                @Query("limit") int limit,
                @Query(value = "after", encoded = true) String after);

        @GET("page/{pageId}/likes")
        Call<ApiAlbum.LikesRes> likesOfPage(
                @Path("pageId") long pageId,
                @Query("limit") int limit,
                @Query(value = "after", encoded = true) String after);

        @GET("{albumId}/pages")
        Call<ApiAlbum.PagesRes> pages(
                @Path("albumId") long albumId,
                @Query("limit") int limit,
                @Query(value = "after", encoded = true) String after);

        @FormUrlEncoded
        @POST("{albumId}/report")
        Call<ApiResult> report(@Path("albumId") long albumId, @Field("report_type") int reportType);

        @GET("{albumId}")
        Call<ApiAlbum.AlbumRes> view(@Path("albumId") long albumId);

        @GET("view_by_page")
        Call<ApiAlbum.AlbumRes> viewByPageId(@Query("page_id") long pageId);

        @DELETE("{albumId}/comment")
        Call<ApiAlbum.CommentsRes> uncomment(@Path("albumId") long albumId, @Query("comment_id") long commentId);

        @DELETE("page/{pageId}/comment")
        Call<ApiAlbum.CommentsRes> uncommentOfPage(@Path("pageId") long pageId, @Query("comment_id") long commentId);

        @DELETE("{albumId}/favorite")
        Call<ApiAlbum.FavoritesRes> unfavorite(@Path("albumId") long albumId);

        @DELETE("{albumId}/like")
        Call<ApiAlbum.LikesRes> unlike(@Path("albumId") long albumId);

        @DELETE("page/{pageId}/like")
        Call<ApiAlbum.LikesRes> unlikePage(@Path("pageId") long pageId);

        @POST("update")
        Call<ApiAlbum.AlbumRes> update(@Query("album_id") long albumId, @Body Album album);

        @POST("page/{pageId}")
        Call<ApiResult> updatePage(@Path("pageId") long pageId, @Body Page page);
    }

    // ================================================================================================
    //  Class: CacheObject
    // ================================================================================================

    private class CacheObject {
        public long album_updated_time;
        public Pages pages;

        public CacheObject(long album_updated_time, Pages pages) {
            this.album_updated_time = album_updated_time;
            this.pages = pages;
        }
    }

    // ================================================================================================
    //  Interface: CompleteHandler
    // ================================================================================================

    public interface CompleteHandler {
        void onComplete(boolean isSuccess);
    }
}
