import com.dejanvuk.parser.MakeCommandUtility;
import com.dejanvuk.parser.Parser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

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

}
