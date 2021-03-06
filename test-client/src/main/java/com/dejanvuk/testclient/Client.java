package com.dejanvuk.testclient;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        Socket socket = null;
        Scanner scanner = new Scanner(System.in);
        UtilityCli utilityCli = null;

        try {
            String host = System.getenv("REDIS_HOST") != null ? System.getenv("REDIS_HOST") : "localhost";
            int port  = System.getenv("REDIS_PORT") != null ? Integer. parseInt(System.getenv("REDIS_PORT")) : 6379;
            System.out.println("Attempting to connect to " + host + ":" + port);
            socket = new Socket(host, 6379);
            System.out.println("Succes!");
            // make a new thread to read the input from the server
            Thread serverThread = new Thread(new HandleServerCommunicationThread(socket));
            serverThread.start();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            utilityCli = new UtilityCli(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // read client commands until 'exit' is detected
        while(true) {
            String userInput = scanner.nextLine();

            if(userInput.equals("quit")) {
                System.out.println("Shutting down...");
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                System.out.println("Shut down successfully!");
                System.exit(0);
            }

            utilityCli.processCommand(userInput);
        }
    }
}
