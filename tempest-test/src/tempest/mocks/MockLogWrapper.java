package tempest.mocks;

import tempest.interfaces.LogWrapper;

import java.util.logging.Handler;
import java.util.logging.Level;

public class MockLogWrapper implements LogWrapper {
    public int addHandlerCallCount;
    public int logpCallCount;
    public String file;
    public Level lastLevel;
    public String lastSourceClass;
    public String lastSourceMethod;
    public String lastMsg;

    public void addFileHandler(String file) {
        ++addHandlerCallCount;
        this.file = file;
    }

    public void logp(Level level, String sourceClass, String sourceMethod, String msg) {
        ++logpCallCount;
        lastLevel = level;
        lastSourceClass = sourceClass;
        lastSourceMethod = sourceMethod;
        lastMsg = msg;
    }
}
