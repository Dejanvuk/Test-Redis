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

    public void readInteger(List<Message> messages) throws IOException {
        int number = readInteger();
        Object[] data = new Object[1];
        data[0] = number;
        Message message = new Message.MessageBuilder().setDataType(DataType.INTEGER).setData(data).build();
        messages.add(message);
    }

    public void readSimpleString(List<Message> messages) throws IOException {
        StringBuilder sb = new StringBuilder();

        char ch = (char) in.read();

        while(ch != '\r') {
            sb.append(ch);
            ch = (char) in.read();
        }
        in.skip(1); // skip CLRF, only \n left to skip

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

        char ch = (char) in.read();

        while(ch != '\r') {
            sb.append(ch);
            ch = (char) in.read();
        }
        in.skip(1); // skip CLRF, only \n left to skip

        Object[] data = new Object[1];
        data[0] = sb.toString();
        Message message = new Message.MessageBuilder().setDataType(DataType.ERROR).setData(data).build();
        messages.add(message);
    }

    public void readBulkString(List<Message> messages) throws IOException{
        int length = readInteger(); // read the length of the bulk string
        char[] str = new char[length];

        for(int i = 0; i < length; i++) {
            str[i] = (char) in.read();
        }
        in.skip(2); // skip CLRF

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

        char ch = (char) in.read();

        while(ch != '\r') {
            int digit = ch - '0';
            result *= 10;
            result += digit;
            ch = (char) in.read();
        }

        in.skip(1); // skip CLRF, only \n left to skip

        return result;
    }
}
