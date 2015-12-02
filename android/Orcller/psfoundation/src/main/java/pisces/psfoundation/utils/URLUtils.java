package pisces.psfoundation.utils;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Patterns;
import android.webkit.URLUtil;

import java.net.URL;
import java.util.regex.Pattern;

import pisces.psfoundation.ext.Application;

/**
 * Created by pisces on 11/16/15.
 */
public class URLUtils {
    public static boolean isLocal(CharSequence input) {
        String storageDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        String cacheDir = Application.applicationContext().getCacheDir().getAbsolutePath();
        String regex = "^(" + storageDir + "|" + cacheDir + ")(.*)";
        return Pattern.matches(regex, input);
    }

    public static boolean isWebURL(CharSequence input) {
        if (TextUtils.isEmpty(input))
            return false;

        Pattern URL_PATTERN = Patterns.WEB_URL;
        boolean isWebURL = URL_PATTERN.matcher(input).matches();
        if (!isWebURL) {
            String urlString = input + "";
            if (URLUtil.isNetworkUrl(urlString)) {
                try {
                    new URL(urlString);
                    isWebURL = true;
                } catch (Exception e) {
                }
            }
        }
        return isWebURL;
    }
}
