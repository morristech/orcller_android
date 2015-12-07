package pisces.psfoundation.html;

import android.text.TextPaint;
import android.text.style.URLSpan;

/**
 * Created by pisces on 12/7/15.
 */
public class URLSpanNoUnderline extends URLSpan {
    public URLSpanNoUnderline(String p_Url) {
        super(p_Url);
    }
    public void updateDrawState(TextPaint p_DrawState) {
        super.updateDrawState(p_DrawState);

        p_DrawState.setUnderlineText(false);
    }

}
