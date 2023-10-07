import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;


public class Compress {

    public static void main(String[] args) {
        Compress cmp = new Compress();
         cmp.encode();
         cmp.decode();
    }

    public void encode() {
        Huffman hm = new Huffman();
        LempelZiv lz = new LempelZiv();
        File input = new File("diverse.lyx");
        File output = new File("callum.lyx");
        byte[] data = null;
        try (DataInputStream ifp = new DataInputStream(new BufferedInputStream(new FileInputStream(input)))) {
            try {
                data = ifp.readAllBytes();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                ifp.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        
        hm.huffEncode(data, data.length);

        try (DataOutputStream ofp = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(output)))) {
            for (int i = 0; i < 256; i++)
                try {
                    ofp.writeInt(hm.freqs[i]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            for (byte b : hm.bytes) {
                try {
                    ofp.writeByte(b);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                ofp.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // lz.lempEncode(data, data.length);

        // try (DataOutputStream ofp = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(output)))) {
        //     try {
        //         ofp.writeInt(lz.references.size());
        //     } catch (IOException e) {
        //         e.printStackTrace();
        //     }
        //     for (int i = 0; i < lz.references.size(); i++) {
        //         try {
        //             ofp.writeShort(lz.references.get(i));
        //         } catch (IOException e) {
        //             e.printStackTrace();
        //         }
        //     }
        //     for (byte b : lz.bytes) {
        //         try {
        //             ofp.writeByte(b);
        //         } catch (IOException e) {
        //             e.printStackTrace();
        //         }
        //     }
        //     try {
        //         ofp.close();
        //     } catch (IOException e) {
        //         e.printStackTrace();
        //     }
        // } catch (IOException e) {
        //     e.printStackTrace();
        // }
    }

    public void decode() {
        Huffman hm = new Huffman();
        LempelZiv lz = new LempelZiv();
        File input = new File("test.cmp");
        File output = new File("decoded.txt");
        byte[] data = null;
        try (DataInputStream ifp = new DataInputStream(new BufferedInputStream(new FileInputStream(input)))) {
            try {
                data = ifp.readAllBytes();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                ifp.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        
        hm.huffDecode(data, data.length);

        try (DataOutputStream ofp = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(output)))) {
            for (byte b : hm.bytes) {
                try {
                    ofp.writeByte(b);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                ofp.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // lz.lempDecode(data);

        // try (DataOutputStream ofp = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(output)))) {
        //     for (byte b : lz.bytes) {
        //         try {
        //             ofp.writeByte(b);
        //         } catch (IOException e) {
        //             e.printStackTrace();
        //         }
        //     }
        //     try {
        //         ofp.close();
        //     } catch (IOException e) {
        //         e.printStackTrace();
        //     }
        // } catch (IOException e) {
        //     e.printStackTrace();
        // }
    }

    class Huffman {
        ArrayList<Byte> bytes;
        ArrayList<int[]> bitSets;
        byte bitSetPos;
        int totalBitsets;
        int[] freqs;

        Huffman() {
            this.bytes = new ArrayList<>();
            this.bitSets = new ArrayList<>();
            for (int i = 0; i < 256; i++)
                bitSets.add(new int[256]);
            this.totalBitsets = 0;
            this.bitSetPos = 0;
            this.freqs = new int[256];
        }

        void huffEncode(byte[] data, int data_len) {
            HuffmanNode root;
            int[] code = new int[256];
            int curByte = 0;

            findFreqs(data);

            root = buildHuffTree();

            createHuffCodes(root, code, 0);

            for (int i = 0; i < data_len; i++) {
                int idx = data[i];
                if (idx < 0)
                    idx += 256;
                int[] curSet = bitSets.get(idx);
                int j = 0;
                while (curSet[j] == '1' || curSet[j] == '0') {
                    curByte <<= 1;
                    if (curSet[j] == '1')
                        curByte |= 1;
                    
                    bitSetPos++;

                    if (bitSetPos == 8) {
                        bytes.add((byte)curByte);
                        totalBitsets++;
                        bitSetPos = 0;
                        curByte = 0;
                    }
                    j++;
                }
            }

            while (bitSetPos > 0) {
                curByte <<= 1;

                bitSetPos++;

                if (bitSetPos == 8)
                    bytes.add((byte) curByte);
                    totalBitsets++;
            }
        }

        void huffDecode(byte[] data, int data_len) {
            HuffmanNode root;
            HuffmanNode curr;

            int curPos = 0;

            for (int i = 0; i < 256; i++) {
                byte[] tmp = new byte[4];
                for (int j = 0; j < 4; j++) {
                    tmp[j] = data[curPos++];
                }
                freqs[i] = ((0xFF & tmp[0]) << 24) | ((0xFF & tmp[1]) << 16) |
                        ((0xFF & tmp[2]) << 8) | (0xFF & tmp[3]);
            }
            
            root = buildHuffTree();

            int[][] bits = new int[data_len][8];

            curr = root;

            for (int i = 1024; i < data_len; i++) {
                for (int j = 7; j >= 0; j--) {
                    bits[i][j] = (data[i] & (1 << j)) != 0 ? '1' : '0';
                }
            }

            int i = 1024; int j = 7;
            while (i < data_len) {
                if (bits[i][j] == '1')
                    curr = curr.right;
                else
                    curr = curr.left;

                if (curr.left == null && curr.right == null) {
                    bytes.add(curr.c);
                    curr = root;
                }

                j--;
                if (j == -1) {
                    j = 7;
                    i++;
                }
            }
        }

        void findFreqs(byte[] data) {
            for (byte b : data) {
                int i = b;
                if (i < 0)
                    i += 256;
                freqs[i]++;
            }
        }

        void createHuffCodes(HuffmanNode node, int code[], int depth) {
            if (node == null)
                return;
            if (node.left == null && node.right == null) {
                int index = node.c;
                if (node.c < 0)
                    index += 256;
                this.bitSets.set(index, code);
                return;
            }
            int[] tmp1 = new int[256];
            int[] tmp2 = new int[256];
            for (int i = 0; i < 256; i++) {
                tmp1[i] = code[i];
                tmp2[i] = code[i];
            }
            tmp1[depth] = '0';
            createHuffCodes(node.left, tmp1, depth + 1);
            tmp2[depth] = '1';
            createHuffCodes(node.right, tmp2, depth + 1);
        }
        
        HuffmanNode buildHuffTree() {
            HuffmanNode tree = null;
            HuffmanNode right = null;
            HuffmanNode left = null;

            PriorityQueue<HuffmanNode> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(n -> n.freq));
            
            for (int i = 0; i < 256; i++) {
                if (freqs[i] == 0)
                    continue;
                priorityQueue.add(new HuffmanNode((byte)i, freqs[i]));
            }

            while (priorityQueue.size() > 1) {
                left = priorityQueue.remove();
                right = priorityQueue.remove();
                
                tree = new HuffmanNode(right, left);

                priorityQueue.add(tree);
            }
            
            return priorityQueue.remove();
        }
        
    }

    class HuffmanNode {
        byte c;
        int freq;
        HuffmanNode left = null;
        HuffmanNode right = null;
    
        public HuffmanNode(HuffmanNode left, HuffmanNode right) {
            this.left = left;
            this.right = right;
            freq = left.freq + right.freq;
        }
    
        public HuffmanNode(byte c, int freq) {
            this.c = c;
            this.freq = freq;
        }
    }

    class LempelZiv {
        ArrayList<Byte> bytes;
        ArrayList<Short> references;
        int rMax;
        int bMax;

        public LempelZiv() {
            bytes = new ArrayList<>();
            references = new ArrayList<>();
            rMax = 0;
        }

        void lempEncode(byte[] data, int data_len) {
            CircularBuffer circ = new CircularBuffer();
            ArrayList<Byte> tmp = new ArrayList<>();
            int i = 0;
            
            while (i < data_len) {
                short[] res = circ.findWord(data, i, data_len);
                if (res[0] != -1) {
                    references.add((short) tmp.size());
                    for (byte b : tmp)
                        bytes.add(b);
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
                        for (byte b : tmp)
                            bytes.add(b);
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
                references.add((short)tmp.size());
                for (byte b : tmp)
                    bytes.add(b);
            }
        }

        void lempDecode(byte[] data) {
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
                references.add((short)(((0xFF & tmp16[0]) << 8) | (0xFF & tmp16[1])));
            }
            
            int j = 0; int bufIdx = 0; int seekIdx = 0;

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

        }
    }

    class CircularBuffer {
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