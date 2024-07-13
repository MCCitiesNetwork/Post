package io.github.md5sha256.democracypost.util;

import javax.annotation.Nonnull;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MMM d h:mm aaa yyyy");

    @Nonnull
    public static String formatDate(Date date) {
        return DATE_FORMAT.format(date);
    }

}
