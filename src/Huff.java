import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class Huff implements ITreeMaker, IHuffEncoder{
    private final CharCounter counter = new CharCounter();
    private HuffTree tree;
    private final Map<Integer, String> encodeMap = new HashMap<>();
    @Override
    public HuffTree makeHuffTree(InputStream stream) throws IOException {
        counter.countAll(stream);
        Map<Integer, Integer> frequencyMap = counter.getTable();
        PriorityQueue<IHuffBaseNode> queue = new PriorityQueue<>();

//        Make the entry set as nodes and add to priority queue
        for (Map.Entry<Integer, Integer> entry : frequencyMap.entrySet()) {
            HuffLeafNode node = new HuffLeafNode(entry.getKey(), entry.getValue());
            queue.add(node);
        }

//        make Huffman tree
        while (queue.size() > 1){
            IHuffBaseNode first = queue.poll();
            IHuffBaseNode second = queue.poll();
            IHuffBaseNode combined = new HuffInternalNode(first,second,first.weight() + second.weight());
            queue.add(combined);
        }

        HuffInternalNode root = (HuffInternalNode) queue.poll();
        assert root != null;
        tree = new HuffTree(root.left(), root.right(), root.weight());
        return tree;
    }

    @Override
    public Map<Integer, String> makeTable() {
        IHuffBaseNode node = tree.root();
        traverse(node, "");
        return encodeMap;
    }

    private void traverse(IHuffBaseNode node, String value){
        if (node == null){
            return;
        }
        if (node.isLeaf()){
            HuffLeafNode leaf = (HuffLeafNode) node;
            encodeMap.put(leaf.element(),value);
        } else {
            HuffInternalNode internal = (HuffInternalNode) node;
            traverse(internal.left(), value + "0");
            traverse(internal.right(), value + "1");
        }
    }

    @Override
    public String getCode(int i) {
        if (encodeMap.isEmpty()){
            return "";
        }
        return encodeMap.get(i);
    }

    @Override
    public Map<Integer, Integer> showCounts() {
        return counter.getTable();
    }
}
