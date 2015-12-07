package com.orcller.app.orcller.proxy;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.orcller.app.orcller.model.album.ImageMedia;
import com.orcller.app.orcller.model.album.Media;
import com.orcller.app.orcller.model.album.VideoMedia;

import java.lang.reflect.Type;

/**
 * Created by pisces on 12/8/15.
 */
public class MediaDeserializer implements JsonDeserializer<Media> {
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
