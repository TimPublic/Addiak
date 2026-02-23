package dev.timkloepper.server;


import dev.timkloepper.server.logicals.I_Logical;
import dev.timkloepper.server.services.I_Service;

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

        _closed = false;

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
                if (server._closed) {
                    server = null;

                    break;
                }

                if (!server.isRunning()) {
                    server = null;

                    continue;
                }

                for (I_Logical logical : server._LOGICALS) logical.update();
            }

            ServerManager.p_close(ID);

        }).start();
    }

    public static Server create() {
        Server server;

        server = ServerManager.p_create();

        System.out.println("[SERVER (id:" + server._managementId + ")] ! CREATED !");

        return server;
    }
    public static Server create(int port) {
        Server server;

        server = Server.create();
        if (!server.up(port)) server = null;

        return server;
    }

    public void close() {
        down();
        ServerManager.p_close(_managementId);

        _closed = true;
    }


    // -+- PARAMETERS -+- //

    // NON-FINALS //

    private int _currentPort;
    protected ServerSocketChannel _socket;

    private boolean _closed;

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

        ServerManager.p_updateSocket(_managementId, _socket);
        ServerManager.p_updatePort(_managementId, _currentPort);

        h_activateServices();
        h_activateLogicals();

        System.out.println("[SERVER (id:" + _managementId + ")] Booted up on port " + _currentPort + "!");

        return true;
    }
    public boolean down() {
        int prevPort;

        if (!isRunning()) return false;

        try {
            _socket.close();
            p_SELECTOR.wakeup();
        } catch (IOException e) {
            return false;
        }

        prevPort = _currentPort;
        _currentPort = -1;

        ServerManager.p_updateSocket(_managementId, _socket);
        ServerManager.p_updatePort(_managementId, _currentPort);

        h_deactivateServices();
        h_deactivateLogicals();

        System.out.println("[SERVER (id:" + _managementId + ")] Booted down from port " + prevPort + "!");

        return true;
    }

    public boolean isRunning() {
        return _currentPort != -1;
    }

    public int getPort() {
        return _currentPort;
    }


    // -+- ATTACHMENT LOGIC -+- //

    public boolean attach(I_Service service) {
        if (service == null) return false;
        if (_SERVICES.contains(service)) return false;

        service.attach(this);

        return _SERVICES.add(service); // Always returns true (look in the method description).
    }
    public boolean attach(I_Logical logical) {
        if (logical == null) return false;
        if (_LOGICALS.contains(logical)) return false;

        logical.attach(this);

        return _LOGICALS.add(logical); // Always returns true (look in the method description).
    }

    public boolean detach(I_Service service) {
        if (service == null) return false;

        service.deactivate(this);
        service.detach(this);

        return _SERVICES.remove(service);
    }
    public boolean detach(I_Logical logical) {
        if (logical == null) return false;

        logical.deactivate(this);
        logical.detach(this);

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