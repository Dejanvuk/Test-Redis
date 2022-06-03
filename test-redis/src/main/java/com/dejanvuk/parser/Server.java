package com.dejanvuk.parser;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        Scanner scanner = new Scanner(System.in);
        final int port = 6379; // TODO: custom port read from file or system env

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

        // main loop, listen for incoming clients
        while(true) {
            String userInput = scanner.nextLine();

            if(userInput.equals("quit")) {
                System.out.println("Shutting down...");
                // clean up
                // Note: System.in is opened by JVM. Hence, it's the responsibility of JVM to close the same
                try {
                    serverSocket.close(); // Not necessary as it's closed by OS, but it's good practice
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Shut down successfully!");
                System.exit(0);
            }

            try {
                // TODO: Encapsulate the socket and store it to close it at will when needed; maybe for a LFU connection cache in the future?
                Socket socket = serverSocket.accept();

                System.out.println("New client received: " + socket.getRemoteSocketAddress().toString());

                Thread clientThread = new Thread(new HandleClientThread(socket));
                clientThread.start();

            } catch (IOException e) {
                //e.printStackTrace();
                System.out.println("ERROR " + e.getMessage() + " Unable to accept the client connection!");
            }
        }

        /*
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }
}
