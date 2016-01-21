package com.orcller.app.orcller.proxy;

import android.content.Intent;

import com.orcller.app.orcller.activity.AlbumCreateActivity;
import com.orcller.app.orcller.activity.AlbumViewActivity;
import com.orcller.app.orcller.activity.CoeditInviteActivity;
import com.orcller.app.orcller.activity.CoeditViewActivity;
import com.orcller.app.orcller.activity.MemberJoinInputActivity;
import com.orcller.app.orcller.activity.OptionsActivity;
import com.orcller.app.orcller.activity.ProfileActivity;
import com.orcller.app.orcller.utils.CustomSchemeGenerator;

import java.util.Arrays;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.utils.Log;

/**
 * Created by pisces on 1/13/16.
 */
public class OpenUrlProxy {
    public static void run(Intent intent) {
        if (intent == null || intent.getData() == null)
            return;

        String category;
        int viewType;
        String[] paths = intent.getData().getPath().split("/");

        if (intent.getData().getScheme().equals("http")) {
            category = paths.length > 0 ? paths[0] : null;
            viewType = paths.length > 1 ? Integer.valueOf(paths[1]) : 0;
        } else {
            category = intent.getData().getHost();
            viewType = paths.length > 0 ? Integer.valueOf(paths[0]) : 0;
        }

        if (category == null || viewType < 1)
            return;

        Class clazz = null;

        if (CustomSchemeGenerator.Category.Users.equals(category)) {
            if (CustomSchemeGenerator.ViewTypeUsers.Profile.equals(viewType))
                clazz = ProfileActivity.class;
        } else if (CustomSchemeGenerator.Category.Member.equals(category)) {
            if (CustomSchemeGenerator.ViewTypeMember.JoinInputView.equals(viewType))
                clazz = MemberJoinInputActivity.class;
        } else if (CustomSchemeGenerator.Category.Album.equals(category)) {
            if (CustomSchemeGenerator.ViewTypeAlbum.View.equals(viewType))
                clazz = AlbumViewActivity.class;
            else if (CustomSchemeGenerator.ViewTypeAlbum.Create.equals(viewType))
                clazz = AlbumCreateActivity.class;
        } else if (CustomSchemeGenerator.Category.Coediting.equals(category)) {
            if (CustomSchemeGenerator.ViewTypeCoediting.View.equals(viewType))
                clazz = CoeditViewActivity.class;
            else if (CustomSchemeGenerator.ViewTypeCoediting.Invite.equals(viewType))
                clazz = CoeditInviteActivity.class;
        } else if (CustomSchemeGenerator.Category.Options.equals(category)) {
            clazz = OptionsActivity.class;
        }

        if (clazz != null) {
            Intent newIntent = new Intent(Application.applicationContext(), clazz);
            newIntent.setData(intent.getData());
            Application.getTopActivity().startActivity(newIntent);
        }
    }
}
