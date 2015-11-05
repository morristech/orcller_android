package com.orcller.app.orcllermodules.model.api;

import com.google.gson.annotations.SerializedName;
import com.orcller.app.orcllermodules.model.APIResult;

/**
 * Created by pisces on 11/5/15.
 */
public class ApiMember {
    public enum IDProviderType {
        Facebook(1),
        Instagram(2);

        private IDProviderType(int type) {
            this.type = type;
        }

        private int type;

        public int getType() {
            return type;
        }
    }

    public class LoginRes extends APIResult {
        @SerializedName("entity")
        public LoginRes.Entity entity;

        public class Entity {
            public String session_token;
        }
    }

    public class Behavior {
        public String device_locale;
        public String device_name;
        public String device_model;
        public String device_system_name;
        public String device_system_version;
        public String device_token;
    }

    public class ChangePasswordReq {
        public String current_password;
        public String change_password;
    }

    public class JoinWithEmailReq extends Behavior {
        public String user_id;
        public String user_password;
        public String user_email;
    }

    public class JoinWithIdpReq extends Behavior {
        public IDProviderType idp_type;
        public String idp_user_id;
        public String user_id;
        public String user_link;
        public String user_name;
        public String user_password;
        public String user_picture;
    }

    public class LoginReq extends Behavior {
        public String user_id;
        public String user_password;
    }

    public class LoginWithIdpReq extends Behavior {
        public IDProviderType idp_type;
        public String idp_user_id;
    }

    public class SyncWithIdpReq {
        public IDProviderType idp_type;
        public String idp_user_id;
    }

    public class UpdateUserOptionsReq {
        public int user_options_album_permission;
        public int user_options_pns_types;
    }
}
