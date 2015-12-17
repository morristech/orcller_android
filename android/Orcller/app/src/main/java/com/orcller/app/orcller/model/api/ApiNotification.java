package com.orcller.app.orcller.model.api;

import com.orcller.app.orcller.model.album.ListEntity;
import com.orcller.app.orcller.model.album.Notification;
import com.orcller.app.orcllermodules.model.ApiResult;

/**
 * Created by pisces on 12/18/15.
 */
public class ApiNotification {
    public static class NotificationListRes extends ApiResult<NotificationList> {
    }

    public static class NotificationList extends ListEntity<Notification> {
    }
}
