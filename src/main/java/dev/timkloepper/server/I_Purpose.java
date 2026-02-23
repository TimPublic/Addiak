package dev.timkloepper.server;


public interface I_Purpose {


    // -+- ENUMS -+- //

    enum PURPOSE {
        LOGGING,
    }


    // -+- PURPOSE LOGIC -+- //

    PURPOSE getPurpose();


}