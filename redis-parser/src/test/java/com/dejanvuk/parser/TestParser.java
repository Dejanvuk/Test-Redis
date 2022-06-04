package com.dejanvuk.parser;

import com.dejanvuk.parser.types.DataType;
import com.dejanvuk.parser.types.MsgType;
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

        //System.out.println("=============== Start of Parser Tests Setup ===============\n");
    }

    /**
     * Testing responses that can be received back from the server by the client
     * Notice we don't use the Make methods to generate the String response as we want to isolate the readData function
     * @throws IOException
     */

    @Test
    public void readIntegerTest() throws IOException {
        String _source1 = "123456\r\n";
        InputStream d = new ByteArrayInputStream(_source1.getBytes(StandardCharsets.UTF_8));

        parser = new Parser(new DataInputStream(d));

        int nr = parser.readInteger();

        assertEquals(nr, 123456);
    }

    @Test
    public void readDataTestClient() throws IOException {
        // Client Read OK message from the server
        String _source1 = "*1\r\n+OK\r\n";
        InputStream d = new ByteArrayInputStream(_source1.getBytes(StandardCharsets.UTF_8));

        parser = new Parser(new DataInputStream(d));

        List<Message> messages = new ArrayList<>();
        parser.readData(messages);
        // validate
        List<Message> correctMessages = new ArrayList<>();
        correctMessages.add(new Message.MessageBuilder().setDataType(DataType.SIMPLE_STR).setData("OK").build());
        assertEquals(messages, correctMessages);


        String _source2 = "*2\r\n+OK\r\n:123456\r\n"; // Client Read OK with message response from Server, where the data is a single integer
        d = new ByteArrayInputStream(_source2.getBytes("UTF-8"));
        messages = new ArrayList<>();
        parser.setIn(new DataInputStream(d));
        parser.readData(messages);
        // validate
        correctMessages = new ArrayList<>();
        correctMessages.add(new Message.MessageBuilder().setDataType(DataType.SIMPLE_STR).setData("OK").build());
        correctMessages.add(new Message.MessageBuilder().setDataType(DataType.INTEGER).setData(123456).build());
        assertEquals(messages, correctMessages);


        String _source3 = "*1\r\n-GET ERROR Key not found!\r\n";  // Client read ERROR message from the Server
        d = new ByteArrayInputStream(_source3.getBytes("UTF-8"));
        messages = new ArrayList<>();
        parser.setIn(new DataInputStream(d));
        parser.readData(messages);
        // validate
        correctMessages = new ArrayList<>();
        correctMessages.add(new Message.MessageBuilder().setDataType(DataType.ERROR).setData("GET ERROR Key not found!").build());

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
        List<Message> correctMessages = new ArrayList<>();
        correctMessages.add(new Message.MessageBuilder().setMsgType(MsgType.SET).setDataType(DataType.BULK_STR).setLength(3).setData("SET").build());
        correctMessages.add(new Message.MessageBuilder().setDataType(DataType.BULK_STR).setData("abcd").setLength(4).build());
        correctMessages.add(new Message.MessageBuilder().setDataType(DataType.INTEGER).setData(123456).build());
        assertEquals(messages, correctMessages);

        String _source2 = "*3\r\n$3\r\nSET\r\n$4\r\nabcd\r\n$7\r\nmessage\r\n";  // tests a set string command
        in.reset();
        d = new ByteArrayInputStream(_source2.getBytes("UTF-8"));
        messages = new ArrayList<>();
        parser.setIn(new DataInputStream(d));
        parser.readData(messages);
        // validate
        correctMessages = new ArrayList<>();
        correctMessages.add(new Message.MessageBuilder().setMsgType(MsgType.SET).setDataType(DataType.BULK_STR).setLength(3).setData("SET").build());
        correctMessages.add(new Message.MessageBuilder().setDataType(DataType.BULK_STR).setData("abcd").setLength(4).build());
        correctMessages.add(new Message.MessageBuilder().setDataType(DataType.BULK_STR).setData("message").setLength(7).build());
        assertEquals(messages, correctMessages);


        String _source3 = "*5\r\n$3\r\nSET\r\n$4\r\nabcd\r\n:1\r\n:22\r\n:333\r\n"; // tests a set array of integers command
        in.reset();
        d = new ByteArrayInputStream(_source3.getBytes("UTF-8"));
        messages = new ArrayList<>();
        parser.setIn(new DataInputStream(d));
        parser.readData(messages);
        // validate
        correctMessages = new ArrayList<>();
        correctMessages.add(new Message.MessageBuilder().setMsgType(MsgType.SET).setDataType(DataType.BULK_STR).setLength(3).setData("SET").build());
        correctMessages.add(new Message.MessageBuilder().setDataType(DataType.BULK_STR).setData("abcd").setLength(4).build());
        correctMessages.add(new Message.MessageBuilder().setDataType(DataType.INTEGER).setData(1).build());
        correctMessages.add(new Message.MessageBuilder().setDataType(DataType.INTEGER).setData(22).build());
        correctMessages.add(new Message.MessageBuilder().setDataType(DataType.INTEGER).setData(333).build());
        assertEquals(messages, correctMessages);


        String _source4 = "*2\r\n$3\r\nGET\r\n$4\r\nabcd\r\n"; // tests a get command
        in.reset();
        d = new ByteArrayInputStream(_source4.getBytes("UTF-8"));
        messages = new ArrayList<>();
        parser.setIn(new DataInputStream(d));
        parser.readData(messages);
        // validate
        correctMessages = new ArrayList<>();
        correctMessages.add(new Message.MessageBuilder().setMsgType(MsgType.GET).setDataType(DataType.BULK_STR).setLength(3).setData("GET").build());
        correctMessages.add(new Message.MessageBuilder().setDataType(DataType.BULK_STR).setData("abcd").setLength(4).build());
        assertEquals(messages, correctMessages);


        String _source5 = "*2\r\n$6\r\nDELETE\r\n$4\r\nabcd\r\n"; // tests a delete command
        in.reset();
        d = new ByteArrayInputStream(_source5.getBytes("UTF-8"));
        messages = new ArrayList<>();
        parser.setIn(new DataInputStream(d));
        parser.readData(messages);
        // validate
        correctMessages = new ArrayList<>();
        correctMessages.add(new Message.MessageBuilder().setMsgType(MsgType.DELETE).setDataType(DataType.BULK_STR).setLength(6).setData("DELETE").build());
        correctMessages.add(new Message.MessageBuilder().setDataType(DataType.BULK_STR).setData("abcd").setLength(4).build());
        assertEquals(messages, correctMessages);


        String _source6 = "*3\r\n$6\r\nRENAME\r\n$6\r\noldKey\r\n$6\r\nnewKey\r\n"; // tests a rename command
        in.reset();
        d = new ByteArrayInputStream(_source6.getBytes("UTF-8"));
        messages = new ArrayList<>();
        parser.setIn(new DataInputStream(d));
        parser.readData(messages);
        // validate
        correctMessages = new ArrayList<>();
        correctMessages.add(new Message.MessageBuilder().setMsgType(MsgType.RENAME).setDataType(DataType.BULK_STR).setLength(6).setData("RENAME").build());
        correctMessages.add(new Message.MessageBuilder().setDataType(DataType.BULK_STR).setData("oldKey").setLength(6).build());
        correctMessages.add(new Message.MessageBuilder().setDataType(DataType.BULK_STR).setData("newKey").setLength(6).build());
        assertEquals(messages, correctMessages);
    }
}
