import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class CharCounter implements ICharCounter{
    private Map<Integer,Integer> frequencyTable;
    public CharCounter() {
        this.frequencyTable = new HashMap<>();

    }
    @Override
    public int getCount(int ch) {
        return frequencyTable.getOrDefault(ch,0);
    }

    @Override
    public int countAll(InputStream stream) throws IOException {
        clear();
        int currentChar;
        int count = 0;
        while ((currentChar = stream.read()) != -1){
            add(currentChar);
            count ++;
        }
        add(IHuffConstants.PSEUDO_EOF);
        return count;
    }

    @Override
    public void add(int i) {
        frequencyTable.put(i, frequencyTable.getOrDefault(i,0) + 1);
    }

    @Override
    public void set(int i, int value) {
        frequencyTable.put(i, value);
    }

    @Override
    public void clear() {
        frequencyTable.clear();
    }

    @Override
    public Map<Integer, Integer> getTable() {
        return frequencyTable;
    }
}
