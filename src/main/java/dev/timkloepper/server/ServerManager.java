package dev.timkloepper.server;


import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;


public class ServerManager {


    // -+- CREATION -+- //

    private ServerManager() {

    }


    // -+- PARAMETERS -+- //

    // NON-FINALS //

    private static int _nextId = 0;

    // FINALS //

    private static final ConcurrentHashMap<Integer, ServerInfo> _INFOS = new ConcurrentHashMap<>();

    private static final ConcurrentLinkedDeque<Integer> _FREE_IDS = new ConcurrentLinkedDeque<>();


    // -+- SERVER MANAGEMENT -+- //

    protected static Server p_create() {
        int id;
        Server server;
        ServerInfo info;

        id = h_getId();

        server = new Server();

        info = new ServerInfo(new WeakReference<>(server), server.p_SELECTOR);
        _INFOS.put(id, info);

        server.p_initUpdateLoop(id);

        return server;
    }
    protected static boolean p_close(int id) {
        ServerInfo info;

        info = _INFOS.remove(id);
        if (info == null) return false;

        try {
            Selector selector;
            ServerSocketChannel socket;

            selector = info.SELECTOR;
            if (selector != null) {
                selector.wakeup();
                selector.close();
            }
            socket = info.socket;
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (info.port != -1) System.out.println("[SERVER (id:" + id + ")] Booted down from port " + info.port + "!");

        if (!_FREE_IDS.contains(id)) _FREE_IDS.add(id);

        System.out.println("[SERVER (id:" + id + ")] ! CLOSED  !");

        return true;
    }

    protected static void p_updateSocket(int id, ServerSocketChannel socket) {
        ServerInfo info;

        info = _INFOS.get(id);
        if (info == null) return;

        info.socket = socket;
    }
    protected static void p_updatePort(int id, int port) {
        ServerInfo info;

        info = _INFOS.get(id);
        if (info == null) return;

        info.port = port;
    }

    protected static boolean p_isAlive(int id) {
        ServerInfo info;

        info = _INFOS.get(id);
        if (info == null) return false;

        return info.SERVER_REFERENCE.get() != null;
    }

    private static int h_getId() {
        if (_FREE_IDS.isEmpty()) return _nextId++;

        return _FREE_IDS.poll();
    }


}


class ServerInfo {


    // -+- CREATION -+- //

    public ServerInfo(WeakReference<Server> serverRef, Selector selector) {
        SERVER_REFERENCE = serverRef;
        SELECTOR = selector;
    }


    // -+- PARAMETERS -+- //

    // NON-FINALS //

    public ServerSocketChannel socket;
    public int port;

    // FINALS //

    public final WeakReference<Server> SERVER_REFERENCE;
    public final Selector SELECTOR;


}