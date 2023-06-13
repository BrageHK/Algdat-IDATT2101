import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;


class DataCompressionApplication {
    public static void main(String[] args) throws IOException {
        String filename = "diverse.pdf";
        var input = new DataInputStream(new BufferedInputStream(new FileInputStream(filename)));
        var output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream("komprimert_" + filename)));

        long size = Files.size(Paths.get(filename));
        byte[] content = new byte[((int)size)];

        input.readFully(content);
        input.close();
        
        LempelZivData lempelZiv = new LempelZivData((short) 32767, (short) 255);
        byte[] compressed = lempelZiv.compressWithLempelZiv(content);
        
        byte[] decompressed = lempelZiv.decompressWithLempelZiv(compressed);
        
        int decompressedSize = lempelZiv.getByteArraySize(decompressed);
        
        byte[] decompressedOutput = Arrays.copyOf(decompressed, decompressedSize);

        output.write(decompressedOutput);

        output.close();

        System.out.println("Compressed file: " + lempelZiv.getByteArrayAsString(compressed));
        
        System.out.println("File sizes:\nContent\tCompressed\tDecompressed:\tEqual file size:\n " + lempelZiv.getByteArraySize(content) + "\t" + lempelZiv.getByteArraySize(compressed) + "\t\t" + lempelZiv.getByteArraySize(decompressed) + "\t\t"+(lempelZiv.getByteArraySize(content) == lempelZiv.getByteArraySize(decompressed)));
    }
}

class Match {

    private final short backJump;
    private final int length;
    private final byte nonMatch;

    public Match(short backJump, int length, byte nonMatch) {
        this.backJump = backJump;
        this.length = length;
        this.nonMatch = nonMatch;
    }

    public short getBackJump() {
        return backJump;
    }

    public int getLength() {
        return length;
    }

    public byte getNonMatch() {
        return nonMatch;
    }

    @Override
    public String toString() {
        return "(" + backJump + ", " + length + ", " + ((char)nonMatch) + ")";
    }

}

class LempelZivData {

    private final int MIN_MATCH_SIZE = 4;
    private final int BYTES_IN_OFFSET = 2;
    private final int DELIMITER = 28;

    private final short lookAheadBufferLength;
    private final byte[] lookAheadBuffer;
    private final short searchBufferLength;
    private final byte[] searchBuffer;

    public LempelZivData(short searchBufferLength, short lookAheadBufferLength) {
        this.lookAheadBufferLength = lookAheadBufferLength;
        this.lookAheadBuffer = new byte[lookAheadBufferLength];
        this.searchBufferLength = searchBufferLength;
        this.searchBuffer = new byte[searchBufferLength];
    }

    public byte[] getLookAheadBuffer() {
        return lookAheadBuffer;
    }

    public byte[] getSearchBuffer() {
        return searchBuffer;
    }

    private int getWindowSize() {
        return searchBufferLength + lookAheadBufferLength;
    }

    private int addToBufferWithPointer(byte[] buffer, int index, byte insertion) {
        buffer[index] = insertion;
        return index + 1;
    }

    private byte getElementAcrossBuffers(int index) {
        if (index >= searchBufferLength)
            return lookAheadBuffer[index - searchBufferLength];
        return searchBuffer[index];
    }

    private void setElementAcrossBuffers(int index, byte element) {
        if (index >= searchBuffer.length) {
            lookAheadBuffer[index - searchBuffer.length] = element;
        } else {
            searchBuffer[index] = element;
        }
    }

    private Match getMatchInBuffers(byte[] content, int contentPointer) {
        int length = 0;
        short offset = 0;
        short lookAheadIndex = 0;
        int i = 0;

        for (i += lookAheadIndex; i < searchBufferLength; i++) {

            lookAheadIndex = 0;
            while (lookAheadIndex < lookAheadBufferLength && getElementAcrossBuffers(i + lookAheadIndex) == lookAheadBuffer[lookAheadIndex]) {
                lookAheadIndex++;
            }
            if (length <= lookAheadIndex && lookAheadIndex >= MIN_MATCH_SIZE) { // Changed tp less than or equal to to have the closest reference
                length = lookAheadIndex - 1; // changed from lookAheadIndex - 1
                offset = (short) (searchBufferLength - i);
            }

        }
        byte nonMatch;
        if (length < lookAheadBufferLength) {
            nonMatch = lookAheadBuffer[length];
        } else {
            nonMatch = content[contentPointer];
        }
        return new Match(offset, length, nonMatch);
    }

