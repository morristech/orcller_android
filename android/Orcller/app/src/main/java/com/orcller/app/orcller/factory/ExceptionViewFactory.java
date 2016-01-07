package com.orcller.app.orcller.factory;

import android.view.ViewGroup;

import com.orcller.app.orcller.R;

import pisces.psuikit.widget.ExceptionView;

/**
 * Created by pisces on 12/21/15.
 */
public class ExceptionViewFactory {
    public enum Type {
        NoActivity(1),
        NoAlbum(2),
        NoAlbumAsk(3),
        NoAlbumCoedit(4),
        NoAlbumInvite(5),
        NoAlbumMine(6),
        NoCollaboration(7),
        NoListData(8),
        NoMedia(9),
        NoRecommendation(10),
        NoPermissionForAlbum(11),
        NoPhotos(12),
        NoStar(13),
        NoTimeline(14),
        DoseNotExistAlbum(15),
        NetworkError(16),
        UnknownError(17);

        private int value;

        private Type(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }
    }

    // ================================================================================================
    //  Public
    // ================================================================================================

    public static ExceptionView create(Type type, ViewGroup parentView) {
        if (parentView == null)
            return null;

        ExceptionView view = new ExceptionView(parentView.getContext(), parentView);

        if (Type.NoAlbum.equals(type)) {
            view.setTitleText(R.string.m_exception_title_no_album);
            view.setDescriptionText(R.string.m_exception_desc_no_album);
        } else if (Type.NoAlbumMine.equals(type)) {
            view.setTitleText(R.string.m_exception_title_no_album);
            view.setDescriptionText(R.string.m_exception_desc_no_album_mine);
            view.setButtonText(R.string.w_title_new_album);
        } else if (Type.NoAlbumAsk.equals(type)) {
            view.setTitleText(R.string.m_exception_title_no_album_ask);
            view.setDescriptionText(R.string.m_exception_desc_no_album_ask);
        } else if (Type.NoAlbumCoedit.equals(type)) {
            view.setTitleText(R.string.m_exception_title_no_album);
            view.setDescriptionText(R.string.m_exception_desc_no_album_coedit);
            view.setButtonText(R.string.w_invite);
        } else if (Type.NoAlbumInvite.equals(type)) {
            view.setTitleText(R.string.m_exception_title_no_album_invite);
            view.setDescriptionText(R.string.m_exception_desc_no_album_invite);
            view.setButtonText(R.string.w_title_new_album);
        } else if (Type.NoTimeline.equals(type)) {
            view.setTitleText(R.string.m_exception_title_no_timeline);
            view.setDescriptionText(R.string.m_exception_desc_no_timeline);
            view.setButtonText(R.string.w_title_new_album);
        } else if (Type.NoMedia.equals(type)) {
            view.setTitleText(R.string.m_exception_title_no_media);
            view.setDescriptionText(R.string.m_exception_desc_no_media);
            view.setButtonText(R.string.w_add_photo);
        } else if (Type.NoActivity.equals(type)) {
            view.setTitleText(R.string.m_exception_title_no_activity);
            view.setDescriptionText(R.string.m_exception_desc_no_activity);
        } else if (Type.NoRecommendation.equals(type)) {
            view.setTitleText(R.string.m_exception_title_no_recommendation);
            view.setDescriptionText(R.string.m_exception_desc_no_recommendation);
        } else if (Type.NoCollaboration.equals(type)) {
            view.setTitleText(R.string.m_exception_title_no_collaborations);
            view.setDescriptionText(R.string.m_exception_desc_no_collaborations);
            view.setButtonText(R.string.w_title_ask_collaboration);
        } else if (Type.NoListData.equals(type)) {
            view.setBackgroundResource(R.color.background_gridview_album_create);
            view.setTitleText(R.string.m_exception_title_no_list);
        } else if (Type.NoPermissionForAlbum.equals(type)) {
            view.setTitleText(R.string.m_exception_title_no_permissions);
            view.setDescriptionText(R.string.m_exception_desc_no_permissions);
        } else if (Type.DoseNotExistAlbum.equals(type)) {
            view.setTitleText(R.string.m_exception_title_dose_not_exist_album);
            view.setDescriptionText(R.string.m_exception_desc_dose_not_exist_album);
        } else if (Type.NoStar.equals(type)) {
            view.setTitleText(R.string.m_exception_title_no_star);
            view.setDescriptionText(R.string.m_exception_desc_no_star);
        } else if (Type.NoPhotos.equals(type)) {
            view.setTitleText(R.string.m_exception_title_no_photos);
            view.setDescriptionText(R.string.m_exception_desc_no_photos);
        } else if (Type.UnknownError.equals(type)) {
            view.setTitleText(R.string.m_exception_title_error_unknown);
            view.setDescriptionText(R.string.m_exception_desc_error_unknown);
            view.setButtonText(R.string.w_retry);
        } else if (Type.NetworkError.equals(type)) {
            view.setTitleText(R.string.m_exception_title_error_network);
            view.setDescriptionText(R.string.m_exception_desc_error_network);
            view.setButtonText(R.string.w_retry);
        }

        view.setTag(type);

        return view;
    }
}
