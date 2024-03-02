
import org.junit.Test;

import java.io.*;
import java.util.Map;

import static org.junit.Assert.*;

public class HuffTest {

    @Test
    public void testMakeHuffTree() throws IOException {
        Huff huff = new Huff();
        InputStream ins = new ByteArrayInputStream("teststring".getBytes("UTF-8"));
        HuffTree huffTree = huff.makeHuffTree(ins);
        assertEquals(10,huffTree.weight());
    }

    @Test
    public void testMakeTable() throws IOException {
        Huff huff = new Huff();
        InputStream ins = new ByteArrayInputStream("teststring".getBytes("UTF-8"));
        huff.makeHuffTree(ins);
        Map<Integer, String> map = huff.makeTable();
        assertNotNull(map);
        assertEquals("10", huff.getCode(115));
        assertEquals("0",huff.getCode(116));
        assertEquals("11110",huff.getCode(101));
        assertEquals("111111",huff.getCode(110));
        assertNotNull(huff.showCounts());
    }

    @Test
    public void testCompress() {
        Huff huff = new Huff();
        int res = huff.write("test.txt", "result.txt", true);
        int res2 = huff.uncompress("result.txt", "uncompressed.txt");
        assertEquals(193, res);
        assertEquals(104, res2);
    }

    @Test
    public void testWriteHeader() throws IOException {
        Huff huff = new Huff();
        try {
            BitInputStream bits = new BitInputStream(new FileInputStream("abc.txt"));
            huff.makeHuffTree(bits);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertEquals(64, huff.writeHeader(new BitOutputStream(out)));
        out.close();
    }


    @Test
    public void testWriteAndUncompress() {
        Huff huff = new Huff();
        huff.write("abc.txt", "abcCompressed.txt", true);
        int res = huff.uncompress("abcCompressed.txt", "abcUncompressed.txt");
        assertEquals(48, res);
    }

    @Test
    public void testWrite() throws IOException {
        Huff testHuff = new Huff();
        int actual = testHuff.write("abc.txt", "abcCompressed.txt",true);
        assertEquals(93, actual);
    }
}