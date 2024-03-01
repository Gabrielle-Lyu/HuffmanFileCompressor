import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import static org.junit.Assert.*;

public class HuffTest {

    @Test
    public void testMakeHuffTree() throws IOException {
        Huff huff = new Huff();
        InputStream ins = new ByteArrayInputStream("teststring".getBytes("UTF-8"));
        HuffTree huffTree = huff.makeHuffTree(ins);
        assertEquals(11,huffTree.weight());
    }

    @Test
    public void testMakeTable() throws IOException {
        Huff huff = new Huff();
        InputStream ins = new ByteArrayInputStream("teststring".getBytes("UTF-8"));
        huff.makeHuffTree(ins);
        Map<Integer, String> map = huff.makeTable();
        System.out.println(map);
        assertNotNull(map);
        assertEquals("10", huff.getCode(115));
        assertEquals("0",huff.getCode(116));
        assertEquals("1110",huff.getCode(101));
        assertEquals("11110",huff.getCode(110));
        assertNotNull(huff.showCounts());
    }
}