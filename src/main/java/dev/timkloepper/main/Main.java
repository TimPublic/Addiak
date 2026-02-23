package dev.timkloepper.main;


import dev.timkloepper.server.Server;


public class Main {


    public static void main(String[] args) {
        Server server;

        server = Server.create(8080);

        server = null;

        System.gc();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


}