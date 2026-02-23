package dev.timkloepper.main;


import dev.timkloepper.client.Client;
import dev.timkloepper.server.Server;


public class Main {


    public static void main(String[] args) {
        Server server;

        server = Server.create(8080);

        Client client;

        client = new Client();
        client.connect(8080);
        client.disconnect();

        server.down();
        server.up(8080);

        server = null;
        System.gc();
    }


}