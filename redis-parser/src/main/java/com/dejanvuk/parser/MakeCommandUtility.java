package com.dejanvuk.parser;

import com.dejanvuk.parser.types.DataType;

import java.util.List;

/**
 * Utility class to make commands
 */
public class MakeCommandUtility {
    public static String makeSetMessage(String key, Object val) {
        StringBuilder sb = new StringBuilder();
        sb.append(makeArrayMessage(3));
        sb.append(makeBinaryMessage("SET"));
        sb.append(makeBinaryMessage(key));
        if(val.getClass() == Integer.class) {
            sb.append(makeIntegerMessage((Integer) val));
        }
        else if(val.getClass() == String.class) {
            sb.append(makeBinaryMessage((String) val));
        }

        return sb.toString();
    }
    // TO-DO: Add support for array
    public static String makeSetMessage(String key, List<Object> values) {
        StringBuilder sb = new StringBuilder();
        sb.append(makeArrayMessage(3));
        sb.append(makeBinaryMessage("SET"));
        sb.append(makeBinaryMessage(key));
        for(int i = 0 ; i < values.size(); i++) {
            Object val = values.get(i);

            if(val.getClass() == Integer.class) {
                sb.append(makeIntegerMessage((Integer) val));
            }
            else if(val.getClass() == String.class) {
                sb.append(makeBinaryMessage((String) val));
            }
        }

        return sb.toString();
    }

    public static String makeGetMessage(String key) {
        StringBuilder sb = new StringBuilder();

        sb.append(makeArrayMessage(2));
        sb.append(makeBinaryMessage("GET"));
        sb.append(makeBinaryMessage(key));

        return sb.toString();
    }

    public static String makeDeleteMessage(String key) {
        StringBuilder sb = new StringBuilder();

        sb.append(makeArrayMessage(2));
        sb.append(makeBinaryMessage("DELETE"));
        sb.append(makeBinaryMessage(key));

        return sb.toString();
    }

    public static String makeRenameMessage(String oldKey, String newKey) {
        StringBuilder sb = new StringBuilder();

        sb.append(makeArrayMessage(3));
        sb.append(makeBinaryMessage("RENAME"));
        sb.append(makeBinaryMessage(oldKey));
        sb.append(makeBinaryMessage(newKey));

        return sb.toString();
    }

    /* ====================
    Methods to convert the message to string
    ====================
    */
    public static String makeIntegerMessage(int nr) {
        StringBuilder sb = new StringBuilder();

        sb.append(":" + nr + "\r\n");

        return sb.toString();
    }

    public static String makeSimpleStrMessage(String str) {
        StringBuilder sb = new StringBuilder();

        sb.append("+" + str + "\r\n");

        return sb.toString();
    }

    public static String makeOkMessage() {
        StringBuilder sb = new StringBuilder();

        sb.append(makeArrayMessage(1));
        sb.append(MakeCommandUtility.makeSimpleStrMessage("OK"));

        return sb.toString();
    }

    public static String makeOkMessageWithData(List<Object> values) {
        StringBuilder sb = new StringBuilder();

        sb.append(makeArrayMessage(1 + values.size())); // + 1 for OK simple string
        sb.append(MakeCommandUtility.makeSimpleStrMessage("OK"));

        for(int i = 0; i < values.size(); i++) {
            Object val = values.get(i);
            if(val.getClass() == Integer.class) {
                sb.append(MakeCommandUtility.makeIntegerMessage((Integer)val));
            }
            else if(val.getClass() == String.class) {
                sb.append(MakeCommandUtility.makeBinaryMessage((String) val));
            }
        }

        return sb.toString();
    }

    public static String makeErrorMessage(String error, String exception) {
        StringBuilder sb = new StringBuilder();

        sb.append(makeArrayMessage(1));
        sb.append("-" + error + " " + exception + "\r\n");

        return sb.toString();
    }

    public static String makeNullMessage() {
        StringBuilder sb = new StringBuilder();

        sb.append("$-1\r\n");

        return sb.toString();
    }

    public static String makeArrayMessage(int length) {
        StringBuilder sb = new StringBuilder();
        sb.append("*" + length + "\r\n");

        return sb.toString();
    }

    public static String makeBinaryMessage(String str) {
        int length = str.length();
        StringBuilder sb = new StringBuilder();

        sb.append("$" + length + "\r\n");
        sb.append(str + "\r\n");

        return sb.toString();
    }
}
