/* Written by Nicolai H. Brand, Carl J. M. H. Gützkow, Eilert W. Hansen, Brage H. Kvamme */


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class LZHuff {
    public static void main(String[] args) {
        String operation;
        String inputPath;
        String outputPath;
        if (args.length != 3) {
            System.out.println("bad input");
            System.out.println("Usage: \njava LZHuff (encode | decode) <inputFile> <outputFile>");
            System.out.println("Example: \njava LZHuff encode the_industrial_revolution_and_its_consequences.pdf");
            System.exit(1);
        }
        operation = args[0];
        inputPath = args[1];
        outputPath = args[2];

        if (operation.equals("encode")) {
            LZHuff.encode(inputPath, outputPath);
            try {
                long uncompressedSize = Files.size(Path.of(inputPath));
                long compressedSize = Files.size(Path.of(outputPath));
                System.out.println("the compressed file is " + (double)(compressedSize*100/uncompressedSize) + "% of the original file");
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }

        }
        else if (operation.equals("decode")) LZHuff.decode(inputPath,outputPath);
        else {
            System.out.println("bad input");
            System.out.println("Usage: \njava LZHuff (encode | decode) <inputFile> <outputFile>");
            System.out.println("Example: \njava LZHuff encode the_industrial_revolution_and_its_consequences.pdf");
            System.exit(1);
        }

    }
    public static void encode(String inputPath, String outputPath) {
        lempelZiv LZ = new lempelZiv();
        try {
            DataInputStream file = new DataInputStream(new BufferedInputStream(new FileInputStream(inputPath)));
            byte[] input = file.readAllBytes();
            byte[] LZEncoded = LZ.lempEncode(input);
            Huffman huffman = new Huffman();
            huffman.encodeAndWriteToFile(LZEncoded, outputPath);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void decode(String inputPath, String outputPath) {
        Huffman huffman = new Huffman();
        lempelZiv LZ = new lempelZiv();
        try {
            byte[] huffmanDecoded = huffman.decode(inputPath);
            byte[] decompressed = LZ.lempDecode(huffmanDecoded);
            int decompressedSize = decompressed.length;
            byte[] decompressedOutput = Arrays.copyOf(decompressed, decompressedSize);
            var output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputPath)));
            output.write(decompressedOutput);
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}

class lempelZiv {
    ArrayList<Byte> bytes;
    ArrayList<Short> references;
    int rMax;
    int bMax;

    public lempelZiv() {
        bytes = new ArrayList<>();
        references = new ArrayList<>();
        rMax = 0;
    }

    public byte [] lempEncode(byte[] data) {
        int data_len = data.length;
        byte [] ret = null;
        CircularBuffer circ = new CircularBuffer();
        ArrayList<Byte> tmp = new ArrayList<>();
        int i = 0;

        while (i < data_len) {
            short[] res = circ.findWord(data, i, data_len);
            if (res[0] != -1) {
                references.add((short) tmp.size());
                bytes.addAll(tmp);
                references.add(res[0]);
                references.add(res[1]);
                for (int j = 0; j < res[1]; j++) {
                    circ.put(circ.buffer[res[0] + j]);
                    i++;
                }
                tmp = new ArrayList<>();
            } else {
                if (tmp.size() == Short.MAX_VALUE) {
                    references.add((short) tmp.size());
                    bytes.addAll(tmp);
                    references.add((short) 0);
                    references.add((short) 0);
                    tmp = new ArrayList<>();
                }
                circ.put(data[i]);
                tmp.add(data[i]);
                i++;
            }
        }
        if (tmp.size() > 0) {
            references.add((short) tmp.size());
            bytes.addAll(tmp);
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream ofp = new DataOutputStream(baos)) {
                ofp.writeInt(references.size());

            for (Short reference : references) {
                ofp.writeShort(reference);
            }
            for (byte b : bytes) {
                ofp.writeByte(b);
            }
            ret = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return ret;
    }

    public byte [] lempDecode(byte[] data) {
        CircularBuffer circ = new CircularBuffer();
        byte[] tmp16 = new byte[2];
        int curPos = 0;


        for (int i = 0; i < 4; i++) {
            rMax = (rMax << 8) + (data[curPos++] & 0xFF);
        }

        for (int i = 0; i < rMax; i++) {
            for (int j = 0; j < 2; j++) {
                tmp16[j] = data[curPos++];
            }
            references.add((short) (((0xFF & tmp16[0]) << 8) | (0xFF & tmp16[1])));
        }

        int j = 0;
        int bufIdx = 0;
        int seekIdx = 0;

        while (curPos < data.length) {
            for (int k = 0; k < references.get(j); k++) {
                bytes.add(data[curPos]);
                circ.put(data[curPos++]);
                if (curPos == data.length)
                    break;
            }

            j++;

            if (curPos == data.length)
                break;

            bufIdx = references.get(j++);
            seekIdx = references.get(j++);

            for (int k = 0; k < seekIdx; k++) {
                circ.put(circ.buffer[bufIdx + k]);
                bytes.add(circ.buffer[bufIdx + k]);
            }
        }
        byte [] ret = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            ret[i] = bytes.get(i);
        }
        return ret;
    }


    private static class CircularBuffer {
        byte[] buffer;
        short head;
        short tail;
        short max;
        boolean full;

        public CircularBuffer() {
            head = 0;
            tail = 0;
            full = false;
            max = Short.MAX_VALUE;
            buffer = new byte[Short.MAX_VALUE];
        }

        void put(byte data) {
            buffer[head] = data;
            if (full) {
                if (++tail == max) {
                    tail = 0;
                }
            }
            if (++head == max) {
                head = 0;
            }
            full = head == tail;
        }

        short[] findWord(byte[] data, int idx, int data_len) {
            short limit;
            short[] ret = new short[2];

            ret[0] = -1;
            ret[1] = 6;

            if (full) {
                limit = Short.MAX_VALUE;
            } else {
                limit = head;
            }

            for (short i = 0; i < limit; i++) {
                short j;
                for (j = 0; (j + i) < limit; j++) {
                    if (buffer[i + j] != data[idx + j] || idx + j == data_len - 1)
                        break;
                }

                if (j - 1 > ret[1]) {
                    ret[0] = (short) i;
                    ret[1] = (short) (j - 1);
                }
            }

            return ret;
        }
    }
}

class Huffman {

    private final int ASCII_MAX = 256;
    private final int BITSET_MAX_CAPACITY = Integer.MAX_VALUE / 8;
    private int bitSetPos = 0;
    private final HuffmanCode[] codes;
    private final LinkedList<BitSet> bitSets;
    private final int[] frequency;

    public Huffman() {
        codes = new HuffmanCode[ASCII_MAX];
        bitSets = new LinkedList<>();
        bitSets.add(new BitSet());
        frequency = new int[ASCII_MAX];
    }

    private void findFrequency(byte[] input) {
        for (byte b : input) {
            int index = b;
            /*
             * since java does not support unsigned bytes, we have to convert the negative bytes into valid indexes
             * in the frequency array
             */
            if (b < 0)
                index += ASCII_MAX;
            frequency[index]++;
        }
    }

    private HuffmanNode constructTree() {
        PriorityQueue<HuffmanNode> queue = new PriorityQueue<>(Comparator.comparingInt(a -> a.frequency));
        /* create all initials nodes */
        for (int ascii_value = 0; ascii_value < ASCII_MAX; ascii_value++) {
            if (frequency[ascii_value] == 0)
                continue;
            HuffmanNode node = new HuffmanNode((byte) ascii_value, frequency[ascii_value]);
            queue.add(node);
        }

        while (queue.size() > 1) {
            HuffmanNode left = queue.remove();
            HuffmanNode right = queue.remove();
            HuffmanNode daddy = new HuffmanNode(left, right);
            queue.add(daddy);
        }

        return queue.remove();
    }

    private void addLong(Long l, int nbits) {
        for (int i = 0; i < nbits; i++) {
            if (bitSetPos == Integer.MAX_VALUE) {
                bitSets.add(new BitSet());
                bitSetPos = 0;
            }

            /*
             * append n < 64 amount of bits from left to right into the BitSet.
             * example:
             * 0b001 + 0b011 -> 0b001011
             *
             * if we were to simply append the bits from right to left we would have gotten the reversed order in the
             * BitSet.
             * example:
             * 0b001 + 0b011 -> 0b100110 (WRONG)
             *
             * Bragefur's algorithm
             * Copyright (C) 2022 Nicolai H. Brand
             * License: Gnu General Public License v3
             * Copy of license: <https://www.gnu.org/licenses/>
             */
            if (i % 8 == 0 && i != 0)
                l = l << 8;
            bitSets.get(bitSets.size() - 1).set(bitSetPos, (l & (0b1L << nbits - (i % 8) - 1)) != 0);
            bitSetPos++;
        }
    }

    private void getHuffmanCodes(HuffmanNode node, long l, int depth) {
        if (node == null)
            return;

        if (node.left == null && node.right == null) {
            int index = node.c;
            if (node.c < 0)
                index += ASCII_MAX;
            codes[index] = new HuffmanCode(l, depth);
            return;
        }

        l = l << 1;
        getHuffmanCodes(node.left, l, depth + 1);
        getHuffmanCodes(node.right, l | 0b1L, depth + 1);
    }

    public void encode(byte[] input) {
        findFrequency(input);
        HuffmanNode root = constructTree();
        getHuffmanCodes(root, 0, 0);

        /* traverse through all bytes in the input file and add its huffman code to the final output */
        for (byte b : input) {
            int index = b;
            /*
             * since java does not support unsigned bytes, we have to convert the negative bytes into valid indexes
             * in the frequency array
             */
            if (b < 0)
                index += ASCII_MAX;
            HuffmanCode hc = codes[index];
            addLong(hc.code, hc.nbits);
        }
    }

    public void writeToFile(String file_path, int inputLength){
        File out_file = new File(file_path);
        try ( DataOutputStream fp = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(out_file)))){
            /* write frequency table to file */
            for (int i = 0; i < ASCII_MAX; i++)
                fp.writeInt(frequency[i]);

            /* write the total amount of bytes in the file */
            fp.writeInt(inputLength);

            /* write the huffman encoded file */
            for (BitSet bitSet : bitSets) {
                byte[] arr = bitSet.toByteArray();
                fp.write(arr, 0, arr.length);
            }
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void encodeAndWriteToFile(byte[] input, String file_path) {
        encode(input);
        writeToFile(file_path, input.length);
    }

    public byte[] decode(String filePath) {
        byte [] ret = null;
        try ( DataInputStream s = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)))){
            for (int i = 0; i < ASCII_MAX; i++)
                frequency[i] = s.readInt();

            ret = new byte[s.readInt()];
            int retIndex = 0;
            BitSet bitSet = BitSet.valueOf(s.readNBytes(BITSET_MAX_CAPACITY));
            int bitSetIndex = 0;

            HuffmanNode root = constructTree();
            HuffmanNode currenNode = root;

            while (retIndex < ret.length) {
                while (!(currenNode.left == null && currenNode.right == null)) {
                    if (bitSet.get(bitSetIndex))
                        currenNode = currenNode.right;
                    else
                        currenNode = currenNode.left;
                    bitSetIndex++;

                    if (bitSetIndex == Integer.MAX_VALUE) {
                        bitSet = BitSet.valueOf(s.readNBytes(BITSET_MAX_CAPACITY));
                        bitSetIndex = 0;
                    }

                }

                ret[retIndex] = currenNode.c;
                retIndex++;
                currenNode = root;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return ret;
    }


    private static class HuffmanNode {
        byte c;
        int frequency;
        HuffmanNode left = null;
        HuffmanNode right = null;

        public HuffmanNode(HuffmanNode left, HuffmanNode right) {
            this.left = left;
            this.right = right;
            frequency = left.frequency + right.frequency;
        }

        public HuffmanNode(byte c, int frequency) {
            this.c = c;
            this.frequency = frequency;
        }
    }

    private static class HuffmanCode {
        long code;

        int nbits;

        public HuffmanCode(long l, int nbits) {
            this.code = l;
            this.nbits = nbits;
        }
    }
}

