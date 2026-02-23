package dev.timkloepper;


import dev.timkloepper.request.A_Request;
import dev.timkloepper.request.RequestManager;

import java.util.HashMap;
import java.util.function.Supplier;


public class Addiak {


    // -+- PARAMETERS -+- //

    // NON-FINALS //

    private static boolean _isInit = false;
    private static boolean _hasInitSetRequests = false;

    // FINALS //

    private static final HashMap<Class<? extends A_Request>, Supplier<? extends A_Request>> _INIT_REQUESTS = new HashMap<>();


    // -+- REQUEST LOGIC -+- //

    public static void registerCustomRequests(HashMap<Class<? extends A_Request>, Supplier<? extends A_Request>> requestTypes) {
        for (Class<? extends A_Request> type : requestTypes.keySet()) {
            RequestManager.regsiterRequest(type, requestTypes.get(type));
        }
    }


    // -+- INITIALIZATION -+- //

    public static boolean init() {
        h_initSetRequests();

        _isInit = true;

        return true;
    }
    public static boolean isInit() {
        return _isInit;
    }
    public static boolean hasInitializedSetRequests() {
        return _hasInitSetRequests;
    }

    private static void h_initSetRequests() {
        // RequestManager.regsiterRequest(..., ...); // Type: 1
        // ... // Type: 2
        // ... // ...
        // ...

        _hasInitSetRequests = true;
    }


}