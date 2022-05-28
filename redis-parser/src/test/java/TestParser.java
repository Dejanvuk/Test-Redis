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
    public void setTest() {
        String setIntegerTest1 = parser.makeSetMessage("abcd", 123456);
        assertEquals(setIntegerTest1, "*3\r\n$3\r\nSET\r\n$4\r\nabcd\r\n:123456\r\n");

        String setStringTest1 = parser.makeSetMessage("abcd", "message");
        assertEquals(setStringTest1, "*3\r\n$3\r\nSET\r\n$4\r\nabcd\r\n$7\r\nmessage\r\n");

        Object[] arr= new Object[3];


    }
}
