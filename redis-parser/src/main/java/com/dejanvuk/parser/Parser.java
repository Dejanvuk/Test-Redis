package com.dejanvuk.parser;

import com.dejanvuk.parser.types.DataType;

import java.io.BufferedReader;
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
    private Reader in;

    Parser(BufferedReader in) {
        this.in = in;
    }

    /**
     * Encodes the message back to the string format
     */
    public void encodeMsg() {

    }

    //public Integer readInteger() {}

    public void readSimpleString(List<Message> messages) {

    }

    //public String readBulkString() {}

    public void readArray(List<Message> messages) throws IOException {
        int length = readInteger(); // read the length of the array
        in.skip(2); // skip CLRF \r\n
        Message message = new Message.MessageBuilder().setDataType(DataType.ARRAYS).setLength(length).build();
        messages.add(message);
    }

    public void readData(List<Message> messages) throws IOException {
        char dataType = (char)in.read(); // read the first byte of the reply

        switch (dataType) {
            case '+': // Simple Strings
                readSimpleString(messages);
                break;
            case '-': // Errors
                break;
            case ':': //  Integers
                break;
            case '$': // Bulk Strings
                break;
            case '*': // Arrays
                readArray(messages);
                break;
        }
    }

    public int readInteger() throws IOException {
        int result = 0;

        char ch = (char) in.read();

        while(ch != '\r') {
            int digit = ch - '0';
            result *= 10;
            result += digit;
            ch = (char) in.read();
        }

        return result;
    }
}
