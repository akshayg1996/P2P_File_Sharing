package utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {
    public static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");

    @Override
    public String format(LogRecord record) {
        return getFormattedMessage(record.getMessage());
    }

    public static String getFormattedMessage(String message) {
        StringBuffer sb = new StringBuffer();
        sb.append(dateTimeFormatter.format(LocalDateTime.now()));
        sb.append(": ");
        sb.append("Peer ");
        sb.append(message);
        return sb.toString();
    }
}
