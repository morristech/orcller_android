package pisces.psfoundation.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pisces on 11/13/15.
 */
public class MapUtils {
    public static String toQueryString(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        for (HashMap.Entry<String, String> e : map.entrySet()){
            if (sb.length() > 0)
                sb.append('&');

            try {
                sb.append(URLEncoder.encode(e.getKey(), "UTF-8")).append('=').append(URLEncoder.encode(e.getValue(), "UTF-8"));
            } catch (UnsupportedEncodingException exception) {
                exception.printStackTrace();
            }
        }
        return sb.toString();
    }
}
