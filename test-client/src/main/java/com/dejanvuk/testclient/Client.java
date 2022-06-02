package com.dejanvuk.testclient;

import com.dejanvuk.parser.Parser;

import java.io.DataInputStream;
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
            socket = new Socket("localhost", 6379);
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

            if(userInput.equals("exit")) {
                System.out.println("See ya!");
                // TODO: do clean up
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.exit(1);
            }

            utilityCli.processCommand(userInput);
        }
    }
}
