package dev.timkloepper.server.services;


import dev.timkloepper.server.Server;
import dev.timkloepper.server.ServerManager;
import dev.timkloepper.util.PrintColors;

import java.awt.*;
import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;


public class Acceptor implements I_Service {


    // -+- CREATION -+- //

    public Acceptor() {
        _isAttached = false;

        _socket = null;
        _serverId = -1;
    }


    // -+- PARAMETERS -+- //

    // NON-FINALS //

    private boolean _isAttached;

    private ServerSocketChannel _socket;
    private int _serverId;


    // -+- ATTACHMENT LOGIC -+- //

    @Override
    public void attach(Server server) {
        _isAttached = true;

        _serverId = server.getId();

        ServerManager.log(_serverId, new String[] {"ACCEPTOR"}, PrintColors.GREEN + "Attached!" + PrintColors.RESET);
    }
    @Override
    public void detach() {
        deactivate(null);

        ServerManager.log(_serverId, new String[] {"ACCEPTOR"}, PrintColors.YELLOW + "Detached!" + PrintColors.RESET);

        _isAttached = false;

        _socket = null;
        _serverId = -1;

    }


    // -+- SERVICE LOGIC -+- //

    @Override
    public void activate(Server server) {
        _socket = server.getSocket();

        ServerManager.log(_serverId, new String[] {"ACCEPTOR"}, PrintColors.GREEN + "Activated!" + PrintColors.RESET);

        new Thread(() -> {
            while (_socket != null) {
                SocketChannel client;

                try {
                    client = _socket.accept();

                    ServerManager.log(_serverId, new String[] {"ACCEPTOR"}, PrintColors.GREEN + "Client (" + PrintColors.UNDERLINE + PrintColors.WHITE + client.getLocalAddress().toString().subSequence(1, client.getLocalAddress().toString().length()) + PrintColors.RESET + PrintColors.GREEN + ") has connected!" + PrintColors.RESET);
                } catch (IOException e) {
                    break;
                }

            }
        }).start();
    }
    @Override
    public void deactivate(Server server) {
        ServerManager.log(_serverId, new String[] {"ACCEPTOR"}, PrintColors.YELLOW + "Deactivated!" + PrintColors.RESET);

        _socket = null;
    }


}