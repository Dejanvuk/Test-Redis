package com.dejanvuk.parser;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        final int port = 6379; // TO-DO: custom port read from file or system env

        System.out.println("========================" +
                "" +
                "Test Redis Server v1.0.0" +
                "" +
                "========================");

        try {
            serverSocket = new ServerSocket(port);

            System.out.println("Waiting for incoming client connections on: " + serverSocket.getInetAddress() + ":" + port);
        } catch (IOException e) {
            // TO-DO "Error could not listen on port " + port"
            System.exit(1);
        }

        // main loop, listen for incoming clients
        // TO-DO: replace true with a conditional
        while(true) {
            try {
                Socket socket = serverSocket.accept(); // TO-DO: Encapsulate the socket and store it to close it at will when needed

                System.out.println("New client received: " + socket.getRemoteSocketAddress().toString());

                Thread clientThread = new Thread(new HandleClientThread(socket));
                clientThread.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
