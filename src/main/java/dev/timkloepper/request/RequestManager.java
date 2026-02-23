package dev.timkloepper.request;


import dev.timkloepper.Addiak;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;


public class RequestManager {


    // -+- PARAMETERS -+- //

    // NON-FINALS //

    private static int _nextFreeId = 0;

    // FINALS //

    private static final ConcurrentHashMap<Integer, RequestInfo> _INFOS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<? extends A_Request>, Integer> _TYPE_IDS = new ConcurrentHashMap<>();



    // -+- REQUEST MANAGEMENT -+- //

    public static ByteBuffer encode(A_Request request) {
        return request.p_encode();
    }
    public static A_Request decode(ByteBuffer bytes) {
        int type, bodyLength;
        ByteBuffer body;
        RequestInfo info;
        A_Request request;

        bytes.flip();

        // Extract heads information
        type = bytes.getInt();
        bodyLength = bytes.getInt();

        // Extract body into a byte buffer.
        body = ByteBuffer.allocate(bodyLength);
        while (body.hasRemaining()) body.put(bytes); // Same as body.put(bytes.get()). This is bulk.

        // Create request.
        info = _INFOS.get(type);
        if (info == null) return null;
        request = info.FACTORY.get();
        request.p_decode(body);

        return request;
    }

    public static boolean regsiterRequest(Class<? extends A_Request> type, Supplier<? extends A_Request> factory) {
        int id;

        if (Addiak.isInit() || !Addiak.hasInitializedSetRequests()) return false; // Ensures a predefined order for addiak set requests. No real use case currently, but it is good to have certainty. TODO: Update this comment.
        if (_TYPE_IDS.containsKey(type)) return false;

        id = _nextFreeId++;

        _TYPE_IDS.put(type, id);
        _INFOS.put(id, new RequestInfo(type, factory));

        return true;
    }


}


class RequestInfo {


    // -+- CREATION -+- //

    public RequestInfo(Class<? extends A_Request> type, Supplier<? extends A_Request> factory) {
        TYPE = type;
        FACTORY = factory;
    }


    // -+- PARAMETERS -+- //

    // FINALS //

    public final Class<? extends A_Request> TYPE;
    public final Supplier<? extends A_Request> FACTORY;


}