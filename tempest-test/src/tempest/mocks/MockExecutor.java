package tempest.mocks;

import tempest.interfaces.Executor;

public class MockExecutor implements Executor {
    public int execCallCount;
    public String command;
    public String options;
    public String[] result = new String[0];

    public String[] execute(String command, String options) {
        ++execCallCount;
        this.command = command;
        this.options = options;
        return result;
    }
}
