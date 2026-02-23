package dev.timkloepper.server;


import dev.timkloepper.server.logicals.I_Logical;
import dev.timkloepper.server.services.Acceptor;
import dev.timkloepper.server.services.I_Service;
import dev.timkloepper.util.PrintColors;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;


public class Server {


    // -+- CREATION -+- //

    protected Server() {
        _currentPort = -1;

        try {
            p_SELECTOR = Selector.open();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        _SERVICES = new ConcurrentLinkedDeque<>();
        _LOGICALS = new ConcurrentLinkedDeque<>();

        _PURPOSES = new ConcurrentHashMap<>();
    }

    protected void p_initUpdateLoop(final int ID) {
        _managementId = ID; // Do not make the update thread reference this management id, as it would p_create a strong reference. Always let it reference the originally passed id.

        final WeakReference<Server> REFERENCE = new WeakReference<>(this);

        new Thread(() -> {
            Server server;

            server = null;

            while (ServerManager.p_isAlive(ID)) {
                server = null;

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                server = REFERENCE.get();
                if (server == null) break;

                if (!server.isRunning()) {
                    server = null;

                    continue;
                }

                for (I_Logical logical : server._LOGICALS) logical.update();
            }

            ServerManager.p_close(ID);

        }).start();
    }
    protected void p_attachInitialAttachments() {
        attach(new Acceptor());
    }

    public static Server create() {
        Server server;

        server = ServerManager.p_create();

        return server;
    }
    public static Server create(int port) {
        Server server;

        server = Server.create();
        if (!server.up(port)) server = null;

        return server;
    }

    public void close() {
        ServerManager.p_close(_managementId);
    }


    // -+- PARAMETERS -+- //

    // NON-FINALS //

    private int _currentPort;
    protected ServerSocketChannel _socket;

    private int _managementId;

    // FINALS //

    protected final Selector p_SELECTOR;

    private final ConcurrentLinkedDeque<I_Service> _SERVICES;
    private final ConcurrentLinkedDeque<I_Logical> _LOGICALS;

    private final ConcurrentHashMap<I_Purpose.PURPOSE, I_Purpose> _PURPOSES;


    // -+- BOOT LOGIC -+- //

    public boolean up(int port) {
        if (port < 0) return false;
        if (isRunning()) return false;

        try {
            _socket = ServerSocketChannel.open();
            _socket.bind(new InetSocketAddress(port));
            p_SELECTOR.wakeup();
        } catch (IOException e) {
            return false;
        }

        _currentPort = port;

        ServerManager.log(_managementId, PrintColors.GREEN + "Booted up on port " + PrintColors.UNDERLINE + PrintColors.WHITE + _currentPort + PrintColors.RESET + PrintColors.GREEN + "!" + PrintColors.RESET);

        ServerManager.p_updateSocket(_managementId, _socket);
        ServerManager.p_updatePort(_managementId, _currentPort);

        h_activateServices();
        h_activateLogicals();

        return true;
    }
    public boolean down() {
        if (!isRunning()) return false;

        try {
            _socket.close();
            p_SELECTOR.wakeup();
        } catch (IOException e) {
            return false;
        }

        ServerManager.log(_managementId, PrintColors.YELLOW + "Booted down from port " + PrintColors.UNDERLINE + PrintColors.WHITE + _currentPort + PrintColors.RESET + PrintColors.YELLOW + "!" + PrintColors.RESET);

        _currentPort = -1;

        ServerManager.p_updateSocket(_managementId, _socket);
        ServerManager.p_updatePort(_managementId, _currentPort);

        h_deactivateServices();
        h_deactivateLogicals();

        return true;
    }

    public boolean isRunning() {
        return _currentPort != -1;
    }

    public int getPort() {
        return _currentPort;
    }
    public ServerSocketChannel getSocket() {
        return _socket;
    }
    public int getId() {
        return _managementId;
    }


    // -+- ATTACHMENT LOGIC -+- //

    public boolean attach(I_Service service) {
        if (service == null) return false;
        if (_SERVICES.contains(service)) return false;

        service.attach(this);

        ServerManager.p_addAttachment(_managementId, service);

        return _SERVICES.add(service); // Always returns true (look in the method description).
    }
    public boolean attach(I_Logical logical) {
        if (logical == null) return false;
        if (_LOGICALS.contains(logical)) return false;

        logical.attach(this);

        ServerManager.p_addAttachment(_managementId, logical);

        return _LOGICALS.add(logical); // Always returns true (look in the method description).
    }

    public boolean detach(I_Service service) {
        if (service == null) return false;

        service.deactivate(this);
        service.detach();

        ServerManager.p_rmvAttachment(_managementId, service);

        return _SERVICES.remove(service);
    }
    public boolean detach(I_Logical logical) {
        if (logical == null) return false;

        logical.deactivate(this);
        logical.detach();

        ServerManager.p_rmvAttachment(_managementId, logical);

        return _LOGICALS.remove(logical);
    }

    public HashSet<I_Service> getServices() {
        return new HashSet<>(_SERVICES);
    }
    public HashSet<I_Logical> getLogicals() {
        return new HashSet<>(_LOGICALS);
    }

    private void h_activateServices() {
        for (I_Service service : _SERVICES) service.activate(this);
    }
    private void h_activateLogicals() {
        for (I_Logical logical : _LOGICALS) logical.activate(this);
    }

    private void h_deactivateServices() {
        for (I_Service service : _SERVICES) service.deactivate(this);
    }
    private void h_deactivateLogicals() {
        for (I_Logical logical : _LOGICALS) logical.deactivate(this);
    }


    // -+- PURPOSE LOGIC -+- //

    public boolean addPurpose(I_Purpose.PURPOSE purpose, I_Purpose purposeObject, boolean overwrite) {
        if (purposeObject == null) return false;
        if (_PURPOSES.containsKey(purpose) && !overwrite) return false;

        _PURPOSES.put(purpose, purposeObject);

        return true;
    }
    public boolean rmvPurpose(I_Purpose purposeObject) {
        I_Purpose.PURPOSE purpose;

        if (purposeObject == null) return false;

        purpose = purposeObject.getPurpose();

        if (_PURPOSES.get(purpose) != purposeObject) return false;

        _PURPOSES.remove(purpose);

        return true;
    }
    public I_Purpose rmvPurpose(I_Purpose.PURPOSE purpose) {
        return _PURPOSES.remove(purpose);
    }

    public I_Purpose getPurpose(I_Purpose.PURPOSE purpose) {
        return _PURPOSES.get(purpose);
    }

    public boolean hasPurpose(I_Purpose.PURPOSE purpose) {
        return _PURPOSES.containsKey(purpose);
    }
    public boolean hasPurpose(I_Purpose.PURPOSE purpose, I_Purpose purposeObject) {
        return _PURPOSES.get(purpose) == purposeObject;
    }


}