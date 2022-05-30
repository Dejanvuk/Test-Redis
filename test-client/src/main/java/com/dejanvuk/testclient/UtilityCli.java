package com.dejanvuk.testclient;

/* ====================
    Utility methods for parsing CLI input, for example messages
    ====================
    */

import com.dejanvuk.parser.MakeCommandUtility;
import com.dejanvuk.parser.Message;
import com.dejanvuk.parser.types.DataType;
import com.dejanvuk.parser.types.MsgType;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class UtilityCli {
    private OutputStreamWriter out = null;

    public UtilityCli(OutputStreamWriter outputStreamWriter) {
        this.out = outputStreamWriter;
    }

    /**
     * 1st: read the command first
     *
     * 2nd:
     * if command isn't valid stop and show an error
     * else if command is valid encode it to match the Redis protocol, before sending it to the server
     *
     * 3rd: send it to the server
     * @param line
     */
    public void processCommand(String line) {
        // TO-DO: Needs more validation testing for bad input
        // TO-DO: Add support for array

        line = line.replaceAll("\\s","");// Remove white lines , just to be sure

        StringBuilder commandBuilder = new StringBuilder(); String command = "";
        StringBuilder keyBuilder = new StringBuilder(); String key = "";
        StringBuilder valueBuilder = new StringBuilder();
        List<Object> values = new ArrayList<>();
        String response = ""; // will hold the final encoded version of the command to be sent to the Redis server

        int i = 0;
        // 1a:read the commandBuilder
        while(i < line.length() && line.charAt(i) != '(') {
            commandBuilder.append(line.charAt(i++));
        }

        command = commandBuilder.toString().toUpperCase(Locale.ROOT); // make the ocmmand to upper case, just to be sure

        // if commandBuilder not found print the error and exit immediately
        if(!isCommandValid(command)) {
            System.out.println("Command doesnt exist! ");
            return;
        }

        if(line.charAt(++i) != '"') {
            System.out.println("Incorrect command: missing \" ");
            return;
        }

        // 1b: read the keyBuilder
        while(i < line.length() && line.charAt(++i) != '"') {
            keyBuilder.append(line.charAt(i));
        }

        key = keyBuilder.toString();


        // only for SET,MSET,MGET command
        if(command.equals(MsgType.SET.name())) {
            if(line.charAt(++i) != ',') {
                System.out.println("Incorrect command: missing , after keyBuilder");
                return;
            }
            else {
                i++; // skip the ','
            }
            // 1c: read the value/values for arrays
            boolean isInteger = true;
            while(i < line.length()) {
                char curr = line.charAt(i++);
                if(curr == ')' ||
                        (isInteger == false && curr == '"') || // string value ended
                        (isInteger == true && curr == ',')) { // integer value ended
                    if(isInteger) {
                        values.add(Integer. valueOf(valueBuilder.toString()));
                    }
                    else { // string
                        values.add(valueBuilder.toString());
                        i++; // also skip the , char
                    }
                    valueBuilder = new StringBuilder();
                }
                valueBuilder.append(curr);
            }

            response = MakeCommandUtility.makeSetMessage(key, values);
        }
        else if(command.equals(MsgType.GET.name())) {
            response = MakeCommandUtility.makeGetMessage(key);
        }
        else if(command.equals(MsgType.DELETE.name())) {
            response = MakeCommandUtility.makeDeleteMessage(key);
        }

        //Just for testing purposes
        System.out.println(command + " " + key);

        // send the command to the server
        sendMessage(response);

        System.out.println("Sent the command to the server!");
    }

    /**
     * Sends the command to the Redis server
     * @param response
     */
    public void sendMessage(String response) {
        /*
        String setTest1 = parser.makeSetMessage("abcd", 123456);
        System.out.println("set(\"abcd), 123456) sent");
        sendMessage(setTest1);
        */

        try {
            out.write(response);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isCommandValid(String command) {
        for(MsgType msgType : MsgType.values()) {
            if(msgType.name().equals(command))
                return true;
        }

        return false;
    }
}
