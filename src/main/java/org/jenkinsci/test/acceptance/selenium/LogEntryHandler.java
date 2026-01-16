package org.jenkinsci.test.acceptance.selenium;

import java.util.function.Consumer;
import java.util.logging.Logger;
import org.openqa.selenium.bidi.log.GenericLogEntry;
import org.openqa.selenium.bidi.log.LogLevel;
import org.openqa.selenium.bidi.log.StackTrace;

public class LogEntryHandler<T extends GenericLogEntry> implements Consumer<T> {

    private static final Logger logger = Logger.getLogger(LogEntryHandler.class.getName());
    private final LogLevel minimumLogLevel;
    private final String loggerPrefix;

    /**
     * Create a new LogEntryHandler that will accept all events of type T and log them to the logger with the supplied name as long as they are not a finer log than the minimum log level.
     * @param minimumLogLevel the lowest level of logs that we want to output.
     * @param loggerPrefix prefix to apply to the logs when logging, so different log streams can be identified.
     */
    public LogEntryHandler(LogLevel minimumLogLevel, String loggerPrefix) {
        this.minimumLogLevel = minimumLogLevel;
        this.loggerPrefix = loggerPrefix;
    }

    @Override
    public void accept(T t) {
        if (shouldLog(t.getLevel())) {
            logger.info(toString(t));
        }
    }

    private boolean shouldLog(LogLevel level) {
        return switch (level) {
            case ERROR -> true;
            case WARNING ->
                minimumLogLevel == LogLevel.WARNING
                        || minimumLogLevel == LogLevel.INFO
                        || minimumLogLevel == LogLevel.DEBUG;
            case INFO -> minimumLogLevel == LogLevel.INFO || minimumLogLevel == LogLevel.DEBUG;
            case DEBUG -> minimumLogLevel == LogLevel.DEBUG;
        };
    }

    private String toString(T entry) {
        StringBuilder sb = new StringBuilder();
        sb.append(loggerPrefix);
        sb.append("[" + entry.getLevel() + "] ");
        sb.append(entry.getText());
        StackTrace stackTrace = entry.getStackTrace();
        if (stackTrace != null) {
            sb.append(stackTrace.getCallFrames().stream()
                    .map(t -> t.getUrl() + "\t" + t.getFunctionName() + ":" + t.getLineNumber() + "["
                            + t.getColumnNumber() + "]")
                    .collect(java.util.stream.Collectors.joining("\n\t", "\n\t", "")));
        }
        return sb.toString();
    }
}
