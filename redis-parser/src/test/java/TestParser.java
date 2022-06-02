import com.dejanvuk.parser.MakeCommandUtility;
import com.dejanvuk.parser.Parser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class TestParser {
    Parser parser = null;

    @BeforeEach
    public void setup() {
        System.out.println("=============== Start of Parser Tests Setup ===============\n");
        parser = new Parser(null);
    }


    @Test
    public void makeSetMessageTest() {
        String setIntegerTest1 = MakeCommandUtility.makeSetMessage("abcd", 123456);
        assertEquals(setIntegerTest1, "*3\r\n$3\r\nSET\r\n$4\r\nabcd\r\n:123456\r\n");

        String setStringTest1 = MakeCommandUtility.makeSetMessage("abcd", "message");
        assertEquals(setStringTest1, "*3\r\n$3\r\nSET\r\n$4\r\nabcd\r\n$7\r\nmessage\r\n");

        Object[] arr= new Object[3];


        Integer[] integerArray = new Integer[] {1,22,333};
        List<Object> integerList = Arrays.asList(integerArray);
        String setiNTEGERTest2 = MakeCommandUtility.makeSetMessage("abcd", integerList);
        assertEquals(setiNTEGERTest2, "*5\r\n$3\r\nSET\r\n$4\r\nabcd\r\n:1\r\n:22\r\n:333\r\n");

    }

    @Test
    public void makeGetMessageTest() {
        String getTest1 = MakeCommandUtility.makeGetMessage("abc");
        assertEquals(getTest1, "*2\r\n$3\r\nGET\r\n$3\r\nabc\r\n");
    }

    @Test
    public void makeDeleteMessageTest() {
        String deleteTest1 = MakeCommandUtility.makeDeleteMessage("abc");
        assertEquals(deleteTest1, "*2\r\n$6\r\nDELETE\r\n$3\r\nabc\r\n");
    }

    @Test
    public void makeRenameMessageTest() {
        String renameTest1 = MakeCommandUtility.makeRenameMessage("oldKey","newKey");
        assertEquals(renameTest1, "*3\r\n$6\r\nRENAME\r\n$6\r\noldKey\r\n$6\r\nnewKey\r\n");
    }

    /* ====================
    Tests the methods to convert the message to string
    ====================
    */

    @Test
    public void makeIntegerMessageTest() {
        String integerMessage = MakeCommandUtility.makeIntegerMessage(123456);
        assertEquals(integerMessage, ":123456\r\n");
    }

    @Test
    public void makeSimpleStrMessageTest() {
        String simpleStrMessage = MakeCommandUtility.makeSimpleStrMessage("OK");
        assertEquals(simpleStrMessage, "+OK\r\n");
    }

    @Test
    public void makeOkMessageTest() {
        assertEquals(MakeCommandUtility.makeOkMessage(), "*1\r\n+OK\r\n");
    }

    @Test
    public void makeOkMessageWithDataTest() {
        Integer[] integerArray = new Integer[] {1,22,333};
        List<Object> integerList = Arrays.asList(integerArray);
        String okMessageWithData = MakeCommandUtility.makeOkMessageWithData(integerList);
        assertEquals(okMessageWithData, "*4\r\n+OK\r\n:1\r\n:22\r\n:333\r\n");
    }

    @Test
    public void makeErrorMessageTest() {
        String errorMessage = MakeCommandUtility.makeErrorMessage("GET ERROR", "Key not found!");
        assertEquals(errorMessage, "*1\r\n-GET ERROR Key not found!\r\n");
    }

    @Test
    public void makeArrayMessageTest() {
        String arrayMessage = MakeCommandUtility.makeArrayMessage(6);
        assertEquals(arrayMessage, "*6\r\n");
    }

    @Test
    public void makeBinaryMessageTest() {
        String makeBinaryMessage = MakeCommandUtility.makeBinaryMessage("Complex binary message");
        assertEquals(makeBinaryMessage, "$22\r\nComplex binary message\r\n");
    }
}
