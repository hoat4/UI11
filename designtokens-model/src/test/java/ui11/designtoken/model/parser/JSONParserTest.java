package ui11.designtoken.model.parser;

import org.junit.Test;
import ui11.designtoken.model.parser.JSONParser.JSONSyntaxException;
import ui11.designtoken.model.parser.JSONParser.ValueType;

import java.io.IOException;

import static org.junit.Assert.*;

public class JSONParserTest {

    @Test
    public void testSimpleNumber() throws JSONSyntaxException, IOException {
        JSONParser p = new JSONParser("123");
        assertEquals(ValueType.NUMBER, p.nextValueType());
        assertEquals(123.0, p.readDouble(), 0);
    }

    @Test
    public void testArrays() throws JSONSyntaxException, IOException {
        JSONParser p = new JSONParser("[\n\n\t32, 0]");
        assertEquals(ValueType.ARRAY, p.nextValueType());
        assertTrue(p.hasMoreElements());
        assertEquals(ValueType.NUMBER, p.nextValueType());
        assertEquals(32.0, p.readDouble(), 0);
        assertTrue(p.hasMoreElements());
        assertEquals(ValueType.NUMBER, p.nextValueType());
        assertEquals(0.0, p.readDouble(), 0);
        assertFalse(p.hasMoreElements());
    }
}
