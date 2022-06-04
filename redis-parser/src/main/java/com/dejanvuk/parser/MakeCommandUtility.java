package com.dejanvuk.parser;

import java.util.List;

/**
 * Utility class to make commands
 */
public class MakeCommandUtility {
    /**
     * Used by clients to create a SET(key, value) command to the server
     * Deprecated because of makeSetMessage(String, List<Object>) below
     * @param key
     * @param val an Integer/String value
     * @return encoded command message
     */
    public static String makeSetMessage(String key, Object val) {
        StringBuilder sb = new StringBuilder();
        sb.append(makeArrayMessage(3));
        sb.append(makeBinaryMessage("SET"));
        sb.append(makeBinaryMessage(key));
        if(val.getClass() == Integer.class) {
            sb.append(makeIntegerMessage((Integer) val));
        }
        else if(val.getClass() == String.class) {
            sb.append(makeBinaryMessage((String) val));
        }

        return sb.toString();
    }

    /**
     * Used by clients to create a SET("key", value/s) command to the server
     * @param key
     * @param values a list of Integer/String values
     * @return encoded SET command message
     */
    public static String makeSetMessage(String key, List<Object> values) {
        StringBuilder sb = new StringBuilder();
        sb.append(makeArrayMessage(2 + values.size()));
        sb.append(makeBinaryMessage("SET"));
        sb.append(makeBinaryMessage(key));
        for(int i = 0 ; i < values.size(); i++) {
            Object val = values.get(i);

            if(val.getClass() == Integer.class) {
                sb.append(makeIntegerMessage((Integer) val));
            }
            else if(val.getClass() == String.class) {
                sb.append(makeBinaryMessage((String) val));
            }
        }

        return sb.toString();
    }

    /**
     * Used by clients to create a GET("key") command to the server
     * @param key
     * @return encoded GET command message
     */
    public static String makeGetMessage(String key) {
        StringBuilder sb = new StringBuilder();

        sb.append(makeArrayMessage(2));
        sb.append(makeBinaryMessage("GET"));
        sb.append(makeBinaryMessage(key));

        return sb.toString();
    }

    /**
     * Used by clients to create a DELETE("key") command to the server
     * @param key
     * @return encoded DELETE command message
     */
    public static String makeDeleteMessage(String key) {
        StringBuilder sb = new StringBuilder();

        sb.append(makeArrayMessage(2));
        sb.append(makeBinaryMessage("DELETE"));
        sb.append(makeBinaryMessage(key));

        return sb.toString();
    }

    /**
     * Used by clients to create a RENAME("oldKey", "newKey") command to the server
     * @param oldKey
     * @param newKey
     * @return encoded RENAME command message
     */
    public static String makeRenameMessage(String oldKey, String newKey) {
        StringBuilder sb = new StringBuilder();

        sb.append(makeArrayMessage(3));
        sb.append(makeBinaryMessage("RENAME"));
        sb.append(makeBinaryMessage(oldKey));
        sb.append(makeBinaryMessage(newKey));

        return sb.toString();
    }

    /* ====================
    Methods to convert the message to string
    ====================
    */

    /**
     * Used by clients to create an integer message
     * Format: :{nr}\r\n
     * @param nr
     * @return the encoded integer message
     */
    public static String makeIntegerMessage(int nr) {
        StringBuilder sb = new StringBuilder();

        sb.append(":" + nr + "\r\n");

        return sb.toString();
    }

    /**
     * Used by clients to create a simple string message
     * Format: +{str}\r\n
     * @param str
     * @return the encoded simple string message
     */
    public static String makeSimpleStrMessage(String str) {
        StringBuilder sb = new StringBuilder();

        sb.append("+" + str + "\r\n");

        return sb.toString();
    }

    /**
     * Used by clients to create an OK message
     * Format:
     * *1\r\n
     * +OK\r\n
     * @return the encoded OK message
     */
    public static String makeOkMessage() {
        StringBuilder sb = new StringBuilder();

        sb.append(makeArrayMessage(1));
        sb.append(MakeCommandUtility.makeSimpleStrMessage("OK"));

        return sb.toString();
    }

    /**
     * Used by clients to create an OK message containing data
     * Format:
     * *{nr of messages + 1}\r\n
     * +OK\r\n
     * {data}
     * @param values : List of Integer/Spring data
     * @return the encoded OK message with data
     */
    public static String makeOkMessageWithData(List<Object> values) {
        StringBuilder sb = new StringBuilder();

        sb.append(makeArrayMessage(1 + values.size())); // + 1 for OK simple string
        sb.append(MakeCommandUtility.makeSimpleStrMessage("OK"));

        for(int i = 0; i < values.size(); i++) {
            Object val = values.get(i);
            if(val.getClass() == Integer.class) {
                sb.append(MakeCommandUtility.makeIntegerMessage((Integer)val));
            }
            else if(val.getClass() == String.class) {
                sb.append(MakeCommandUtility.makeBinaryMessage((String) val));
            }
        }

        return sb.toString();
    }

    /**
     * Used by clients to create an ERROR message
     * Format: -{Error} {Message}\r\n
     * example: -WRONGTYPE Operation against a key holding the wrong kind of value
     * @param error : Error
     * @param exception : Message
     * @return the encoded error message
     */
    public static String makeErrorMessage(String error, String exception) {
        StringBuilder sb = new StringBuilder();

        sb.append(makeArrayMessage(1));
        sb.append("-" + error + " " + exception + "\r\n");

        return sb.toString();
    }

    /**
     * Used by clients to create a null message
     * Format: $-1\r\n
     * @return the encoded null message
     */
    public static String makeNullMessage() {
        StringBuilder sb = new StringBuilder();

        sb.append("$-1\r\n");

        return sb.toString();
    }

    /**
     * Used by clients to create an array message
     * Format: *{length}\r\n
     * @param length : number of messages to follow
     * @return the encoded array message
     */
    public static String makeArrayMessage(int length) {
        StringBuilder sb = new StringBuilder();
        sb.append("*" + length + "\r\n");

        return sb.toString();
    }

    /**
     * Used by clients to create a bulk string message
     * Format:
     * ${str length}\r\n
     * {str}\r\n
     * @param str : the string to be encoded
     * @return th encoded bulk string message
     */
    public static String makeBinaryMessage(String str) {
        int length = str.length();
        StringBuilder sb = new StringBuilder();

        sb.append("$" + length + "\r\n");
        sb.append(str + "\r\n");

        return sb.toString();
    }
}
