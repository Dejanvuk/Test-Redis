package com.dejanvuk.parser;

import com.dejanvuk.parser.exceptions.InvalidMsgException;
import com.dejanvuk.parser.types.DataType;
import com.dejanvuk.parser.types.MsgType;

import java.io.DataInputStream;
import java.io.IOException;
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
                // TO-DO: The message processing methods should return a message list which will then be encoded and
                // sent back to the client

                String response = null;

                if(!messages.get(0).dataType.equals(DataType.ARRAY) || !messages.get(1).dataType.equals(DataType.BULK_STR)) {
                    throw new InvalidMsgException();
                }

                Message message = messages.get(2);
                MsgType msgType = message.msgType;

                if(msgType == null || message.dataType == DataType.ERROR) {
                    // return back the error message
                    /**
                     *  The messages can throw errors during processing
                     *  Send it back to the client alongside the exception
                     *
                     * ERROR message with the exception
                     * S: *2\r\n
                     * S: +ERROR\r\n
                     * S: ${nr of bytes of the string}\r\n
                     * S: {exception as string}\r\n
                     */
                    response = parser.makeBinaryMessage("invalid message!");
                }
                else if(msgType == MsgType.SET) {
                    response = processSetMsg(messages);
                }
                else if(msgType == MsgType.GET) {
                    response = processGetMsg(messages);
                }
                else if(msgType == MsgType.DELETE) {
                    response = processDeleteMsg(messages);
                }
                // 4th: write the message back to the client
                sendMessage(response);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidMsgException e) {
                e.printStackTrace();
                // TO-DO: Send client an error message
            }
        }
    }

    /**
     *
     * @param messages
     * @return a list containing the messages to be send back to the client
     * @throws InvalidMsgException
     */
    public String processSetMsg(List<Message> messages){
        /*
        example of client requests for SET("abcd", 123456)
        C: *2\r\n   0
        C: $3\r\n   1
        C: SET\r\n  2
        C: $4\r\n   3
        C: abcd\r\n     4
        C: :123456\r\n  5
        */
        List<Message> valueList = new ArrayList<>();
        for(int i = 5; i < messages.size(); i++) {
            valueList.add(messages.get(i));
        }
        db.put((String)messages.get(4).data[0], valueList);

        // send an OK message back
        return parser.makeSimpleStrMessage("OK");
    }

    /**
     * SET,DELETE messages it will send back an empty OK
     * GET message will send back and OK alongside the encoded data as string
     * @return
     *
     * Empty OK         OK with data
     * S: *1\r\n        S: *{nr of messages}\r\n
     * S: +OK\r\n       S: +OK\r\n
     *                  S: {data}
     * {data} will be non-array, however later we will add support to parse complex nested arrays and data
     * if {data} is array, get each message from the array
     * {nr of messages} is 2 for non-array, and for array is the array size + 1 to account for the OK simple string
     */
    public String processGetMsg(List<Message> messages){
        // starting the get from 0 cause we store only the contents of the msg
        // check the processSetMsg above
        db.get((String)messages.get(0).data[0]);

        // send an OK message back along with the data
        return parser.encodeResponse(messages);
    }

    /**
     *
     * @param messages
     * @return a list containing the messages to be send back to the client
     * @throws InvalidMsgException
     */
    public String processDeleteMsg(List<Message> messages){
        /*
        #### Client requests for **DELETE("abcd")**
        C: *2\r\n 0
        C: $6\r\n 1
        C: DELETE\r\n 2
        C: $4\r\n 3
        C: abcd\r\n 4
        */
        db.remove((String)messages.get(4).data[0]);

        // send an OK message back
        return parser.makeSimpleStrMessage("OK");
    }

    /**
     *
     * @param response : to be send back to the client
     */
    public void sendMessage(String response) {
    }
}
