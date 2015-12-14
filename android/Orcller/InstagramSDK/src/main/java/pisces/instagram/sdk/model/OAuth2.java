package pisces.instagram.sdk.model;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pisces.instagram.sdk.InstagramApplicationCenter;

/**
 * Created by pisces on 11/13/15.
 */
public class OAuth2 implements Serializable {
    public enum InstagramLoginScope {
        Basic(0),
        Comments(1<<1),
        Relationships(1<<2),
        Likes(1<<3);

        private int value;

        private InstagramLoginScope(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }
    }

    public enum InstagramParametersType {
        Login(1),
        AccessToken(2);

        private int value;

        private InstagramParametersType(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }
    }

    public static final String ACCESS_TOKEN_KEY = "access_token";
    public static final String CLIENT_ID_KEY = "client_id";
    public static final String CLIENT_SECRET_KEY = "client_secret";
    public static final String CODE_KEY = "code";
    public static final String GRANT_KEY = "grant_type";
    public static final String REDIRECT_TYPE_KEY = "redirect_uri";
    public static final String RESPONSE_TYPE_KEY = "response_type";
    public static final String SCOPE_KEY = "scope";
    public static final String[] SCOPE_TYPES = {"basic", "comments", "relationships", "likes"};
    private InstagramLoginScope scope;
    private String clientId;
    private String clientSecret;
    private String redirectURI;

    public Map<String, String> getParameters(InstagramParametersType type) {
        Map<String, String> parameters = null;

        if (type.equals(InstagramParametersType.Login)) {
            parameters = new HashMap<String, String>();
            parameters.put(CLIENT_ID_KEY, clientId);
            parameters.put(CLIENT_SECRET_KEY, clientSecret);
            parameters.put(REDIRECT_TYPE_KEY, redirectURI);
            parameters.put(RESPONSE_TYPE_KEY, "code");
            parameters.put(SCOPE_KEY, getScopeString());
        } else if (type.equals(InstagramParametersType.AccessToken)) {
            parameters = new HashMap<String, String>();
            parameters.put(CLIENT_ID_KEY, clientId);
            parameters.put(CLIENT_SECRET_KEY, clientSecret);
            parameters.put(GRANT_KEY, "authorization_code");
            parameters.put(REDIRECT_TYPE_KEY, redirectURI);
            parameters.put(CODE_KEY, InstagramApplicationCenter.getDefault().getCode());
        }

        return parameters;
    }

    public String getScopeString() {
        List<String> strings = new ArrayList<String>();

        for (int i=0; i<SCOPE_TYPES.length; i++) {
            int enumBitValueToCheck = 1 << i;
            if (((int) scope.value() & enumBitValueToCheck) == 1)
                strings.add(SCOPE_TYPES[i]);
        }

        if (strings.size() < 1)
            return "basic";

        return TextUtils.join("+", strings);
    }

    public InstagramLoginScope getScope() {
        return scope;
    }

    public void setScope(InstagramLoginScope scope) {
        this.scope = scope;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getRedirectURI() {
        return redirectURI;
    }

    public void setRedirectURI(String redirectURI) {
        this.redirectURI = redirectURI;
    }

    public static class Builder {
        private InstagramLoginScope scope;
        private String clientId;
        private String clientSecret;
        private String redirectURI;

        public OAuth2 build() {
            OAuth2 object = new OAuth2();
            object.setScope(scope);
            object.setClientId(clientId);
            object.setClientSecret(clientSecret);
            object.setRedirectURI(redirectURI);
            return object;
        }

        public Builder setScope(InstagramLoginScope scope) {
            this.scope = scope;
            return this;
        }

        public Builder setClientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public Builder setRedirectURI(String redirectURI) {
            this.redirectURI = redirectURI;
            return this;
        }
    }
}
