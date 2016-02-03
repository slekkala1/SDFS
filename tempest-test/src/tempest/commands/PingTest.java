package tempest.commands;

import static org.junit.Assert.*;
import org.junit.Test;

import tempest.commands.command.Grep;
import tempest.commands.command.Ping;
import tempest.commands.handler.PingHandler;
import tempest.protos.Command;

public class PingTest {
    @Test
    public void PingRequest() {
        Object request = new Object();
        Ping ping = new Ping();
        ping.setRequest(request);
        assertEquals(request, ping.getRequest());
    }

    @Test
    public void PingResponse() {
        String response = "Hello";
        Ping ping = new Ping();
        ping.setResponse(response);
        assertEquals(response, ping.getResponse());
    }

    @Test
    public void PingHandlerSerialize() {
        Object request = new Object();
        String response = "Hello";
        Ping ping = new Ping();
        ping.setRequest(request);
        ping.setResponse(response);
        PingHandler pingHandler = new PingHandler();
        Command.Message message = pingHandler.serialize(ping);
        assertEquals(Command.Message.Type.PING, message.getType());
        assertEquals("Hello", message.getPing().getResponse());
    }

    @Test
    public void PingHandlerDeserialize() {
        Command.Message message = Command.Message.newBuilder()
                .setType(Command.Message.Type.PING)
                .setPing(Command.Ping.newBuilder()
                        .setResponse("Hello"))
                .build();
        PingHandler pingHandler = new PingHandler();
        Ping ping = pingHandler.deserialize(message);
        assertEquals(null, ping.getRequest());
        assertEquals("Hello", ping.getResponse());
    }

    @Test
    public void PingHandlerCanHandle() {
        PingHandler pingHandler = new PingHandler();
        assertTrue(pingHandler.canHandle(new Ping().getType()));
        assertFalse(pingHandler.canHandle(new Grep().getType()));
    }

    @Test
    public void PingHandlerExecute() {
        PingHandler pingHandler = new PingHandler();
        assertEquals("Hello", pingHandler.execute(null));
        assertEquals("Hello", pingHandler.execute("always returns Hello"));
    }

    @Test
    public void PingAdd() {
        String response1 = "Response 1";
        String response2 = "Response 2";
        String expectedResult = response1 + System.lineSeparator() + response2;
        Ping ping = new Ping();
        assertEquals(expectedResult, ping.add(response1, response2));
    }
}
