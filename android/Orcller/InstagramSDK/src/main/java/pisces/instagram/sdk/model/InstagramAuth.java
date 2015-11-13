package pisces.instagram.sdk.model;

import pisces.psfoundation.model.AbstractModel;

/**
 * Created by pisces on 11/13/15.
 */
public class InstagramAuth extends AbstractModel {
    private String code;
    private String accessToken;

    public InstagramAuth(String code, String accessToken) {
        this.code = code;
        this.accessToken = accessToken;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public static class Builder {
        private String code;
        private String accessToken;

        public InstagramAuth build() {
            return new InstagramAuth(code, accessToken);
        }

        public void setCode(String code) {
            this.code = code;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }
    }
}
