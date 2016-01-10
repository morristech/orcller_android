package pisces.psfoundation.utils;

import android.text.format.DateUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import pisces.android.psfoundation.R;
import pisces.psfoundation.model.Resources;

/**
 * Created by pisces on 11/17/15.
 */
public class DateUtil {
    public static CharSequence getRelativeTimeSpanString(long unixtimestamp) {
        long diffInMs = new Date().getTime() - toDate(unixtimestamp).getTime();
        long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMs);
        if (diffInSec < 60) return Resources.getString(R.string.w_just_now);
        return DateUtils.getRelativeTimeSpanString(toDate(unixtimestamp).getTime(), new Date().getTime(), DateUtils.SECOND_IN_MILLIS);
    }

    public static long toUnixtimestamp(Date date) {
        return date.getTime() / 1000;
    }

    public static Date toDate(long unixtimestamp) {
        return new Date(unixtimestamp * 1000);
    }
}
