package com.dejanvuk.parser;

import java.io.BufferedReader;
import java.io.Reader;


/*
https://redis.io/docs/reference/protocol-spec
Client sends requests in an array
Server also stores the requests in an array, even thoug there might be just one request
* */
public class Parser {
    private Reader reader;

    Parser(BufferedReader reader) {
        this.reader = reader;
    }

    public void encodeMsg() {

    }

    public Integer readInteger() {}

    public String readSimpleString() {}

    public String readBulkString() {}

    public Object[] readArray() {}

    public Object[] decodeMsg() {

    }
}
