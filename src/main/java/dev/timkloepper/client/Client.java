package dev.timkloepper.client;


import dev.timkloepper.util.PrintColors;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;


public class Client {


    // -+- CREATION -+- //

    public Client() {
        _currentPort = -1;
    }


    // -+- PARAMETERS -+- //

    // NON-FINALS //

    private int _currentPort;
    private int _currentClientId;

    private SocketChannel _socket;


    // -+- CONNECTION LOGIC -+- //

    public boolean connect(int port) {
        if (isConnected()) return false;

        try {
            _socket = SocketChannel.open();
            _socket.connect(new InetSocketAddress(port));
        } catch (IOException e) {
            return false;
        }

        _currentPort = port;

        log(PrintColors.GREEN + "Connected to port " + PrintColors.UNDERLINE + PrintColors.WHITE + _currentPort + PrintColors.RESET);

        return true;
    }
    public boolean disconnect() {
        if (!isConnected()) return false;

        try {
            _socket.close();
        } catch (IOException e) {
            return false;
        }

        _socket = null;
        _currentPort = -1;

        return true;
    }

    public boolean isConnected() {
        return _currentPort != -1;
    }


    // -+- LOGGING LOGIC -+- //

    public void log(String message) {
        try {
            System.out.println(PrintColors.ORANGE + "[CLIENT::" + PrintColors.UNDERLINE + PrintColors.WHITE + _socket.getLocalAddress().toString().subSequence(1, _socket.getLocalAddress().toString().length()) + PrintColors.RESET + PrintColors.ORANGE + "]" + PrintColors.RESET + " " + message);
        } catch (IOException e) {
            System.out.println(PrintColors.ORANGE + "[CLIENT::" + PrintColors.RED + "UNKNOWN" + PrintColors.RESET + "]" + PrintColors.RESET + " " + message);
        }
    }


}