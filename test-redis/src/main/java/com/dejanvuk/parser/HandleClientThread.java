package com.dejanvuk.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;

public class HandleClientThread implements Runnable{
    private final Socket socket; // TO-DO: Handle different socket states and add a conditional to close it
    private BufferedReader in = null;
    private Parser parser = null;

    public HandleClientThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            parser = new Parser(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(socket.isConnected()) {
            // read the data
            List<Message> messages = null;

            try {
                parser.readData(messages);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // process it
            processData(messages);
            // write the message back to the client
            sendMessage();
        }
    }

    public void processData(List<Message> messages) {
        /*
        MsgType msgType = MsgType.valueOf();

        switch (msgType) {
            case SET:
                parser.
                break;
            case GET:

                break;
            case DELETE:

                break;
        }
        */

    }


    public void sendMessage() {
    }
}
