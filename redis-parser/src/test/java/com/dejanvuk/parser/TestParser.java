package com.dejanvuk.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestParser {

    Parser parser = null;

    @BeforeEach
    public void setup() {
        System.out.println("=============== Start of Parser Tests Setup ===============\n");
    }

    // Notice we don't use the Make methods to generate the String response as we want to isolate the readData function
    @Test
    public void readDataTest() throws IOException {
        // Client Read OK message from the server
        String _source1 = "*1\r\n+OK\r\n";
        InputStream d = new ByteArrayInputStream(_source1.getBytes(StandardCharsets.UTF_8));
        DataInputStream in = new DataInputStream(d);

        parser = new Parser(in);

        List<Message> messages = new ArrayList<>();
        parser.readData(messages);


        // Client Read OK with message response from Server, where the data is a single integer
        String _source2 = "*2\r\n+OK\r\n:123456\r\n";
        in.reset();
        d = new ByteArrayInputStream(_source2.getBytes("UTF-8"));
        messages = new ArrayList<>();
        parser.readData(messages);


        // Client read ERROR message from the Server
        String _source3 = "*1\r\n-GET ERROR Key not found!\r\n";
        in.reset();
        d = new ByteArrayInputStream(_source2.getBytes("UTF-8"));
        messages = new ArrayList<>();
        parser.readData(messages);

        System.out.println("blabla");
    }
}
