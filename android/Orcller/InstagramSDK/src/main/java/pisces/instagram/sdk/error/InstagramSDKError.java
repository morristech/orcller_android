package pisces.instagram.sdk.error;

/**
 * Created by pisces on 11/13/15.
 */
public class InstagramSDKError extends Error {
    public enum Code {
        DoesNotExistResourceError(-10000),
        AlreadyAuthorizedError(10000),
        InvalidClientIdError(10001),
        InvalidClientSecretError(10002),
        InvalidRedirectURIError(10003),
        NeedAuthorizationError(10004),
        UnknownAPIError(10100);

        private int value;

        private Code(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum Message {
        DoesNotExistResourceError("DoesNotExistResourceError"),
        AlreadyAuthorizedError("AlreadyAuthorizedError"),
        InvalidClientIdError("InvalidClientIdError"),
        InvalidClientSecretError("InvalidClientSecretError"),
        InvalidRedirectURIError("InvalidRedirectURIError"),
        NeedAuthorizationError("NeedAuthorizationError"),
        UnknownAPIError("UnknownAPIError");

        private String value;

        private Message(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private int code;

    public InstagramSDKError(int code, String detailMessage) {
        super(detailMessage);

        this.code = code;
    }

    public InstagramSDKError(Code code, Message detailMessage) {
        super(detailMessage.getValue());

        this.code = code.getValue();
    }

    public int getCode() {
        return code;
    }
}
