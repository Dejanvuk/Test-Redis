package com.dejanvuk.parser;

import com.dejanvuk.parser.exceptions.InvalidMsgException;
import com.dejanvuk.parser.types.DataType;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;


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

        sb.append(makeArrayMessage(length + 1)); // 1 extra for the Ok simple string
        sb.append(makeSimpleStrMessage("OK"));
        for(int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);

            if(message.dataType == DataType.INTEGER) {
                sb.append(makeIntegerMessage((Integer)message.data[0]));
            }
            else if(message.dataType == DataType.SIMPLE_STR) {
                sb.append(makeSimpleStrMessage((String) message.data[0]));
            }
            else if(message.dataType == DataType.BULK_STR) {
                sb.append(makeBinaryMessage((String) message.data[0]));
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
        in.skipBytes(2); // skip CLRF

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

    /*
    public void readDataOld(List<Message> messages) throws IOException {
        System.out.println("Reading data from client");

        int readByte = in.readUnsignedByte();


        char dataType = (char)readByte; // read the first byte of the reply

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
                // invalid data type; return error

        }

        readData(messages);
    }
    */

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
    Methods to convert the message to string
    ====================
    */
    public String makeIntegerMessage(int nr) {
        StringBuilder sb = new StringBuilder();

        sb.append(":" + nr + "\r\n");

        return sb.toString();
    }

    public String makeSimpleStrMessage(String str) {
        StringBuilder sb = new StringBuilder();

        sb.append("+" + str + "\r\n");

        return sb.toString();
    }

    public String makeErrorMessage(String exception) {
        StringBuilder sb = new StringBuilder();

        sb.append("-ERR " + exception + "\r\n");

        return sb.toString();
    }

    public String makeNullMessage() {
        StringBuilder sb = new StringBuilder();

        sb.append("$-1\r\n");

        return sb.toString();
    }

    public String makeArrayMessage(int length) {
        StringBuilder sb = new StringBuilder();
        sb.append("*" + length + "\r\n");

        return sb.toString();
    }

    public String makeBinaryMessage(String str) {
        int length = str.length();
        StringBuilder sb = new StringBuilder(length + 6);

        sb.append("$" + length + "\r\n");
        sb.append(str + "\r\n");

        return sb.toString();
    }

    /**
     * Exposed for clients to use
     * @param key
     * @param val
     * @return
     */
    // TO-DO: Move to a separate class and expose this as an interface
    public String makeSetMessage(String key, Object val) {
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
    // TO:DO:
    //public String makeSetMessage(String key, Object[] values) {}

    public String makeGetMessage(String key) {
        StringBuilder sb = new StringBuilder();

        sb.append(makeArrayMessage(2));
        sb.append(makeBinaryMessage("GET"));
        sb.append(sb.append(makeBinaryMessage(key)));

        return sb.toString();
    }

    public String makeDeleteMessage(String key) {
        StringBuilder sb = new StringBuilder();

        sb.append(makeArrayMessage(2));
        sb.append(makeBinaryMessage("DELETE"));
        sb.append(sb.append(makeBinaryMessage(key)));

        return sb.toString();
    }
}
