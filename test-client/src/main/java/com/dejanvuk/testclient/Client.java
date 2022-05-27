package com.dejanvuk.testclient;

import com.dejanvuk.parser.Parser;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 6379)){
            DataInputStream in = new DataInputStream(socket.getInputStream());
            Parser parser = new Parser(in);
            OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");



        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
