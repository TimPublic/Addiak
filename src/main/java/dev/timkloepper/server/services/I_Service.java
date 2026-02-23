package dev.timkloepper.server.services;


import dev.timkloepper.server.I_Attachment;
import dev.timkloepper.server.Server;


public interface I_Service extends I_Attachment {


    // -+- LIFE CYCLE -+- //

    void activate(Server server);
    void deactivate(Server server);


}