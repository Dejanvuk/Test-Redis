package com.dejanvuk.parser.exceptions;

public class InvalidMsgException extends RuntimeException{
    public InvalidMsgException() {
    }

    public InvalidMsgException(String message) {
        super(message);
    }

    public InvalidMsgException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidMsgException(Throwable cause) {
        super(cause);
    }
}
