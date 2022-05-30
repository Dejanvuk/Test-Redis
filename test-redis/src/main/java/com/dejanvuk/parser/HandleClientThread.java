package com.dejanvuk.parser;

import com.dejanvuk.parser.exceptions.InvalidMsgException;
import com.dejanvuk.parser.types.DataType;
import com.dejanvuk.parser.types.MsgType;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HandleClientThread implements Runnable{
    private final Socket socket; // TO-DO: Handle different socket states and add a conditional to close it
    private DataInputStream in = null;
    private OutputStreamWriter out = null;
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
            out = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(socket.isConnected()) {
            try {
                List<Message> messages = new ArrayList<>();
                // 1st: read the data
                parser.readData(messages);

                // 1.1st: Print the command received
                System.out.print("Message received and parsed: ");
                PrintUtility.printMessage(messages);

                // 2nd: process the data

                /*
                Note:
                -first message from client is always a BULK STR with the command name
                -second message is always another BULK STR with the name of the key
                -the server can send back direct messages like OK or ERROR though

                Read through-out the messages list and process the data
                Also use a double-linked list for LRU cache functionality
                */

                String response = null;

                Message message = messages.get(0);
                MsgType msgType = message.msgType;

                if(!messages.get(0).dataType.equals(DataType.BULK_STR)) {
                    response = MakeCommandUtility.makeErrorMessage("ERROR:","First message is always an array!");
                }
                else if(msgType == null || message.dataType == DataType.ERROR) {
                    /**
                     *  The messages can throw errors during processing
                     *  Send it back to the client alongside the exception
                     *
                     * ERROR message with the exception
                     * S: *2\r\n
                     * S: -{ERROR} {exception as string}\r\n
                     */
                    response = MakeCommandUtility.makeErrorMessage("ERROR:","Invalid message received, please see the --help for guidance!");
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

        /*
        try {
            cleanUp();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }

    /**
     * Cleans up the resources
     */
    public void cleanUp() throws IOException {
        if(in != null) in.close();
        if(out != null) out.close();
        // We don't close the socket in here, we will close it manually if needed after sending the message completely
    }

    /**
     *
     * @param messages
     * @return a list containing the messages to be send back to the client
     * @throws InvalidMsgException
     */
    public String processSetMsg(List<Message> messages){
        System.out.println("PROCESSING SET MESSAGE");
        /*
        example of client requests for SET("abcd", 123456)
        C: $3\r\n   0
        C: SET\r\n
        C: $4\r\n   1
        C: abcd\r\n
        C: :123456\r\n  2
        {$ String: "SET"},{$ String: "ABCD"},{: Integer: 123456}
        */
        List<Message> valueList = new ArrayList<>();
        for(int i = 2; i < messages.size(); i++) {
            valueList.add(messages.get(i));
        }

        db.put((String)messages.get(1).data[0], valueList);

        // send an OK message back
        return MakeCommandUtility.makeOkMessage();
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
        System.out.println("PROCESSING GET MESSAGE");

        // starting the get from 0 cause we store only the contents of the msg
        // check the processSetMsg above
        db.get((String)messages.get(1).data[0]);

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
        System.out.println("PROCESSING DELETE MESSAGE");
        /*
        #### Client requests for **DELETE("abcd")**
        C: $6\r\n 0
        C: DELETE\r\n
        C: $4\r\n  1
        C: abcd\r\n
        */
        db.remove((String)messages.get(1).data[0]);

        // send an OK message back
        return MakeCommandUtility.makeOkMessage();
    }

    /**
     *
     * @param response : to be send back to the client
     */
    public void sendMessage(String response) throws IOException {
        out.write(response);
        out.flush();
    }
}
