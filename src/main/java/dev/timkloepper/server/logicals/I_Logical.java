package dev.timkloepper.server.logicals;


import dev.timkloepper.server.I_Attachment;
import dev.timkloepper.server.Server;


public interface I_Logical extends I_Attachment {


    // -+- LIFE CYCLE -+- //

    void activate(Server server);
    void deactivate(Server server);


    // -+- UPDATE LOOP -+- //

    void update();


}