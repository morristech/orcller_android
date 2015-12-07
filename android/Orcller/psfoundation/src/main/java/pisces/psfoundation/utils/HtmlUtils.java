package pisces.psfoundation.utils;

import android.text.Spannable;
import android.text.style.URLSpan;

import pisces.psfoundation.html.URLSpanNoUnderline;

/**
 * Created by pisces on 12/7/15.
 */
public class HtmlUtils {
    public static Spannable removeUnderlines(Spannable p_Text) {
        URLSpan[] spans = p_Text.getSpans(0, p_Text.length(), URLSpan.class);
        for (URLSpan span : spans) {
            int start = p_Text.getSpanStart(span);
            int end = p_Text.getSpanEnd(span);
            p_Text.removeSpan(span);
            span = new URLSpanNoUnderline(span.getURL());
            p_Text.setSpan(span, start, end, 0);
        }
        return p_Text;
    }
}
