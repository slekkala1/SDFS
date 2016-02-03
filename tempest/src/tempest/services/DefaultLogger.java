package tempest.services;

import tempest.interfaces.Executor;
import tempest.interfaces.LogWrapper;

import java.io.*;
import java.net.Inet4Address;
import java.util.logging.*;

public class DefaultLogger implements tempest.interfaces.Logger {

    private final LogWrapper logWrapper;
    private final String logFile;
    private final String grepFile;
    private final Executor executor;

    public DefaultLogger(Executor executor, LogWrapper logWrapper) throws IOException {
        this(executor, logWrapper, "machine." + Inet4Address.getLocalHost().getHostName() + ".log", "machine." + Inet4Address.getLocalHost().getHostName() + ".log");
    }

    public DefaultLogger(Executor executor, LogWrapper logWrapper, String logfile, String grepFile) throws IOException {
        this.logFile = logfile;
        this.grepFile = grepFile;
        this.executor = executor;
        this.logWrapper = logWrapper;
        logWrapper.addFileHandler(logFile);
    }

    public void logLine(String level, String message) {
        StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[2];
        if (level.equals(SEVERE))
            logWrapper.logp(Level.SEVERE, stackTrace.getClassName(), stackTrace.getMethodName(), message);
        if (level.equals(WARNING))
            logWrapper.logp(Level.WARNING, stackTrace.getClassName(), stackTrace.getMethodName(), message);
        if (level.equals(INFO))
            logWrapper.logp(Level.INFO, stackTrace.getClassName(), stackTrace.getMethodName(), message);
    }

    public String grep(String options) {
        String[] results = executor.execute("grep", options + " " + grepFile);
        StringBuilder resultBuilder = new StringBuilder();
        for (String line : results) {
            resultBuilder.append(line).append(System.lineSeparator());
        }
        return resultBuilder.toString();
    }

    public String getLogFile() {
        return logFile;
    }

    public String getGrepFile() {
        return grepFile;
    }
}
