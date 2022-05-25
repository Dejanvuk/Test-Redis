package com.dejanvuk.parser;

import com.dejanvuk.parser.types.DataType;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.List;


/*
https://redis.io/docs/reference/protocol-spec
Client sends requests in an array
Server also stores the requests in an array, even thoug there might be just one request
* */
public class Parser {
    private DataInputStream in;

    Parser(DataInputStream in) {
        this.in = in;
    }

    /**
     * Encodes the message back to the string format
     */
    // TO-DO: Needs testing
    public void encodeMsg() {

    }

    /**
     * SET,DELETE messages it will send back an empty OK
     * GET message will send back and OK alongside the encoded data as string
     * @param data
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
    public String createOkMessage(List<Message> data) {
        StringBuilder sb = new StringBuilder();
        // Encode the data first
        if(data != null) {

        }

        return sb.toString();
    }

    /**
     *  The messages can throw errors during processing
     *  Send it back to the client alongside the exception
     * @return
     *
     * ERROR message with the exception
     * S: *2\r\n
     * S: +ERROR\r\n
     * S: ${nr of bytes of the string}\r\n
     * S: {exception as string}\r\n
     */
    public String createErrorMessage() {
        StringBuilder sb = new StringBuilder();

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
        Message message = new Message.MessageBuilder().setDataType(DataType.ARRAY).setLength(length).build();
        messages.add(message);
    }

    public void readData(List<Message> messages) throws IOException {
        int readByte = in.readUnsignedByte();

        /* read() is blocking anyway if the buffer is empty
        if(readByte == -1) { // -1 if the end of the stream has been reached
            return;
        }
        */

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
        }

        readData(messages);
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
}
