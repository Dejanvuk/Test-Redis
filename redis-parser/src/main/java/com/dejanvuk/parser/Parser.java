package com.dejanvuk.parser;

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

    Parser(DataInputStream in) {
        this.in = in;
    }

    /**
     * Encodes the message to the string format
     */
    // TO-DO: Needs testing
    public String encodeMsg(StringBuilder sb, Message message) {
        DataType dataType = message.dataType;

        StringBuilder sb = new StringBuilder();

        if(dataType == DataType.SIMPLE_STR) {

        }
        else if(dataType == DataType.ERROR) {

        }
        else if(dataType == DataType.INTEGER) {

        }
        else if(dataType == DataType.BULK_STR) {

        }
        else if(dataType == DataType.ARRAY) {

        }
        else {
            // return error
        }

    }

    /**
     * encodes the response back to the client
     * @param sb
     * @param messages
     * @return
     */
    public String encodeResponse(StringBuilder sb, List<Message> messages) {

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
            default:
                // invalid data type; return error

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

    /* ====================
    Methods to convert the message to string
    ====================
    */
    public String makeIntegerMessage(int nr) {
        StringBuilder sb = new StringBuilder();

        sb.append(':');
        sb.append(nr);
        sb.append('\r');
        sb.append('\n');

        return sb.toString();
    }

    public String makeSimpleStrMessage(String str) {
        StringBuilder sb = new StringBuilder();

        sb.append('+');
        sb.append(str);
        sb.append('\r');
        sb.append('\n');

        return sb.toString();
    }

    public String makeErrorMessage(String exception) {
        StringBuilder sb = new StringBuilder();

        sb.append('-');
        sb.append(exception);
        sb.append('\r');
        sb.append('\n');

        return sb.toString();
    }

    public String makeNullMessage() {
        StringBuilder sb = new StringBuilder();

        sb.append('-');
        sb.append(1);
        sb.append('\r');
        sb.append('\n');

        return sb.toString();
    }

    public String makeBinaryMessage(String str) {
        int length = str.length();
        StringBuilder sb = new StringBuilder(length + 6);

        sb.append('$');
        sb.append(length);
        sb.append('\r');
        sb.append('\n');
        sb.append(str);
        sb.append('\r');
        sb.append('\n');

        return sb.toString();
    }
}
