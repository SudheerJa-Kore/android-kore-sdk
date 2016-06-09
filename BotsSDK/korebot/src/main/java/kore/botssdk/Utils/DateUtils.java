package kore.botssdk.utils;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Pradeep Mahato on 09-Jun-16.
 */
public class DateUtils {

    public static final SimpleDateFormat isoFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
    public static final Format dateFormat4 = new SimpleDateFormat("d MMM yyyy 'at' h:mm a", Locale.ENGLISH);

    public static String getTimeStamp(String timeStamp) throws ParseException {
        long timeStampMillis = isoFormatter.parse(timeStamp).getTime();
        return getTimeStamp(timeStampMillis);
    }

    public static String getTimeStamp(long timeStampMillis) {
        return dateFormat4.format(new Date(timeStampMillis));
    }

}