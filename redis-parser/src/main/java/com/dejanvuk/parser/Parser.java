package com.dejanvuk.parser;

import com.dejanvuk.parser.exceptions.InvalidMsgException;
import com.dejanvuk.parser.types.DataType;
import com.dejanvuk.parser.types.MsgType;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/*
https://redis.io/docs/reference/protocol-spec
Client sends requests in an array
Server also stores the requests in an array, even thoug there might be just one request
* */
public class Parser {
    private DataInputStream in;

    public Parser(DataInputStream in) {
        this.in = in;
    }

    /**
     * Encodes the Message List
     * Typically used for GET messages
     * @param messages
     * @return
     */
    // TO-DO: Test this method
    public String encodeResponse(List<Message> messages) {
        int length = messages.size();

        StringBuilder sb = new StringBuilder(length + 6);

        sb.append(MakeCommandUtility.makeArrayMessage(length + 1)); // 1 extra for the Ok simple string
        sb.append(MakeCommandUtility.makeSimpleStrMessage("OK"));
        for(int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);

            if(message.dataType == DataType.INTEGER) {
                sb.append(MakeCommandUtility.makeIntegerMessage((Integer)message.data[0]));
            }
            else if(message.dataType == DataType.SIMPLE_STR) {
                sb.append(MakeCommandUtility.makeSimpleStrMessage((String) message.data[0]));
            }
            else if(message.dataType == DataType.BULK_STR) {
                sb.append(MakeCommandUtility.makeBinaryMessage((String) message.data[0]));
            }
        }

        return sb.toString();
    }

    public void readInteger(List<Message> messages) throws IOException {
        int number = readInteger();
        Object[] data = new Object[1];
        data[0] = number;
        Message message = new Message.MessageBuilder().setDataType(DataType.INTEGER).setData(data).build();
        messages.add(message);
    }

    public void readSimpleString(List<Message> messages) throws IOException {
        StringBuilder sb = new StringBuilder();

        char ch = (char) in.readUnsignedByte();

        while(ch != '\r') {
            sb.append(ch);
            ch = (char) in.readUnsignedByte();
        }
        in.skipBytes(1); // skip CLRF, only \n left to skip

        Object[] data = new Object[1];
        data[0] = sb.toString();
        Message message = new Message.MessageBuilder().setDataType(DataType.SIMPLE_STR).setData(data).build();
        messages.add(message);
    }

    /**
     * Clients won't really send errors but whatever, usually only the server would send the client such a message
     * @param messages
     * @throws IOException
     */
    public void readError(List<Message> messages) throws IOException {
        StringBuilder sb = new StringBuilder();

        char ch = (char) in.readUnsignedByte();

        while(ch != '\r') {
            sb.append(ch);
            ch = (char) in.readUnsignedByte();
        }
        in.skipBytes(1); // skip CLRF, only \n left to skip

        Object[] data = new Object[1];
        data[0] = sb.toString();
        Message message = new Message.MessageBuilder().setDataType(DataType.ERROR).setData(data).build();
        messages.add(message);
    }

    public void readBulkString(List<Message> messages) throws IOException{
        int length = readInteger(); // read the length of the bulk string
        char[] str = new char[length];

        for(int i = 0; i < length; i++) {
            str[i] = (char) in.readUnsignedByte();
        }
        in.skipBytes(2); // skip CLRF

        Object[] data = new Object[1];
        data[0] = String.valueOf(str);

        Message message = new Message.MessageBuilder().setDataType(DataType.BULK_STR).setData(data).setLength(length).build();

        if(data[0].equals("GET")) {
            message.msgType = MsgType.GET;
        }
        else if(data[0].equals("SET")) {
            message.msgType = MsgType.SET;
        }
        else if(data[0].equals("DELETE")) {
            message.msgType = MsgType.DELETE;
        }
        messages.add(message);
    }

    public void readArray(List<Message> messages) throws IOException {
        int length = readInteger(); // read the length of the array
        in.skipBytes(2); // skip CLRF

        Message message = new Message.MessageBuilder().setDataType(DataType.ARRAY).setLength(length).build();
        messages.add(message);
    }

    public void readData(List<Message> messages) throws IOException {
        System.out.println("Incoming message from client");

        char dataType = (char) in.readUnsignedByte();

        if(dataType != '*') { // First message is always an array
            throw new InvalidMsgException("Invalid message: expected an array first");
        }

        int length = readInteger(); // read the length of the array

        for(int i = 0; i < length; i++) {
            dataType = (char)in.readUnsignedByte(); // read the first byte of the reply

            switch (dataType) {
                case '+': // Simple Strings
                    readSimpleString(messages);
                    break;
                case '-': // Errors
                    readError(messages);
                    break;
                case ':': //  Integers
                    readInteger(messages);
                    break;
                case '$': // Bulk Strings
                    readBulkString(messages);
                    break;
                case '*': // Arrays
                    readArray(messages);
                    break;
                default:
                    throw new InvalidMsgException("Invalid message: Improper message type!");
            }
        }
    }

    // TO-DO: Test this method
    public int readInteger() throws IOException {
        int result = 0;

        char ch = (char) in.readUnsignedByte();

        while(ch != '\r') {
            int digit = ch - '0';
            result *= 10;
            result += digit;
            ch = (char) in.readUnsignedByte();
        }

        in.skipBytes(1); // skip CLRF, only \n left to skip

        return result;
    }

    /* ====================
    Utility methods for printing to CLI
    ====================
    */

    /**
     * Print the full command received
     * @param messages
     */
    public void printMessage(List<Message> messages) {
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
    public void printDatabase(Map<String, List<Message>> db) {

    }

    /**
     * Prints the value at key from database
     * @param key
     */
    public void printDbEntry( Map<String, List<Message>> db, String key) {

    }
}