    private boolean isBufferEmpty(byte[] buffer) {
        for (byte bufferByte : buffer) {
            if (bufferByte != 0) {
                return false;
            }
        }
        return true;
    }

    public int shiftSearchAndLookAhead(int shifts, byte[] content, int contentPointer) {
        int windowSize = getWindowSize();
        for (int i = 0; i < windowSize - shifts; i++) {
            setElementAcrossBuffers(i, getElementAcrossBuffers(i + shifts));
        }

        for (int i = windowSize - shifts; i < windowSize; i++) {
            if (contentPointer < content.length) {
                setElementAcrossBuffers(i, content[contentPointer]);
                contentPointer++;
            }
            else {
                setElementAcrossBuffers(i, (byte) '\0');
            }
        }
        return contentPointer;
    }

    public byte[] compressWithLempelZiv(byte[] content) {
        byte[] output = new byte[content.length];
        int outputPointer = 0;
        int contentPointer = 0;

        for (int i = 0; i < lookAheadBufferLength && i < content.length; i++) {
            contentPointer = addToBufferWithPointer(lookAheadBuffer, contentPointer, content[i]);
        }

        while (!isBufferEmpty(lookAheadBuffer)) {
            Match match = getMatchInBuffers(content, contentPointer);
            int length = match.getLength();
            short backJump = match.getBackJump();

            if (length >= lookAheadBufferLength) 
                contentPointer++;

            if (length > 0) {
                outputPointer = addToBufferWithPointer(output, outputPointer, ((byte) DELIMITER));  // File delimiter
                outputPointer = addToBufferWithPointer(output, outputPointer, ((byte) DELIMITER));  // File delimiter

                byte[] arr=new byte[]{(byte)((backJump>>8)&0xFF),(byte)(backJump&0xFF)}; // Convert short to bytes
                outputPointer = addToBufferWithPointer(output, outputPointer, arr[0]);
                outputPointer = addToBufferWithPointer(output, outputPointer, arr[1]);

                outputPointer = addToBufferWithPointer(output, outputPointer, ((byte) length)); // length of match
                outputPointer = addToBufferWithPointer(output, outputPointer, match.getNonMatch()); // Non match
            } else {
                outputPointer = addToBufferWithPointer(output, outputPointer, match.getNonMatch());
            }

            contentPointer = shiftSearchAndLookAhead(length + 1, content, contentPointer);
        }

        return output;

    }

    public byte[] decompressWithLempelZiv(byte[] compressedContent) {
        int contentLength = compressedContent.length;
        for(int i = 0; i < compressedContent.length; i++) {
            if(compressedContent[i] == DELIMITER && compressedContent[i + 1] == DELIMITER) {
                int length = compressedContent[i + 4];
                if (length < 0) {length += 256;}
                contentLength += length;
                i += 5;
            }
        }

        byte[] output = new byte[contentLength];
        ByteBuffer bb = ByteBuffer.allocate(2);
        int outputPointer = 0;
        for (int compressedContentPointer = 0; compressedContentPointer < compressedContent.length; compressedContentPointer++) {

            if (compressedContent[compressedContentPointer] == DELIMITER && compressedContent[compressedContentPointer+1] == DELIMITER) {
                compressedContentPointer++;
                byte[] offset = new byte[2];
                offset[0] = compressedContent[compressedContentPointer + 1];    // Get the first byte of the offset
                offset[1] = compressedContent[compressedContentPointer + 2];    // Get the second byte of the offset
                bb.put((byte) offset[0]);
                bb.put((byte) offset[1]);
                short backJump = bb.getShort(0);
                bb.clear();
                int length = compressedContent[compressedContentPointer + 3];
                if (length < 0) {length += 256;}
                byte nonMatch = compressedContent[compressedContentPointer + 4];// Get the non match of the match

                for (int i = 0; i < length; i++) {
                    output[outputPointer] = output[outputPointer - backJump]; // KEY
                    outputPointer++;
                }
                output[outputPointer] = nonMatch;
                outputPointer++;
                compressedContentPointer += 4;
                
            } else {
                output[outputPointer] = compressedContent[compressedContentPointer];
                outputPointer++;
            }
        }
        return Arrays.copyOf(output, getByteArraySize(output) + 1);
    }

    public String getByteArrayAsString(byte[] array) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if(array[i] != 0) {
                sb.append(array[i]);
                sb.append(' ');
            }
        }
        return sb.toString();
    }

    public int getByteArraySize(byte[] array) {
        int i = array.length;
        while (i-- > 0 && array[i] == 0) {}
        return i + 1;
    }

}
