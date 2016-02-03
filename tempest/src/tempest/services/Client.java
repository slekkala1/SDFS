package tempest.services;

import tempest.commands.Response;
import tempest.commands.command.Grep;
import tempest.commands.command.Introduce;
import tempest.commands.command.Leave;
import tempest.commands.command.Ping;
import tempest.commands.interfaces.*;
import tempest.interfaces.*;
import tempest.networking.TcpClientResponseCommandExecutor;
import tempest.networking.UdpClientCommandExecutor;
import tempest.networking.UdpClientResponseCommandExecutor;
import tempest.protos.Membership;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class Client {
    private static ExecutorService pool = Executors.newFixedThreadPool(7);
    private final MembershipService membershipService;
    private final Logger logger;
    private final CommandExecutor[] commandHandlers;
    private final ResponseCommandExecutor[] responseCommandHandlers;

    public Client(MembershipService membershipService, Logger logger, CommandExecutor[] commandHandlers, ResponseCommandExecutor[] responseCommandHandlers) {
        this.membershipService = membershipService;
        this.logger = logger;
        this.commandHandlers = commandHandlers;
        this.responseCommandHandlers = responseCommandHandlers;
    }

    public Response grep(Membership.Member member, String options) {
        Grep grep = new Grep();
        grep.setRequest(options);
        return createResponseExecutor(member, grep).execute();
    }

    public Response grepAll(String options) {
        Grep grep = new Grep();
        grep.setRequest(options);
        return executeAllParallel(grep, true);
    }

    public Response introduce(Membership.Member member, Membership.Member localMember) {
        Introduce introduce = new Introduce();
        introduce.setRequest(localMember);
        return createResponseExecutor(member, introduce).execute();
    }

    public Response leave(Membership.Member localMember) {
        Leave leave = new Leave();
        leave.setRequest(localMember);
        return executeAllParallel(leave, false);
    }

    public Response list(String sDFSFileName) {
        tempest.commands.command.List list = new tempest.commands.command.List();
        list.setRequest(sDFSFileName);
        return executeAllParallel(list, true);
    }

    public void membership(Membership.Member member, Membership.MembershipList membershipList) {
        tempest.commands.command.Membership membership = new tempest.commands.command.Membership();
        membership.setRequest(membershipList);
        createExecutor(member, membership).execute();
    }

    public Response ping(Membership.Member member) {
        return createResponseExecutor(member, new Ping()).execute();
    }

    public Response pingAll() {
        return executeAllParallel(new Ping(), false);
    }

    private <TRequest, TResponse> Response<TResponse> executeAllParallel(ResponseCommand<TRequest, TResponse> responseCommand, boolean includeLocal) {
        Collection<Callable<Response<TResponse>>> commandExecutors = new ArrayList<>();
        if (includeLocal) {
            for (Membership.Member machine : membershipService.getMembershipList().getMemberList()) {
                commandExecutors.add(createResponseExecutor(machine, responseCommand));
            }
        } else {
            for (Membership.Member machine : membershipService.getMembershipListNoLocal().getMemberList()) {
                commandExecutors.add(createResponseExecutor(machine, responseCommand));
            }
        }
        List<Future<Response<TResponse>>> results;
        try {
            results = pool.invokeAll(commandExecutors);
            Response<TResponse> response = null;
            for (Future<Response<TResponse>> future : results) {
                try {
                    if (response == null)
                        response = future.get();
                    else {
                        Response<TResponse> tResponse = future.get();
                        if (tResponse != null) {
                            response.setResponseData(response.getResponseData().add(tResponse.getResponseData()));
                            response.setResponse(responseCommand.add(response.getResponse(), tResponse.getResponse()));
                        }
                    }
                } catch (ExecutionException e) {
                    logger.logLine(DefaultLogger.SEVERE, String.valueOf(e));
                }
            }
            return response;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private <TRequest> ClientCommandExecutor createExecutor(Membership.Member member, Command<TRequest> command) {
        CommandExecutor commandHandler = null;
        for (CommandExecutor ch : commandHandlers) {
            if (ch.canHandle(command.getType()))
                commandHandler = ch;
        }
        return new UdpClientCommandExecutor<>(member, command, commandHandler, logger);
    }

    private <TRequest, TResponse> ClientResponseCommandExecutor<TResponse> createResponseExecutor(Membership.Member member, ResponseCommand<TRequest, TResponse> command) {
        ResponseCommandExecutor commandHandler = null;
        for (ResponseCommandExecutor ch : responseCommandHandlers) {
            if (ch.canHandle(command.getType()))
                commandHandler = ch;
        }
        if (command instanceof Udp)
            return new UdpClientResponseCommandExecutor<>(member, command, commandHandler, logger);
        return new TcpClientResponseCommandExecutor<>(member, command, commandHandler, logger);
    }
}
