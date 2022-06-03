package com.dejanvuk.parser;

import com.dejanvuk.parser.types.DataType;
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

    /**
     * Testing responses that can be received back from the server by the client
     * Notice we don't use the Make methods to generate the String response as we want to isolate the readData function
     * @throws IOException
     */
    @Test
    public void readDataTestClient() throws IOException {
        // Client Read OK message from the server
        String _source1 = "*1\r\n+OK\r\n";
        InputStream d = new ByteArrayInputStream(_source1.getBytes(StandardCharsets.UTF_8));
        DataInputStream in = new DataInputStream(d);

        parser = new Parser(in);

        List<Message> messages = new ArrayList<>();
        parser.readData(messages);
        // validate
        List<Message> correctMessages = new ArrayList<>();
        correctMessages.add(new Message.MessageBuilder().setDataType(DataType.SIMPLE_STR).setData("OK").build());
        assertEquals(messages, correctMessages);

        String _source2 = "*2\r\n+OK\r\n:123456\r\n"; // Client Read OK with message response from Server, where the data is a single integer
        in.reset();
        d = new ByteArrayInputStream(_source2.getBytes("UTF-8"));
        messages = new ArrayList<>();
        parser.readData(messages);
        // validate


        String _source3 = "*1\r\n-GET ERROR Key not found!\r\n";  // Client read ERROR message from the Server
        in.reset();
        d = new ByteArrayInputStream(_source2.getBytes("UTF-8"));
        messages = new ArrayList<>();
        parser.readData(messages);
        // validate

    }

    /**
     * Testing commands that can be received by the server from a client
     * @throws IOException
     */
    @Test
    public void readDataTestServer() throws IOException {
        String _source1 = "*3\r\n$3\r\nSET\r\n$4\r\nabcd\r\n:123456\r\n"; // tests a set integer command
        InputStream d = new ByteArrayInputStream(_source1.getBytes(StandardCharsets.UTF_8));
        DataInputStream in = new DataInputStream(d);

        parser = new Parser(in);

        List<Message> messages = new ArrayList<>();
        parser.readData(messages);
        // validate

        String _source2 = "*3\r\n$3\r\nSET\r\n$4\r\nabcd\r\n$7\r\nmessage\r\n";  // tests a set string command
        in.reset();
        d = new ByteArrayInputStream(_source2.getBytes("UTF-8"));
        messages = new ArrayList<>();
        parser.readData(messages);
        // validate

        String _source3 = "*5\r\n$3\r\nSET\r\n$4\r\nabcd\r\n:1\r\n:22\r\n:333\r\n"; // tests a set array of integers command
        in.reset();
        d = new ByteArrayInputStream(_source3.getBytes("UTF-8"));
        messages = new ArrayList<>();
        parser.readData(messages);
        // validate

        String _source4 = "*2\r\n$3\r\nGET\r\n$3\r\nabc\r\n"; // tests a get command
        in.reset();
        d = new ByteArrayInputStream(_source4.getBytes("UTF-8"));
        messages = new ArrayList<>();
        parser.readData(messages);
        // validate

        String _source5 = "*2\r\n$6\r\nDELETE\r\n$3\r\nabc\r\n"; // tests a delete command
        in.reset();
        d = new ByteArrayInputStream(_source5.getBytes("UTF-8"));
        messages = new ArrayList<>();
        parser.readData(messages);
        // validate

        String _source6 = "*3\r\n$6\r\nRENAME\r\n$6\r\noldKey\r\n$6\r\nnewKey\r\n"; // tests a rename command
        in.reset();
        d = new ByteArrayInputStream(_source6.getBytes("UTF-8"));
        messages = new ArrayList<>();
        parser.readData(messages);
        // validate

        System.out.println("blabla");
    }
}
