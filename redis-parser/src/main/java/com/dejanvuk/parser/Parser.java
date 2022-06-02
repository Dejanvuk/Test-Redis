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

    public void readInteger(List<Message> messages) throws IOException {
        int number = readInteger();
        Integer data = number;
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

        String data = sb.toString();
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

        String data = sb.toString();
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

        String data = String.valueOf(str);

        Message message = new Message.MessageBuilder().setDataType(DataType.BULK_STR).setData(data).setLength(length).build();

        if(data.equals("GET")) {
            message.msgType = MsgType.GET;
        }
        else if(data.equals("SET")) {
            message.msgType = MsgType.SET;
        }
        else if(data.equals("DELETE")) {
            message.msgType = MsgType.DELETE;
        }
        else if(data.equals("RENAME")) {
            message.msgType = MsgType.RENAME;
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

    // TODO: Test this method
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
