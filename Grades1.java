import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Grades {

    static double average = 0;
    // DEFINE OTHER GLOBAL VARIABLES

    static final BoundedRandomGenerator random = new BoundedRandomGenerator();

    private static final int ARRAY_LENGTH = 10000000;

    private static final int NUM_THREADS = 10;

    // TODO: Define sychronization elements
    static long globalSum = 0;
    static Lock lock;
    static Lock lock1;

    static int zeros = 0;


    static void init() {
        lock = new ReentrantLock();  //inicijalno totalSum e 0 na pocetok
        globalSum = 0;
        average = 0;
        lock1 = new ReentrantLock();
    }

    // DO NOT CHANGE
    public static int[] getSubArray(int[] array, int start, int end) {
        return Arrays.copyOfRange(array, start, end);
    }

    public static void main(String[] args) {

        init();

        int[] arr = ArrayGenerator.generate(ARRAY_LENGTH);

        Thread[] threads = new Thread[NUM_THREADS];
        int part = ARRAY_LENGTH / NUM_THREADS;

        for (int i = 0; i < NUM_THREADS; i++) {
            int start = i * part;
            int end = (i == NUM_THREADS - 1) ? ARRAY_LENGTH : start + part;
            threads[i] = new CalculateThread(arr, start, end);
        }

        int numZeros = 100;
        Random random = new Random();

        for (int i = 0; i < numZeros; i++) {
            int randomInx = random.nextInt(arr.length);
            arr[randomInx] = 0;
        }

        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i].start();
        }


        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        average = (double) globalSum / ARRAY_LENGTH;

        // DO NOT CHANGE

        System.out.println("Your calculated average grade is: " + average);
        System.out.println("The actual average grade is: " + ArrayGenerator.actualAvg);
        System.out.println("Number of zeros: " + zeros);

        SynchronizationChecker.checkResult();

    }


    // TO DO: Make the CalculateThread class a thread, you can add methods and attributes
    static class CalculateThread extends Thread {

        private int[] arr;
        int startSearch;
        int endSearch;

        public CalculateThread(int[] arr, int startSearch, int endSearch) {
            this.arr = arr;
            this.startSearch = startSearch;
            this.endSearch = endSearch;
        }

        @Override
        public void run() {
            long localSum = 0;
            for (int i = startSearch; i < endSearch; i++) {
                if (arr[i] == 0) {
                    lock1.lock();
                    zeros++;
                    lock1.unlock();
                    continue;
                }
                localSum += arr[i];
            }

            lock.lock();
            try {
                globalSum += localSum;
            } finally {
                lock.unlock();
            }
        }


        public Double calculateAverageGrade() {
            return Arrays.stream(arr).average().getAsDouble();
        }


        public void calculateAverageGradeParallel() {

        }
    }

    /******************************************************
     // DO NOT CHANGE THE CODE BELOW TO THE END OF THE FILE
     *******************************************************/

    static class BoundedRandomGenerator {
        static final Random random = new Random();
        static final int RANDOM_BOUND_UPPER = 10;
        static final int RANDOM_BOUND_LOWER = 6;

        public int nextInt() {
            return random.nextInt(RANDOM_BOUND_UPPER - RANDOM_BOUND_LOWER) + RANDOM_BOUND_LOWER;
        }

    }

    static class ArrayGenerator {

        private static double actualAvg = 0;

        static int[] generate(int length) {
            int[] array = new int[length];

            for (int i = 0; i < length; i++) {
                int grade = Grades.random.nextInt();
                actualAvg += grade;
                array[i] = grade;
            }

            actualAvg /= array.length;

            return array;
        }
    }

    static class SynchronizationChecker {
        public static void checkResult() {
            if (ArrayGenerator.actualAvg != average) {
                throw new RuntimeException("The calculated result is not equal to the actual average grade!");
            }
        }
    }
}



