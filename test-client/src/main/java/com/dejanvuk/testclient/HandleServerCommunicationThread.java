package com.dejanvuk.testclient;

import com.dejanvuk.parser.Message;
import com.dejanvuk.parser.Parser;
import com.dejanvuk.parser.PrintUtility;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class HandleServerCommunicationThread implements Runnable{
    private final Socket socket; // TO-DO: Handle different socket states and add a conditional to close it
    private DataInputStream in = null;
    private Parser parser = null;

    public HandleServerCommunicationThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            this.in = new DataInputStream(socket.getInputStream());
            this.parser = new Parser(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // keep reading messages from the server
        while(!socket.isClosed()) { // listen for messages from the server
            //System.out.println("Waiting for server messages");
            /*
            // SET("abcd", 123456)
            String setTest1 = parser.makeSetMessage("abcd", 123456);
            System.out.println(setTest1);
            sendMessage(setTest1);
            // GET("abcd")
            String getTest1 = parser.makeGetMessage("abcd");
            System.out.println(getTest1);
            sendMessage(getTest1);
            */


            List<Message> messages = new ArrayList<>();
            try {
                parser.readData(messages);
                PrintUtility.printMessage(messages);
            } catch (IOException e) {
                //e.printStackTrace();
                System.out.println("ERROR " + e.getMessage());
                System.exit(-1);
            }

        }
    }
}
