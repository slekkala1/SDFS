package tempest.mocks;

import tempest.interfaces.Logger;

public class MockLogger implements Logger {
    public String logFile;
    public String grepFile;
    public String grep;

    public void logLine(String level, String message) {
    }

    public String grep(String options) {
        return grep;
    }

    public String getLogFile() {
        return logFile;
    }

    public String getGrepFile() {
        return grepFile;
    }
}
