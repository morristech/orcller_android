package com.orcller.app.orcller.event;

/**
 * Created by pisces on 11/8/15.
 */
public class ViewEvent {
    public ViewEvent() {
    }

    public static class OnFocusChange {
        private boolean hasFocus;

        public OnFocusChange(boolean hasFocus) {
            this.hasFocus = hasFocus;
        }

        public boolean getHasFocus() {
            return hasFocus;
        }
    }
}
