package tempest.services;

import static org.junit.Assert.*;

import org.junit.Test;

import tempest.interfaces.Logger;
import tempest.mocks.MockExecutor;
import tempest.mocks.MockLogWrapper;

import java.io.IOException;
import java.net.Inet4Address;
import java.util.logging.Level;

public class LoggerTest {
   @Test
    public void loggerConstructorSetsFileHandler() throws IOException {
        MockLogWrapper logWrapper = new MockLogWrapper();
        Logger logger = new DefaultLogger(new MockExecutor(), logWrapper);
        assertEquals(1, logWrapper.addHandlerCallCount);
        assertEquals(logger.getLogFile(), logWrapper.file);
    }

    @Test
    public void logLineCorrectSourceClassAndSourceMethod() throws IOException {
        MockLogWrapper logWrapper = new MockLogWrapper();
        DefaultLogger logger = new DefaultLogger(new MockExecutor(), logWrapper);
        logger.logLine(DefaultLogger.INFO, "foo bar");
        assertEquals(1, logWrapper.logpCallCount);
        assertEquals("tempest.services.LoggerTest", logWrapper.lastSourceClass);
        assertEquals("logLineCorrectSourceClassAndSourceMethod", logWrapper.lastSourceMethod);
    }

    @Test
    public void logLineSevere() throws IOException {
        MockLogWrapper logWrapper = new MockLogWrapper();
        DefaultLogger logger = new DefaultLogger(new MockExecutor(), logWrapper);
        logger.logLine(DefaultLogger.SEVERE, "foo bar");
        assertEquals(1, logWrapper.logpCallCount);
        assertEquals(Level.SEVERE, logWrapper.lastLevel);
    }

    @Test
    public void logLineWarning() throws IOException {
        MockLogWrapper logWrapper = new MockLogWrapper();
        DefaultLogger logger = new DefaultLogger(new MockExecutor(), logWrapper);
        logger.logLine(DefaultLogger.WARNING, "foo bar");
        assertEquals(1, logWrapper.logpCallCount);
        assertEquals(Level.WARNING, logWrapper.lastLevel);
    }

    @Test
    public void logLineInfo() throws IOException {
        MockLogWrapper logWrapper = new MockLogWrapper();
        DefaultLogger logger = new DefaultLogger(new MockExecutor(), logWrapper);
        logger.logLine(DefaultLogger.INFO, "foo bar");
        assertEquals(1, logWrapper.logpCallCount);
        assertEquals(Level.INFO, logWrapper.lastLevel);
    }

    @Test
    public void getLogFileSetCorrectlyDefault() throws IOException {
        DefaultLogger logger = new DefaultLogger(new MockExecutor(), new MockLogWrapper());
        assertEquals("machine." + Inet4Address.getLocalHost().getHostName() + ".log", logger.getLogFile());
    }

    @Test
    public void getGrepFileSetCorrectlyDefault() throws IOException {
        DefaultLogger logger = new DefaultLogger(new MockExecutor(), new MockLogWrapper());
        assertEquals("machine." + Inet4Address.getLocalHost().getHostName() + ".log", logger.getGrepFile());
    }

    @Test
    public void getLogFileAndGrepFileSetCorrectly() throws IOException {
        DefaultLogger logger = new DefaultLogger(new MockExecutor(), new MockLogWrapper(), "logFile.log", "grepFile.log");
        assertEquals("logFile.log", logger.getLogFile());
        assertEquals("grepFile.log", logger.getGrepFile());
    }

    @Test
    public void grepDelegatesExecutor() throws IOException {
        MockExecutor executor = new MockExecutor();
        DefaultLogger logger = new DefaultLogger(executor, new MockLogWrapper());
        logger.grep("foo");
        assertEquals(1, executor.execCallCount);
    }

    @Test
    public void grepBuildsCorrectCommand() throws IOException {
        MockExecutor executor = new MockExecutor();
        DefaultLogger logger = new DefaultLogger(executor, new MockLogWrapper());
        logger.grep("foo");
        assertEquals("grep", executor.command);
        assertEquals("foo machine." + Inet4Address.getLocalHost().getHostName() + ".log", executor.options);
    }

    @Test
    public void grepConcatResults() throws IOException {
        MockExecutor executor = new MockExecutor();
        executor.result = new String[]{"foo bar", "foolicious"};
        DefaultLogger logger = new DefaultLogger(executor, new MockLogWrapper());
        String expectedResult = "foo bar" + System.lineSeparator()
                + "foolicious" + System.lineSeparator();
        assertEquals(expectedResult, logger.grep("foo"));
    }
}
