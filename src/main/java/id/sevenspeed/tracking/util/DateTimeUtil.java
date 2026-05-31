package id.sevenspeed.tracking.util;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtil {

    private DateTimeUtil() {
        // utility class, no instantiation
    }

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    public static String format(OffsetDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.atZoneSameInstant(ZoneId.of("Asia/Jakarta"))
                .format(FORMATTER);
    }

    public static String now() {
        return FORMATTER.format(OffsetDateTime.now());
    }
}