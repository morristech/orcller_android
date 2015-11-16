package com.orcller.app.orcller.event;

import pisces.psfoundation.model.Model;

/**
 * Created by pisces on 11/16/15.
 */
public class AlbumEvent {
    public enum Type {
        didChangeImagesOfMedia("didChangeImagesOfMedia");

        private String value;

        private Type(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private Type type;
    private Model model;

    public AlbumEvent(Type type, Model model) {
        this.type = type;
        this.model = model;
    }

    public Type getType() {
        return type;
    }

    public Model getModel() {
        return model;
    }
}
