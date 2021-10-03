import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class Application {

    private static class MergeSortTask extends RecursiveTask<int[]> {
        final int[] array;

        public MergeSortTask(int[] array) {
            this.array = array;
        }

        public int[] compute() {
            if (array.length == 1) {
                return array;
            }
            final int middle = array.length / 2;

            final MergeSortTask leftResult = new MergeSortTask(Arrays.copyOfRange(array, 0, middle));
            leftResult.fork();
            final MergeSortTask rightResult = new MergeSortTask(Arrays.copyOfRange(array, middle, array.length));
            rightResult.fork();

            return merge(leftResult.join(), rightResult.join());
        }
    }

    public static int[] merge(int[] left, int[] right) {
        final int[] result = new int[left.length + right.length];

        int result_index = 0;
        int left_index = 0;
        int right_index = 0;

        while (left_index < left.length && right_index < right.length) {
            result[result_index++] =
                    left[left_index] <= right[right_index] ? left[left_index++] : right[right_index++];
        }

        System.arraycopy(left, left_index, result, result_index, left.length - left_index);
        System.arraycopy(right, right_index, result, result_index, right.length - right_index);

        return result;
    }

    public static int[] mergeSort(int[] array) {
        if (array.length == 1) return array;
        final int middle = array.length / 2;
        final int[] leftSubArray = Arrays.copyOfRange(array, 0, middle);
        final int[] rightSubArray = Arrays.copyOfRange(array, middle, array.length);

        final int[] sortedLeft = mergeSort(leftSubArray);
        final int[] sortedRight = mergeSort(rightSubArray);

        return merge(sortedLeft, sortedRight);
    }

    public static void main(String[] args) {

        final int arraySize = 100000;
        final int iterations = 10;
        final int warmupIterations = 10000;

        int[] array = new int[arraySize];
        final Random random = new Random();
        for (int i = 0; i < arraySize; i++) {
            array[i] = random.nextInt();
        }

        final ForkJoinPool forkJoinPool = new ForkJoinPool(8);

        // firstly, warm up jvm
        for (int i = 0; i < warmupIterations; i++) {
            forkJoinPool.invoke(new MergeSortTask(array));
            mergeSort(array);
        }

        final long multithreadedSortStartTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            final long t1 = System.currentTimeMillis();
            forkJoinPool.invoke(new MergeSortTask(array));
            System.out.println("Multithreaded time: " + (System.currentTimeMillis() - t1));
        }
        final long multithreadedSortDuration = System.currentTimeMillis() - multithreadedSortStartTime;
        System.out.println("Multithreaded duration avg: " + ((double) multithreadedSortDuration / iterations));

        final long singlethreadedSortStartTime = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            final  long t1 = System.currentTimeMillis();
            mergeSort(array);
            System.out.println("Singlethreaded time: " + (System.currentTimeMillis() - t1));
        }
        final long singlethreadedSortDuration = System.currentTimeMillis() - singlethreadedSortStartTime;
        System.out.println("Singlethreaded duration avg: " + ((double) singlethreadedSortDuration / iterations));
    }
}