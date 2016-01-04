package com.orcller.app.orcller.factory;

import android.view.ViewGroup;

import com.orcller.app.orcller.R;

import pisces.psuikit.widget.ExceptionView;

/**
 * Created by pisces on 12/21/15.
 */
public class ExceptionViewFactory {
    public enum Type {
        NoAlbumMine(1),
        NoAlbum(2),
        NoAlbumCoedit(3),
        NoTimeline(4),
        NoActivity(5),
        NoRecommendation(6),
        NoCollaboration(7),
        NoListData(8),
        NoPermissionForAlbum(9),
        DoseNotExistAlbum(10),
        DoseNotExistStar(11),
        DoseNotExistPhoto(12),
        NetworkError(13),
        UnknownError(14);

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
        } else if (Type.NoAlbumCoedit.equals(type)) {
            view.setTitleText(R.string.m_exception_title_no_album);
            view.setDescriptionText(R.string.m_exception_desc_no_album_coedit);
            view.setButtonText(R.string.w_invite);
        } else if (Type.NoTimeline.equals(type)) {
            view.setTitleText(R.string.m_exception_title_no_timeline);
            view.setDescriptionText(R.string.m_exception_desc_no_timeline);
            view.setButtonText(R.string.w_title_new_album);
        } else if (Type.NoActivity.equals(type)) {
            view.setTitleText(R.string.m_exception_title_no_activity);
            view.setDescriptionText(R.string.m_exception_desc_no_activity);
        } else if (Type.NoRecommendation.equals(type)) {
            view.setTitleText(R.string.m_exception_title_no_recommendation);
            view.setDescriptionText(R.string.m_exception_desc_no_recommendation);
        } else if (Type.NoCollaboration.equals(type)) {
            view.setTitleText(R.string.m_exception_title_no_collaborations);
            view.setDescriptionText(R.string.m_exception_desc_no_collaborations);
        } else if (Type.NoListData.equals(type)) {
            view.setBackgroundResource(R.color.background_gridview_album_create);
            view.setTitleText(R.string.m_exception_title_no_list);
        } else if (Type.NoPermissionForAlbum.equals(type)) {
            view.setTitleText(R.string.m_exception_title_no_permissions);
            view.setDescriptionText(R.string.m_exception_desc_no_permissions);
        } else if (Type.DoseNotExistAlbum.equals(type)) {
            view.setTitleText(R.string.m_exception_title_dose_not_exist_album);
            view.setDescriptionText(R.string.m_exception_desc_dose_not_exist_album);
        } else if (Type.DoseNotExistStar.equals(type)) {
            view.setTitleText(R.string.m_exception_title_no_star);
            view.setDescriptionText(R.string.m_exception_desc_no_star);
        } else if (Type.DoseNotExistPhoto.equals(type)) {
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
