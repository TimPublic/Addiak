package dev.timkloepper.server;


import dev.timkloepper.util.PrintColors;

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
        server.p_attachInitialAttachments();

        ServerManager.log(id, PrintColors.GREEN + "Created!" + PrintColors.RESET);

        return server;
    }
    protected static boolean p_close(int id) {
        ServerInfo info;

        info = _INFOS.get(id);
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
            for (I_Attachment attachment : info.ATTACHMENTS) attachment.detach();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (info.port != -1) log(id, PrintColors.YELLOW + "Booted down from port " + PrintColors.UNDERLINE + PrintColors.WHITE + info.port + PrintColors.RESET + PrintColors.YELLOW + "!" + PrintColors.RESET);
        log(id, PrintColors.YELLOW + "Closed!" + PrintColors.RESET);

        _INFOS.remove(id);

        if (!_FREE_IDS.contains(id)) _FREE_IDS.add(id);

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
    protected static void p_addAttachment(int id, I_Attachment attachment) {
        ServerInfo info;

        info = _INFOS.get(id);
        if (info == null) return;

        info.ATTACHMENTS.add(attachment);
    }
    protected static void p_rmvAttachment(int id, I_Attachment attachment) {
        ServerInfo info;

        info = _INFOS.get(id);
        if (info == null) return;

        info.ATTACHMENTS.remove(attachment);
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


    // -+- LOGGING LOGIC -+- //

    public static boolean log(int id, String message) {
        if (!_INFOS.containsKey(id)) return log(message);

        System.out.println(PrintColors.PURPLE + "[SERVER MANAGER]" + PrintColors.CYAN + "[SERVER::" + id + "]" + PrintColors.RESET + " " + message);

        return true;
    }
    public static boolean log(int id, String[] tags, String message) {
        StringBuilder tagsString;

        if (!_INFOS.containsKey(id)) return log(tags, message);

        tagsString = new StringBuilder();

        for (String tag : tags) tagsString.append("[").append(tag).append("]");

        System.out.println(PrintColors.PURPLE + "[SERVER MANAGER]" + PrintColors.CYAN + "[SERVER::" + id + "]" + PrintColors.RESET + tagsString + " " + message);

        return true;
    }
    public static boolean log(String message) {
        System.out.println(PrintColors.PURPLE + "[SERVER MANAGER]" + PrintColors.CYAN + "[SERVER::UNKNOWN]" + PrintColors.RESET + " " + message);

        return true;
    }
    public static boolean log(String[] tags, String message) {
        StringBuilder tagsString;

        tagsString = new StringBuilder();

        for (String tag : tags) tagsString.append("[").append(tag).append("]");

        System.out.println(PrintColors.PURPLE + "[SERVER MANAGER]" + PrintColors.CYAN + "[SERVER::UNKNOWN]" + PrintColors.RESET + tagsString + " " + message);

        return true;
    }


}


class ServerInfo {


    // -+- CREATION -+- //

    public ServerInfo(WeakReference<Server> serverRef, Selector selector) {
        SERVER_REFERENCE = serverRef;
        SELECTOR = selector;

        ATTACHMENTS = new ConcurrentLinkedDeque<>();
    }


    // -+- PARAMETERS -+- //

    // NON-FINALS //

    public ServerSocketChannel socket;
    public int port;

    // FINALS //

    public final WeakReference<Server> SERVER_REFERENCE;
    public final Selector SELECTOR;

    public final ConcurrentLinkedDeque<I_Attachment> ATTACHMENTS;


}