package tempest.commands;

import static org.junit.Assert.*;
import org.junit.Test;

import tempest.commands.command.Grep;
import tempest.commands.command.Ping;
import tempest.commands.handler.GrepHandler;
import tempest.mocks.MockLogger;
import tempest.protos.Command;

public class GrepTest {
    @Test
    public void GrepRequest() {
        String request = "dogs";
        Grep Grep = new Grep();
        Grep.setRequest(request);
        assertEquals(request, Grep.getRequest());
    }

    @Test
    public void GrepResponse() {
        String response = "dogs love trucks";
        Grep Grep = new Grep();
        Grep.setResponse(response);
        assertEquals(response, Grep.getResponse());
    }

    @Test
    public void GrepHandlerSerialize() {
        String request = "dogs";
        String response = "dogs love trucks";
        Grep grep = new Grep();
        grep.setRequest(request);
        grep.setResponse(response);
        GrepHandler grepHandler = new GrepHandler(new MockLogger());
        Command.Message message = grepHandler.serialize(grep);
        assertEquals(Command.Message.Type.GREP, message.getType());
        assertEquals("dogs", message.getGrep().getRequest());
        assertEquals("dogs love trucks", message.getGrep().getResponse());
    }

    @Test
    public void GrepHandlerDeserialize() {
        Command.Message message = Command.Message.newBuilder()
                .setType(Command.Message.Type.GREP)
                .setGrep(Command.Grep.newBuilder()
                        .setRequest("cats")
                        .setResponse("cats hate water"))
                .build();
        GrepHandler GrepHandler = new GrepHandler(new MockLogger());
        Grep Grep = GrepHandler.deserialize(message);
        assertEquals("cats", Grep.getRequest());
        assertEquals("cats hate water", Grep.getResponse());
    }

    @Test
    public void GrepHandlerCanHandle() {
        GrepHandler GrepHandler = new GrepHandler(new MockLogger());
        assertTrue(GrepHandler.canHandle(new Grep().getType()));
        assertFalse(GrepHandler.canHandle(new Ping().getType()));
    }

    @Test
    public void GrepHandlerExecute() {
        MockLogger mockLogger = new MockLogger();
        mockLogger.grep = "dogs love trucks";
        GrepHandler GrepHandler = new GrepHandler(mockLogger);
        assertEquals(mockLogger.grep, GrepHandler.execute("dogs"));
    }

    @Test
    public void GrepAdd() {
        String response1 = "Response 1";
        String response2 = "Response 2";
        String expectedResult = response1 + System.lineSeparator() + response2;
        Grep Grep = new Grep();
        assertEquals(expectedResult, Grep.add(response1, response2));
    }
}
