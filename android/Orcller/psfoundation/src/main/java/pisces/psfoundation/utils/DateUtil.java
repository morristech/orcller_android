package pisces.psfoundation.utils;

import java.util.Date;

/**
 * Created by pisces on 11/17/15.
 */
public class DateUtil {
    public static long toUnixtimestamp(Date date) {
        return date.getTime() / 1000;
    }

    public static Date toDate(long unixtimestamp) {
        return new Date(unixtimestamp * 1000);
    }
}
