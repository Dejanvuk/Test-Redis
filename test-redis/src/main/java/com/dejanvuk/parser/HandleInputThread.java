package com.dejanvuk.parser;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;

/**
 * Thread handling the input from the command line
 */
public class HandleInputThread implements Runnable{
    Scanner scanner = new Scanner(System.in);
    ServerSocket serverSocket = null;

    public HandleInputThread(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        System.out.println("Please type help for the full list of commands and their definition.");

        while(true) {
            String userInput = scanner.nextLine();

            if(userInput.equals("quit")) {
                System.out.println("Shutting down...");
                // clean up
                // Note: System.in is opened by JVM. Hence, it's the responsibility of JVM to close the same
                try {
                    serverSocket.close(); // Not necessary as it's closed by OS, but it's good practice
                } catch (IOException e) {
                    //e.printStackTrace();
                    System.out.println("Unable to gracefully close the server socket!");
                    System.exit(-1);
                }
                System.out.println("Shut down successfully!");
                System.exit(0);
            }
        }
    }
}
