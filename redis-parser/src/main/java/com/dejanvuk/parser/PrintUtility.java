package com.dejanvuk.parser;

import com.dejanvuk.parser.types.DataType;

import java.util.List;

public class PrintUtility {
    /* ====================
    Utility methods for printing to CLI
    ====================
    */

    /**
     * Print the list of messages decocoded
     * The server will also print the command, which is the first message (after the array message was discarded before creating the list)
     * @param messages : the list of decoded messages
     */
    public static void printMessage(List<Message> messages) {
        StringBuilder sb = new StringBuilder();
        boolean isMsg = false;

        for(int i = 0; i < messages.size(); i++) { // 0 because first * is already skipped in readData
            Message message = messages.get(i);

            if(message.msgType != null) { // only servers can receive commands
                sb.append("Command received: " + message.msgType.toString() + "(");
                isMsg = true;
            }
            else {
                if(message.dataType == DataType.INTEGER) {
                    sb.append(Integer.toString((Integer) message.data));
                }
                else {
                    sb.append((String) message.data);
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
}
