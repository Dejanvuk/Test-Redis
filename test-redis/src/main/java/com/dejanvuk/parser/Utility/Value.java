package com.dejanvuk.parser.Utility;

import java.time.format.DateTimeFormatter;
import java.util.List;

// Wrapper for the Key's value, so we can add attributes to each key
public class Value {
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private List<Object> values = null;
    private MessageNodeList.MessageNode messageNode = null;

    public Value(List<Object> values) {
        this.values = values;
    }

    public Value(DateTimeFormatter dtf, List<Object> values) {
        this.dtf = dtf;
        this.values = values;
    }

    public DateTimeFormatter getDtf() {
        return dtf;
    }

    public void setDtf(DateTimeFormatter dtf) {
        this.dtf = dtf;
    }

    public List<Object> getValues() {
        return values;
    }

    public void setValues(List<Object> values) {
        this.values = values;
    }

    public MessageNodeList.MessageNode getMessageNode() {
        return messageNode;
    }

    public void setMessageNode(MessageNodeList.MessageNode messageNode) {
        this.messageNode = messageNode;
    }

    @Override
    public String toString() {
        return "Value{" +
                "dtf=" + dtf +
                ", value=" + values +
                '}';
    }
}
