import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class Huff implements ITreeMaker, IHuffEncoder, IHuffHeader, IHuffModel {
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
        while (queue.size() > 1) {
            IHuffBaseNode first = queue.poll();
            IHuffBaseNode second = queue.poll();
            IHuffBaseNode combined =
                    new HuffInternalNode(first,second,first.weight() + second.weight());
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

    private void traverse(IHuffBaseNode node, String value) {
        if (node == null) {
            return;
        }
        if (node.isLeaf()) {
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
        if (encodeMap.isEmpty()) {
            return "";
        }
        return encodeMap.get(i);
    }

    @Override
    public Map<Integer, Integer> showCounts() {
        return counter.getTable();
    }

    @Override
    public int headerSize() {
        return BITS_PER_INT + tree.size();
    }

    @Override
    public int writeHeader(BitOutputStream out) {
        out.write(BITS_PER_INT, MAGIC_NUMBER);
        preOrder(out, tree.root());
        return headerSize();
    }

    private void preOrder(BitOutputStream out, IHuffBaseNode root) {
        if (root == null) {
            return;
        }
        if (root.isLeaf()) {
            out.write(1,1);
            out.write(BITS_PER_WORD + 1, ((HuffLeafNode)root).element());
        } else {
            out.write(1,0);
            preOrder(out, ((HuffInternalNode) root).left());
            preOrder(out, ((HuffInternalNode) root).right());
        }
    }

    @Override
    public HuffTree readHeader(BitInputStream in) throws IOException {
        int magic = in.read(BITS_PER_INT);
        if (magic != MAGIC_NUMBER) {
            throw new IOException("magic number not right");
        }
        return magicTree(in);
    }

    private HuffTree magicTree(BitInputStream in) throws IOException {
        int bitRead = in.read(1);
        if (bitRead == -1) {
            throw new IOException("Finished reading the tree. ");
        }
        if ((bitRead & 1) == 1) {
            bitRead = in.read(BITS_PER_WORD + 1);
            return new HuffTree(bitRead, 0);
        }
        HuffTree left = magicTree(in);
        HuffTree right = magicTree(in);
        return new HuffTree(left.root(), right.root(), 0);
    }

    @Override
    public int write(String inFile, String outFile, boolean force) {
        int compressedSize = 0;
        try {
            FileInputStream fs = new FileInputStream(inFile);
            makeHuffTree(fs);
            makeTable();

            int originalSize = 0;
            compressedSize += this.headerSize() + getCode(PSEUDO_EOF).length();
            for (Integer key : showCounts().keySet()) {
                int freq = showCounts().get(key);
                int numBits = getCode(key).length();
                compressedSize += freq * numBits;
                originalSize += freq * BITS_PER_WORD;
            }

            if (compressedSize < originalSize || force) {
                FileInputStream fis = new FileInputStream(inFile);
                BitOutputStream bos = new BitOutputStream(outFile);
                writeHeader(bos);

                int bitRead = fis.read();
                while (bitRead != -1) {
                    String code = encodeMap.get(bitRead);
                    for (int i = 0; i < code.length(); i++) {
                        if (code.charAt(i) == '1') {
                            bos.write(1, 1);
                        } else {
                            bos.write(1, 0);
                        }
                    }
                    bitRead = fis.read();
                }

                String endOfFile = encodeMap.get(PSEUDO_EOF);
                for (int i = 0; i < endOfFile.length(); i++) {
                    if (endOfFile.charAt(i) == '1') {
                        bos.write(1, 1);
                    } else {
                        bos.write(1, 0);
                    }
                }
                fis.close();
                bos.close();
            }
            fs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return compressedSize;
    }

    @Override
    public int uncompress(String inFile, String outFile) {
        int fileSize = 0;
        try {
            BitInputStream fs = new BitInputStream(inFile);
            FileOutputStream out = new FileOutputStream(outFile);
            // Start writing out
            HuffTree tree = readHeader(fs);
            IHuffBaseNode node = tree.root();

            int inbit = fs.read(1);
            while (true) {
                //Return if its empty and check for everything else
                if (inbit == -1) {
                    return 0;
                }
                if ((inbit & 1) == 0) {
                    node = ((HuffInternalNode) node).left();
                } else {
                    node = ((HuffInternalNode) node).right();
                }
                if (node.isLeaf()) {
                    if (((HuffLeafNode) node).element() == PSEUDO_EOF) {
                        break;
                    } else {
                        out.write((char) ((HuffLeafNode) node).element());
                        fileSize += BITS_PER_WORD;
                        node = tree.root();
                    }

                }
                inbit = fs.read(1);
            }
            fs.close();
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileSize;

    }
}
