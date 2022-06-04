package com.dejanvuk.parser;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Main Server class
 */
public class Server {
    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        final int port = System.getenv("REDIS_PORT") != null ? Integer. parseInt(System.getenv("REDIS_PORT")) : 6379;

        System.out.println("========================" +
                "" +
                "Test Redis Server v1.0.0" +
                "" +
                "========================");

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Waiting for incoming client connections on: " + serverSocket.getInetAddress() + ":" + port);
        } catch (IOException e) {
            System.out.println("Error could not listen on port " + port);
            System.exit(-1);
        }

        // main thread that listens for user input
        Thread handleInputThread = new Thread(new HandleInputThread(serverSocket));
        handleInputThread.start();

        // main loop, listen for incoming clients
        while(true) {
            try {
                // TODO: Encapsulate the socket and store it to close it at will when needed; maybe for a LFU connection cache in the future?
                // 1: get a socket to the new client's connection
                Socket socket = serverSocket.accept();

                System.out.println("New client received: " + socket.getRemoteSocketAddress().toString());

                // 2: start a separate thread to handle the new client
                Thread clientThread = new Thread(new HandleClientThread(socket));
                clientThread.start();

            } catch (IOException e) {
                //e.printStackTrace();
                System.out.println("ERROR " + e.getMessage() + " Unable to accept the client connection!");
            }
        }

        /* Since it's only 1 socket, we don't really have to close it manually
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }
}
