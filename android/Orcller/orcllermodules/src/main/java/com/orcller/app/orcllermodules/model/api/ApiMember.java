package com.orcller.app.orcllermodules.model.api;

import android.os.Build;

import com.orcller.app.orcllermodules.managers.DeviceManager;
import com.orcller.app.orcllermodules.model.APIResult;
import com.orcller.app.orcllermodules.utils.DeviceUtils;

import java.util.Locale;

import pisces.psfoundation.ext.Application;
import pisces.psfoundation.model.Model;

/**
 * Created by pisces on 11/5/15.
 */
public class ApiMember {
    public enum IDProviderType {
        Facebook(1),
        Instagram(2);

        private IDProviderType(int value) {
            this.value = value;
        }

        private int value;

        public int getValue() {
            return value;
        }
    }

    public static class LoginRes extends APIResult {
        public LoginRes.Entity entity;

        public class Entity {
            public String session_token;
        }
    }

    public static class BaseReq extends Model {
        public String device_locale;
        public String device_name;
        public String device_model;
        public String device_system_name;
        public String device_system_version;
        public String device_token;

        public BaseReq() {
            Locale locale = Application.applicationContext().getResources().getConfiguration().locale;
            device_locale = locale.toLanguageTag();
            device_name = Build.DEVICE;
            device_model = DeviceUtils.getDeviceModel();
            device_token = DeviceManager.getDefault().getDeviceToken();
            device_system_name = DeviceManager.SYSTEM_NAME;
            device_system_version = Build.VERSION.RELEASE;
        }
    }

    public static class ChangePasswordReq {
        public String current_password;
        public String change_password;
    }

    public static class JoinWithEmailReq extends BaseReq {
        public String user_id;
        public String user_password;
        public String user_email;
    }

    public static class JoinWithIdpReq extends BaseReq {
        public int idp_type;
        public String idp_user_id;
        public String user_id;
        public String user_link;
        public String user_name;
        public String user_password;
        public String user_picture;
    }

    public static class LoginReq extends BaseReq {
        public String user_id;
        public String user_password;
    }

    public static class LoginWithIdpReq extends BaseReq {
        public int idp_type;
        public String idp_user_id;
    }

    public static class SyncWithIdpReq {
        public int idp_type;
        public String idp_user_id;
    }

    public static class UpdateUserOptionsReq {
        public int user_options_album_permission;
        public int user_options_pns_types;
    }
}
