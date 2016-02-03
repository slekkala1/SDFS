package tempest.services;

import tempest.interfaces.LogWrapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.*;

public class DefaultLogWrapper implements LogWrapper {
    private final java.util.logging.Logger logger = java.util.logging.Logger.getGlobal();

    public void addFileHandler(String file) throws IOException {
        FileHandler fileHandler = new FileHandler(file);
        fileHandler.setFormatter(new SingleLineFormatter());
        logger.addHandler(fileHandler);
    }

    public void logp(Level level, String sourceClass, String sourceMethod, String msg) {
        logger.logp(level, sourceClass, sourceMethod, msg);
    }

    class SingleLineFormatter extends Formatter {

        private static final String format = "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s %2$s %5$s%6$s%n";
        private final Date dat = new Date();

        public synchronized String format(LogRecord record) {
            dat.setTime(record.getMillis());
            String source;
            if (record.getSourceClassName() != null) {
                source = record.getSourceClassName();
                if (record.getSourceMethodName() != null) {
                    source += " " + record.getSourceMethodName();
                }
            } else {
                source = record.getLoggerName();
            }
            String message = formatMessage(record);
            String throwable = "";
            if (record.getThrown() != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                pw.println();
                record.getThrown().printStackTrace(pw);
                pw.close();
                throwable = sw.toString();
            }
            return String.format(format,
                    dat,
                    source,
                    record.getLoggerName(),
                    record.getLevel().getName(),
                    message,
                    throwable);
        }
    }
}
