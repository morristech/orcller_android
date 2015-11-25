package pisces.psuikit.imagepicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import pisces.psfoundation.model.Model;
import pisces.psfoundation.utils.DateUtil;

/**
 * Created by pisces on 11/24/15.
 */
public class Media extends Model {
    public int id;
    public int dataColumnIndex;
    public long date;
    public String path;

    public String getDateString() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(this.date);

        return new SimpleDateFormat("yyyy. MM. dd").format(calendar.getTime());
    }
}
