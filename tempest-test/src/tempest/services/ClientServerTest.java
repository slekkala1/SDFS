package tempest.services;

public class ClientServerTest {
    private final String log1 = "the";
    private final String log2 = "quick";
    private final String log3 = "brown";
    private final String log4 = "fox";
    private final String log5 = "jumped";
    private final String log6 = "over";
    private final String log7 = "lazy";
    private final String logFull = log1 + System.lineSeparator()
            + log2 + System.lineSeparator()
            + log3 + System.lineSeparator()
            + log4 + System.lineSeparator()
            + log5 + System.lineSeparator()
            + log6 + System.lineSeparator()
            + log7;

//    @Test
//    public void distributedPingAll() throws IOException {
//        String hostTemplate = Inet4Address.getLocalHost().getHostName() + ":";
//        MembershipService membershipService = new MembershipService(new String[] {
//                hostTemplate + 5541,
//                hostTemplate + 5542,
//                hostTemplate + 5543,
//                hostTemplate + 5544,
//                hostTemplate + 5545,
//                hostTemplate + 5546,
//                hostTemplate + 5547
//        }, 5541);
//        Logger logger = new MockLogger();
//        CommandHandler[] commandHandlers = new CommandHandler[] { new PingHandler(), new GrepHandler(logger)};
//        Server server1 = new Server(logger, 5541, commandHandlers);
//        Server server2 = new Server(logger, 5542, commandHandlers);
//        Server server3 = new Server(logger, 5543, commandHandlers);
//        Server server4 = new Server(logger, 5544, commandHandlers);
//        Server server5 = new Server(logger, 5545, commandHandlers);
//        Server server6 = new Server(logger, 5546, commandHandlers);
//        Server server7 = new Server(logger, 5547, commandHandlers);
//
//        server1.start();
//        server2.start();
//        server3.start();
//        server4.start();
//        server5.start();
//        server6.start();
//        server7.start();
//
//        Client client = new Client(membershipService, logger, commandHandlers);
//        Response<String> response = client.pingAll();
//
//        assertTrue(5 <= response.getResponseData().getLineCount());
//
//        server1.stop();
//        server2.stop();
//        server3.stop();
//        server4.stop();
//        server5.stop();
//        server6.stop();
//        server7.stop();
//    }
//
//    @Test
//    public void distributedGrepAll() throws IOException {
//        String hostTemplate = Inet4Address.getLocalHost().getHostName() + ":";
//        MembershipService membershipService = new MembershipService(new String[] {
//                hostTemplate + 5441,
//                hostTemplate + 5442,
//                hostTemplate + 5443,
//                hostTemplate + 5444,
//                hostTemplate + 5445,
//                hostTemplate + 5446,
//                hostTemplate + 5447
//        }, 5441);
//        MockLogger logger1 = new MockLogger();
//        logger1.grep = log1;
//        CommandHandler[] commandHandlers1 = new CommandHandler[] { new PingHandler(), new GrepHandler(logger1)};
//        Server server1 = new Server(logger1, 5441, commandHandlers1);
//        MockLogger logger2 = new MockLogger();
//        logger2.grep = log2;
//        Server server2 = new Server(logger2, 5442, new CommandHandler[] { new PingHandler(), new GrepHandler(logger2)});
//        MockLogger logger3 = new MockLogger();
//        logger3.grep = log3;
//        Server server3 = new Server(logger3, 5443, new CommandHandler[] { new PingHandler(), new GrepHandler(logger3)});
//        MockLogger logger4 = new MockLogger();
//        logger4.grep = log4;
//        Server server4 = new Server(logger4, 5444, new CommandHandler[] { new PingHandler(), new GrepHandler(logger4)});
//        MockLogger logger5 = new MockLogger();
//        logger5.grep = log5;
//        Server server5 = new Server(logger5, 5445, new CommandHandler[] { new PingHandler(), new GrepHandler(logger5)});
//        MockLogger logger6 = new MockLogger();
//        logger6.grep = log6;
//        Server server6 = new Server(logger6, 5446, new CommandHandler[] { new PingHandler(), new GrepHandler(logger6)});
//        MockLogger logger7 = new MockLogger();
//        logger7.grep = log7;
//        Server server7 = new Server(logger7, 5447, new CommandHandler[] { new PingHandler(), new GrepHandler(logger7)});
//
//        MembershipService membershipServiceFull = new MembershipService(new String[] { hostTemplate + 5448 }, 5448);
//        MockLogger loggerFull = new MockLogger();
//        loggerFull.grep = logFull;
//        CommandHandler[] commandHandlersFull = new CommandHandler[] { new PingHandler(), new GrepHandler(loggerFull)};
//        Server serverFull = new Server(loggerFull, 5448, commandHandlersFull);
//
//        server1.start();
//        server2.start();
//        server3.start();
//        server4.start();
//        server5.start();
//        server6.start();
//        server7.start();
//
//        serverFull.start();
//
//        Client client = new Client(membershipService, logger1, commandHandlers1);
//        Response<String> response = client.grepAll("catalog");
//
//        Client clientFull = new Client(membershipServiceFull, loggerFull, commandHandlersFull);
//        Response<String> responseFull = clientFull.grep(membershipServiceFull.getLocalMember(), "catalog");
//
//        assertEquals(responseFull.getResponseData().getLineCount(), response.getResponseData().getLineCount());
//
//        server1.stop();
//        server2.stop();
//        server3.stop();
//        server4.stop();
//        server5.stop();
//        server6.stop();
//        server7.stop();
//        serverFull.stop();
//    }
}
