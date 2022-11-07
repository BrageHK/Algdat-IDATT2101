package KjørbarFil;

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
import java.util.concurrent.ExecutionException;


class DataCompressionApplication {
    public static void main(String[] args) throws IOException {
        /*
        Diverse.pdf virker ikke og enwik8 tar for lang tid. Alle de andre filene virker.
         */
        String filename = "diverse.lyx";
        //String filename = "diverse.txt";
        //String filename = "opg8-kompr.pdf";

        var input = new DataInputStream(new BufferedInputStream(new FileInputStream(filename)));
        var output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream("dekomprimert_" + filename)));

        long size = Files.size(Paths.get(filename));
        byte[] content = new byte[((int)size)];

        input.readFully(content);
        input.close();

        LempelZivData lempelZiv = new LempelZivData((short) 32565, (short) 255);
        byte[] compressed = lempelZiv.compressWithLempelZiv(content);

        byte[] decompressed = lempelZiv.decompressWithLempelZiv(compressed);

        int decompressedSize = lempelZiv.getByteArraySize(decompressed);

        byte[] decompressedOutput = Arrays.copyOf(decompressed, decompressedSize);
        output.write(decompressedOutput);

        output.close();

        System.out.println("File sizes:\nContent\tCompressed\tDecompressed:\tEqual file size:\n " +
                lempelZiv.getByteArraySize(content) + "\t" + lempelZiv.getByteArraySize(compressed) + "\t\t\t" +
                lempelZiv.getByteArraySize(decompressed) + "\t\t" +
                (lempelZiv.getByteArraySize(content) == lempelZiv.getByteArraySize(decompressed)));
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
    private final int SHORT_MAX_SIZE = 65535;
    private final int BYTE_MAX_SIZE = 256;

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

    private int getWindowSize() {
        return searchBufferLength + lookAheadBufferLength;
    }

    private int addToBufferWithPointer(byte[] buffer, int index, byte insertion) {
        if (buffer[index] != 0) {
            System.out.println("Not empty at index " + index + " inserting " + insertion);
        }
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

    public short twoBytesToShort(ByteBuffer bb, byte byteOne, byte byteTwo) {
        bb.clear();
        bb.put(byteOne);
        bb.put(byteTwo);
        short result = bb.getShort(0);
        bb.clear();
        return result;
    }

    public byte[] compressWithLempelZiv(byte[] content) {
        byte[] output = new byte[content.length];
        int outputPointer = 2;  // Skipping first byte as this will be the number to the next backwards-refrence.
        int contentPointer = 0;
        int lastReferenceIndex = 0;
        int thisReferenceIndex = 0;

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

                thisReferenceIndex = outputPointer;

                short nextReference = (short) (thisReferenceIndex - lastReferenceIndex);
                byte[] referenceByteArray = new byte[] {(byte) ((nextReference>>8)&0xFF), (byte) (nextReference&0xFF)};
                output[lastReferenceIndex] = referenceByteArray[0]; // Set the previous reference pointer.
                output[lastReferenceIndex + 1] = referenceByteArray[1]; // Set the previous reference pointer.


                byte[] arr=new byte[]{(byte)((backJump>>8)&0xFF),(byte)(backJump&0xFF)}; // Convert short to bytes
                outputPointer = addToBufferWithPointer(output, outputPointer, arr[0]);
                outputPointer = addToBufferWithPointer(output, outputPointer, arr[1]);



                outputPointer = addToBufferWithPointer(output, outputPointer, ((byte) length)); // length of match
                outputPointer = addToBufferWithPointer(output, outputPointer, match.getNonMatch()); // Non match
                // Trenger vi non-match? kanskje.

                lastReferenceIndex = outputPointer;

                outputPointer++; // Her skal plassen til neste referanse være
                outputPointer++; // Her skal andre byten til referansen være

            } else {
                outputPointer = addToBufferWithPointer(output, outputPointer, match.getNonMatch());
            }

            contentPointer = shiftSearchAndLookAhead(length + 1, content, contentPointer);
        }
        return output;

    }

    public byte[] decompressWithLempelZiv(byte[] compressedContent) {
        ByteBuffer bb = ByteBuffer.allocate(2);
        /*
        short contentLength = twoBytesToShort(bb, compressedContent[0], compressedContent[1]);
        while(twoBytesToShort(bb, compressedContent[contentLength], compressedContent[contentLength+1]) != 0) {

            contentLength += twoBytesToShort(bb, compressedContent[contentLength], compressedContent[contentLength+1]) + 5;
        }

        System.out.println("Output-lengde: " + contentLength);*/


        byte[] output = new byte[2000000000];

        int jumpIndex = twoBytesToShort(bb, compressedContent[0], compressedContent[1]);
        int outputPointer = 0;
        for (int compressedContentPointer = 2; compressedContentPointer < getByteArraySize(compressedContent); compressedContentPointer++) {
            if (compressedContentPointer == jumpIndex){

                int backJump = twoBytesToShort(bb, compressedContent[compressedContentPointer], compressedContent[compressedContentPointer + 1]);
                if (backJump < 0) {
                    backJump += SHORT_MAX_SIZE;
                }

                int length = compressedContent[compressedContentPointer + 2];

                if (length < 0) {
                    length += BYTE_MAX_SIZE;
                }

                byte nonMatch = compressedContent[compressedContentPointer + 3];// Get the non match of the match
                int nextJump = twoBytesToShort(bb, compressedContent[compressedContentPointer + 4], compressedContent[compressedContentPointer + 5]);
                if (nextJump < 0) {
                    nextJump += SHORT_MAX_SIZE;
                }
                jumpIndex += nextJump + 4;          // Sets the jumpIndex to the next reference point

                for (int i = 0; i < length; i++) {
                    outputPointer = addToBufferWithPointer(output, outputPointer, output[outputPointer - backJump]);
                }
                output[outputPointer] = nonMatch;
                outputPointer++;
                compressedContentPointer += 5;
            } else {
                outputPointer = addToBufferWithPointer(output, outputPointer, compressedContent[compressedContentPointer]);
            }
        }
        return Arrays.copyOf(output, getByteArraySize(output));
    }

    public int getByteArraySize(byte[] array) {
        int i = array.length;
        while (i-- > 0 && array[i] == 0) {}
        return i + 1;
    }

}