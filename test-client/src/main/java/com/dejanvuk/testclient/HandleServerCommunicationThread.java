package com.dejanvuk.testclient;

import com.dejanvuk.parser.Parser;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

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

        while(true) {
            // SET("abcd", 123456)
            String setTest1 = parser.makeSetMessage("abcd", 123456);
            System.out.println(setTest1);
            sendMessage(setTest1);
            // GET("abcd")
            String getTest1 = parser.makeGetMessage("abcd");
            System.out.println(getTest1);
            sendMessage(getTest1);
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
