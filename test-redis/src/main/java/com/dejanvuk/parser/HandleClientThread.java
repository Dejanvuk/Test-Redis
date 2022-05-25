package com.dejanvuk.parser;

import com.dejanvuk.parser.exceptions.InvalidMsgException;
import com.dejanvuk.parser.types.DataType;
import com.dejanvuk.parser.types.MsgType;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HandleClientThread implements Runnable{
    private final Socket socket; // TO-DO: Handle different socket states and add a conditional to close it
    private DataInputStream in = null;
    private Parser parser = null;
    Map<String, List<Message>> db = new HashMap<>(); // in-memory db


    public HandleClientThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new DataInputStream(socket.getInputStream());
            parser = new Parser(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(socket.isConnected()) {
            try {
                List<Message> messages = new ArrayList<>();
                // 1st: read the data
                parser.readData(messages);

                // 2nd: process the data

                /*
                Note:
                -first message is always an array
                -second message is always a simple string with the command name in msgType

                Read through-out the messages list and process the data
                Also use a double-linked list for LRU cache functionality
                 */

                // TO-DO: Verify better that the first and second messages to be array and simple str
                if(!messages.get(0).dataType.equals(DataType.ARRAY) || !messages.get(1).dataType.equals(DataType.SIMPLE_STR)) {
                    throw new InvalidMsgException();
                }

                Message message = messages.get(1);
                MsgType msgType = message.msgType;

                if(msgType == null) {
                    throw new InvalidMsgException();
                }

                // TO-DO: The message processing methods should return a message which will then be written to the client
                List<Message> response = null;
                // TO-DO: Create a method which extracts the key's name
                if(msgType == MsgType.SET) {
                    processSetMsg(messages);
                }
                if(msgType == MsgType.GET) {
                    processGetMsg(messages);
                }
                if(msgType == MsgType.DELETE) {
                    processDeleteMsg(messages);
                }
                // 3rd: write the message back to the client
                sendMessage();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidMsgException e) {
                e.printStackTrace();
                // TO-DO: Send client an error message
            }
        }
    }

    public void processSetMsg(List<Message> messages) throws InvalidMsgException{
        /*
        example of client requests for SET("abcd", 123456)
        C: *2\r\n
        C: $3\r\n
        C: SET\r\n
        C: $4\r\n
        C: abcd\r\n
        C: :123456\r\n
        */
        db.put((String)messages.get(2).data[0], messages);

        // send an OK message back or ERROR
    }

    public void processGetMsg(List<Message> messages) throws InvalidMsgException{
        db.get((String)messages.get(2).data[0]);

        // send an OK message back along with the data or ERROR
    }

    public void processDeleteMsg(List<Message> messages) throws InvalidMsgException{
        db.remove((String)messages.get(2).data[0]);

        // send an OK message back or ERROR
    }

    public void sendMessage() {
    }
}
