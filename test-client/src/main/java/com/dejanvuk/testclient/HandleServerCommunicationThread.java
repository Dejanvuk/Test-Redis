package com.dejanvuk.testclient;

import com.dejanvuk.parser.Message;
import com.dejanvuk.parser.Parser;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class HandleServerCommunicationThread implements Runnable{
    private final Socket socket; // TO-DO: Handle different socket states and add a conditional to close it
    private DataInputStream in = null;
    private OutputStreamWriter out = null;
    private Parser parser = null;

    public HandleServerCommunicationThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            this.in = new DataInputStream(socket.getInputStream());
            this.parser = new Parser(in);
            this.out = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        String setTest1 = parser.makeSetMessage("abcd", 123456);
        System.out.println("set(\"abcd), 123456) sent");
        sendMessage(setTest1);
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

            /*
            List<Message> messages = new ArrayList<>();
            try {
                parser.readData(messages);
                System.out.println("read messages");
            } catch (IOException e) {
                e.printStackTrace();
            }
            */
        }
    }

    public void sendMessage(String response) {
        try {
            out.write(response);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
