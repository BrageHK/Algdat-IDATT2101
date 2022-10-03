import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Random;

class HashTime {
    public static int[] createRandomNumbers(int size) {
        Random  rand = new Random();
        int[] numbers = new int[size];

        for(int i = 0; i < size; i++) {
            numbers[i] = rand.nextInt();
        }

        return numbers;
    }

    public static long addNumbers(int[] arr) {
        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
       int len = arr.length;
        Instant start = Instant.now();
        for (int i = 0; i < len; i++) {
            map.put(arr[i],arr[i]);
        }
        Instant end = Instant.now();
        return Duration.between(start, end).toMillis();
    }

    public static void main(String[] args) {
        int[] numbers = createRandomNumbers(10000000); // Creating 10 million numbers
        long time = addNumbers(numbers);
        System.out.println("\nTime: " + (double) time/1000 + " s.");
    }
}