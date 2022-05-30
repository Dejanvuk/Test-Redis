package com.dejanvuk.parser;

import com.dejanvuk.parser.types.DataType;

import java.util.List;
import java.util.Map;

public class PrintUtility {
    /* ====================
    Utility methods for printing to CLI
    ====================
    */

    /**
     * Print the full command received
     * @param messages
     */
    public static void printMessage(List<Message> messages) {
        StringBuilder sb = new StringBuilder();
        boolean isMsg = false;

        for(int i = 0; i < messages.size(); i++) { // 0 because first * is already skipped in readData
            Message message = messages.get(i);

            if(message.msgType != null) {
                sb.append("Command received: " + message.msgType.toString() + "(");
                isMsg = true;
            }
            else {
                if(message.dataType == DataType.INTEGER) {
                    sb.append(Integer.toString((Integer) message.data[0]));
                }
                else {
                    sb.append((String) message.data[0]);
                }

                if(i != messages.size() - 1)
                    sb.append(", ");
            }
        }

        if(isMsg) {
            sb.append(")");
        }

        System.out.println(sb.toString());
    }

    // TO-DO: Not yet needed :)
    /**
     * Prints the whole database
     */
    public static void printDatabase(Map<String, List<Message>> db) {

    }

    /**
     * Prints the value at key from database
     * @param key
     */
    public static void printDbEntry( Map<String, List<Message>> db, String key) {

    }
}
