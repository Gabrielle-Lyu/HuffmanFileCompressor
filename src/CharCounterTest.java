import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import static org.junit.Assert.*;

public class CharCounterTest {
    private final CharCounter counter = new CharCounter();

    @Test
    public void testGetCount() throws IOException {
        InputStream ins = new ByteArrayInputStream("teststring".getBytes("UTF-8"));
        int actualSize = counter.countAll(ins);
        assertEquals(10, actualSize );
        assertEquals(3, counter.getCount('t'));
    }

    @Test
    public void testCountAll() throws IOException {
        InputStream ins = new ByteArrayInputStream("teststring".getBytes("UTF-8"));
        int actualSize = counter.countAll(ins);
        assertEquals(10, actualSize );
        assertEquals(3, counter.getCount('t'));
        assertEquals(1, counter.getCount('e'));
        assertEquals(2, counter.getCount('s'));
        assertEquals(1, counter.getCount('r'));
        assertEquals(1, counter.getCount('i'));
        assertEquals(1, counter.getCount('n'));
        assertEquals(1, counter.getCount('g'));
        assertEquals(1, counter.getCount(IHuffConstants.PSEUDO_EOF));
    }

    @Test
    public void add() throws IOException {
        InputStream ins = new ByteArrayInputStream("ttt".getBytes("UTF-8"));
        counter.countAll(ins);
        assertEquals(3, counter.getCount('t'));
        counter.add('t');
        assertEquals(4,counter.getCount('t'));
    }

    @Test
    public void set() {
        counter.set('a',5);
        assertEquals(5,counter.getCount('a'));
    }

    @Test
    public void clear() throws IOException {
        InputStream ins = new ByteArrayInputStream("abcabcbbb".getBytes("UTF-8"));
        assertEquals(9,counter.countAll(ins));
        assertEquals(5,counter.getCount('b'));
        counter.clear();
        assertEquals(0,counter.getCount('b'));
    }

    @Test
    public void getTable() throws IOException {
        InputStream ins = new ByteArrayInputStream("abcabcbbb".getBytes("UTF-8"));
        counter.countAll(ins);
        Map<Integer,Integer> map = counter.getTable();
        System.out.println(map);
        assertEquals(Integer.valueOf(5),map.get((int)'b'));
        assertEquals(Integer.valueOf(2),map.get((int)'a'));
        assertEquals(Integer.valueOf(2),map.get((int)'c'));
        assertEquals(Integer.valueOf(1),map.get(IHuffConstants.PSEUDO_EOF));
    }
}