package pisces.psfoundation.utils;

import android.text.format.DateUtils;

import java.util.Date;

/**
 * Created by pisces on 11/17/15.
 */
public class DateUtil {
    public static CharSequence getRelativeTimeSpanString(long unixtimestamp) {
        return DateUtils.getRelativeTimeSpanString(toDate(unixtimestamp).getTime(), new Date().getTime(), DateUtils.SECOND_IN_MILLIS);
    }

    public static long toUnixtimestamp(Date date) {
        return date.getTime() / 1000;
    }

    public static Date toDate(long unixtimestamp) {
        return new Date(unixtimestamp * 1000);
    }
}
