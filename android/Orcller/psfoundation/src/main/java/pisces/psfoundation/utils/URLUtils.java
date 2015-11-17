package pisces.psfoundation.utils;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Patterns;
import android.webkit.URLUtil;

import java.net.URL;
import java.util.regex.Pattern;

/**
 * Created by pisces on 11/16/15.
 */
public class URLUtils {
    public static boolean isLocal(CharSequence input) {
        String regex = "^" + Environment.getExternalStorageDirectory().getAbsolutePath() + "(.*)";
        return Pattern.matches(regex, input);
    }

    public static boolean checkURL(CharSequence input) {
        if (TextUtils.isEmpty(input))
            return false;

        Pattern URL_PATTERN = Patterns.WEB_URL;
        boolean isURL = URL_PATTERN.matcher(input).matches();
        if (!isURL) {
            String urlString = input + "";
            if (URLUtil.isNetworkUrl(urlString)) {
                try {
                    new URL(urlString);
                    isURL = true;
                } catch (Exception e) {
                }
            }
        }
        return isURL;
    }
}
