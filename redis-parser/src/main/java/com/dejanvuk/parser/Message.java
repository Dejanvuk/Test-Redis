package com.dejanvuk.parser;

import com.dejanvuk.parser.types.DataType;
import com.dejanvuk.parser.types.MsgType;

/**
 * Class that holds the decoded message
 */
public class Message {
    MsgType msgType;
    DataType dataType;
    int length; // only for binary and arrays
    Object[] data;

    public Message(MessageBuilder messageBuilder) {
        this.msgType = messageBuilder.msgType;
        this.dataType = messageBuilder.dataType;
        this.length = messageBuilder.length;
        this.data = messageBuilder.data;
    }

    public static class MessageBuilder {
        MsgType msgType = null;
        DataType dataType = null;
        int length = 0; // only for binary and arrays
        Object[] data  = null;

        public MessageBuilder setMsgType(MsgType msgType) {
            this.msgType = msgType;
            return this;
        }

        public MessageBuilder setDataType(DataType dataType) {
            this.dataType = dataType;
            return this;
        }

        public MessageBuilder setLength(int length) {
            this.length = length;
            return this;
        }

        public MessageBuilder setData(Object[] data) {
            this.data = data;
            return this;
        }

        public Message build() {
            Message message = new Message(this);
            return message;
        }
    }
}
