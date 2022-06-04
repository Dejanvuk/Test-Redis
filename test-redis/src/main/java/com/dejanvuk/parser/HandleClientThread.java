package com.dejanvuk.parser;

import com.dejanvuk.parser.Utility.MessageNodeList;
import com.dejanvuk.parser.Utility.Value;
import com.dejanvuk.parser.exceptions.InvalidMsgException;
import com.dejanvuk.parser.types.DataType;
import com.dejanvuk.parser.types.MsgType;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Thread for handling new clients
 */
public class HandleClientThread implements Runnable{
    private final Socket socket;
    private DataInputStream in = null;
    private OutputStreamWriter out = null;
    private Parser parser = null;
    Map<String, Value> db = new HashMap<>(); // in-memory db
    MessageNodeList messageNodeList = null;

    public HandleClientThread(Socket socket) {
        this.socket = socket;
        this.messageNodeList = new MessageNodeList(3, db);
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

        /*
        * Main loop where we read the incoming commands from the client
        * */
        while(!socket.isClosed()) {
            try {
                List<Message> messages = new ArrayList<>();
                // 1st: read the data
                parser.readData(messages);

                // 1.1st: Print the command received
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
                    response = MakeCommandUtility.makeErrorMessage("ERROR:","Invalid command received, please see the --help for guidance!");
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
                    response = MakeCommandUtility.makeErrorMessage("ERROR:","Invalid command received, please see the --help for guidance!");
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
                else if(msgType == MsgType.RENAME) {
                    response = processRenameMsg(messages);
                }

                // 4th: write the message back to the client
                sendMessage(response);
            } catch (IOException e) {
                //e.printStackTrace();
                System.out.println("Client" + socket.getRemoteSocketAddress().toString() +  " disconnected unexpectedly!");
                try {
                    cleanUp();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } catch (InvalidMsgException e) {
                //e.printStackTrace();

                String response = MakeCommandUtility.makeErrorMessage("ERROR: ", e.getMessage());
                try {
                    sendMessage(response);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Cleans up the resources
     * Not necessary as it's closed by OS, but it's good practice
     */
    public void cleanUp() throws IOException {
        if(in != null) in.close();
        if(out != null) out.close();
    }

    /**
     * Processes the SET message received
     * @param messages : list of decoded messages
     * @return an encoded string containing the messages to be sent back to the client
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
        List<Object> valueList = new ArrayList<>();
        for(int i = 2; i < messages.size(); i++) {
            valueList.add(messages.get(i).data);
        }

        String key = (String)messages.get(1).data;

        // create the MessageNode and perform the LRU put
        MessageNodeList.MessageNode newNode = messageNodeList.put(key);
        Value value = new Value(valueList);
        value.setMessageNode(newNode);

        db.put(key, value);

        // send an OK message back
        return MakeCommandUtility.makeOkMessage();
    }

    /**
     * SET,DELETE messages it will send back an empty OK
     * GET message will send back and OK alongside the encoded data as string
     * @return an encoded string containing the messages to be sent back to the client
     *
     * Empty OK         OK with data
     * S: *1\r\n        S: *{nr of messages + 1}\r\n
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
        String key = (String)messages.get(1).data;

        if(db.containsKey(key)) {
            List<Object> valueList = db.get(key).getValues();

            // make the key the MRU
            messageNodeList.get(key);

            // send an OK message back along with the data
            return MakeCommandUtility.makeOkMessageWithData(valueList);
        }
        else {
            return MakeCommandUtility.makeErrorMessage("GET ERROR", "Key not found!");
        }
    }

    /**
     * Processes the DELETE message received
     * @param messages : list of decoded messages
     * @return an encoded string containing the messages to be sent back to the client
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
        String key = (String)messages.get(1).data;
        if(db.containsKey(key)) {
            db.remove(key);

            //Delete the Node in LRU List
            messageNodeList.delete(key);

            return MakeCommandUtility.makeOkMessage(); // send an OK message back
        }
        else {
            return MakeCommandUtility.makeErrorMessage("DELETE ERROR", "Key "  + key + "not found!");
        }
    }

    /**
     * Processes the RENAME message received
     * @param messages : list of decoded messages
     * @return an encoded string containing the messages to be sent back to the client
     */
    public String processRenameMsg(List<Message> messages){
        System.out.println("PROCESSING RENAME MESSAGE");
        /*
        #### Client requests for **DELETE("abcd")**
        C: $6\r\n 0
        C: RENAME\r\n
        C: $6\r\n  1
        C: oldkey\r\n
        C: $6\r\n  2
        C: newkey\r\n
        */
        String oldKey = (String)messages.get(1).data;
        String newkey = (String)messages.get(2).data;
        if(db.containsKey(oldKey)) {
            // cannot rename the key once created, so delete and recreate
            Value oldValue = db.remove(oldKey);
            oldValue.getMessageNode().setKey(newkey);
            db.put(newkey, oldValue);

            // we dont have to modify the LRU cache after as only the name of the key is changed

            return MakeCommandUtility.makeOkMessage(); // send an OK message back
        }
        else {
            return MakeCommandUtility.makeErrorMessage("RENAME ERROR", "Key " + oldKey + "not found!");
        }
    }

    /**
     * Sends the command to the Redis server
     * @param response : the encoded command to be sent to the server
     */
    public void sendMessage(String response) throws IOException {
        out.write(response);
        out.flush();
    }
}
