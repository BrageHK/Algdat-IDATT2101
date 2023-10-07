import java.nio.ByteBuffer;

public class test {
    public static void main(String[] args) {
        System.out.println((byte) (300));

        ByteBuffer bb = ByteBuffer.allocate(2);

        bb.put((byte) 127);
        bb.put((byte) 255);
        System.out.println(bb.getShort(0));
        short value1 = bb.getShort(0);

        bb.clear();


        bb.put((byte) 62);
        bb.put((byte) -5);
        short value2 = bb.getShort(0);
        System.out.println(bb.getShort(0));
        System.out.println(value2+65536);
        bb.clear();


        /*
        lengdeTilNeste1
        lengdeTilNeste2
        back1
        back2
        lengde
        nonMatch
         */

    }
}