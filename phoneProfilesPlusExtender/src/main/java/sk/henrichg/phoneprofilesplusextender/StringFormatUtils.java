package sk.henrichg.phoneprofilesplusextender;

import android.content.res.Resources;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.BulletSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.URLSpan;
import android.util.TypedValue;

import androidx.core.text.HtmlCompat;

import org.xml.sax.XMLReader;

class StringFormatUtils {

    @SuppressWarnings("SameParameterValue")
    static Spanned fromHtml(String source, boolean forBullets, boolean boldBullet, boolean forNumbers, int numberFrom, int sp, boolean trimTrailingWhiteSpaces) {
        Spanned htmlSpanned;

        if (forNumbers)
            htmlSpanned = HtmlCompat.fromHtml(source, HtmlCompat.FROM_HTML_MODE_COMPACT, null, new LiTagHandler());
        else {
            htmlSpanned = HtmlCompat.fromHtml(source, HtmlCompat.FROM_HTML_MODE_COMPACT);
            //htmlSpanned = HtmlCompat.fromHtml(source, HtmlCompat.FROM_HTML_MODE_COMPACT, null, new GlobalGUIRoutines.LiTagHandler());
        }

        htmlSpanned = removeUnderline(htmlSpanned);

        SpannableStringBuilder result;

        if (forBullets)
            result = addBullets(htmlSpanned, boldBullet);
        else if (forNumbers)
            result = addNumbers(htmlSpanned, numberFrom, sp);
        else
            result = new SpannableStringBuilder(htmlSpanned);

        if (trimTrailingWhiteSpaces)
            result = trimTrailingWhitespace(result);

        return result;
    }

    private static class URLSpanline_none extends URLSpan {
        public URLSpanline_none(String url) {
            super(url);
        }
        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
        }
    }

    private static SpannableStringBuilder removeUnderline(Spanned htmlSpanned) {
        SpannableStringBuilder spannableBuilder = new SpannableStringBuilder(htmlSpanned);
        URLSpan[] spans = spannableBuilder.getSpans(0, spannableBuilder.length(), URLSpan.class);
        for (URLSpan span: spans) {
            int start = spannableBuilder.getSpanStart(span);
            int end = spannableBuilder.getSpanEnd(span);
            spannableBuilder.removeSpan(span);
            span = new URLSpanline_none(span.getURL());
            spannableBuilder.setSpan(span, start, end, 0);
        }
        return spannableBuilder;
    }

    private static SpannableStringBuilder addBullets(Spanned htmlSpanned, boolean boldBullet) {
        SpannableStringBuilder spannableBuilder = new SpannableStringBuilder(htmlSpanned);
        BulletSpan[] spans = spannableBuilder.getSpans(0, spannableBuilder.length(), BulletSpan.class);
        if (spans != null) {
            for (BulletSpan span : spans) {
                int start = spannableBuilder.getSpanStart(span);
                int end = spannableBuilder.getSpanEnd(span);
                spannableBuilder.removeSpan(span);
                int radius = dip(2);
                if (boldBullet)
                    radius += 1;
                spannableBuilder.setSpan(new ImprovedBulletSpan(radius, dip(8)/*, 0*/), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }
        return spannableBuilder;
    }

    private static SpannableStringBuilder addNumbers(Spanned htmlSpanned, int numberFrom, int sp) {
        int listItemCount = numberFrom-1;
        SpannableStringBuilder spannableBuilder = new SpannableStringBuilder(htmlSpanned);
        BulletSpan[] spans = spannableBuilder.getSpans(0, spannableBuilder.length(), BulletSpan.class);
        if (spans != null) {
            for (BulletSpan span : spans) {
                int start = spannableBuilder.getSpanStart(span);
                int end = spannableBuilder.getSpanEnd(span);
                spannableBuilder.removeSpan(span);
                ++listItemCount;
                spannableBuilder.insert(start, listItemCount + ". ");
                spannableBuilder.setSpan(new LeadingMarginSpan.Standard(0, sip(sp)), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }
        return spannableBuilder;
    }

    public static SpannableStringBuilder trimTrailingWhitespace(SpannableStringBuilder source) {

        if (source == null)
            return null;

        int i = source.length();

        // loop back to the first non-whitespace character
        //noinspection StatementWithEmptyBody
        while (--i >= 0 && Character.isWhitespace(source.charAt(i))) {
        }

        return (SpannableStringBuilder) source.subSequence(0, i + 1);
    }

    static int dip(int dp) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics()));
    }

    static int sip(int sp) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, Resources.getSystem().getDisplayMetrics()));
    }

    private static class LiTagHandler implements Html.TagHandler {

        @Override
        public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {

            class Bullet {
            }

            if (tag.equals("li") && opening) {
                output.setSpan(new Bullet(), output.length(), output.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }
            if (tag.equals("li") && !opening) {
                //output.append("\n\n");
                output.append(StringConstants.CHAR_NEW_LINE);
                Bullet[] spans = output.getSpans(0, output.length(), Bullet.class);
                if (spans != null) {
                    Bullet lastMark = spans[spans.length - 1];
                    int start = output.getSpanStart(lastMark);
                    output.removeSpan(lastMark);
                    if (start != output.length()) {
                        output.setSpan(new BulletSpan(), start, output.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    }
                }
            }
        }

    }

}
