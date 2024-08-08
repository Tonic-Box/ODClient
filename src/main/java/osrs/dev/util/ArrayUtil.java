package osrs.dev.util;

import java.util.Map;

public class ArrayUtil
{
    public static boolean contains(int[] array, int value) {
        if (array == null) {
            return false;
        }
        for (int element : array) {
            if (element == value) {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(double[] array, double value) {
        if (array == null) {
            return false;
        }
        for (double element : array) {
            if (Double.compare(element, value) == 0) {
                return true;
            }
        }
        return false;
    }

    public static <T> boolean contains(T[] array, T value) {
        if (array == null) {
            return false;
        }
        for (T element : array) {
            if (element == value || (element != null && element.equals(value))) {
                return true;
            }
        }
        return false;
    }

    public static String getKeyWithLowestValue(Map<String, Integer> map) {
        Map.Entry<String, Integer> minEntry = null;

        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (minEntry == null || entry.getValue().compareTo(minEntry.getValue()) < 0) {
                minEntry = entry;
            }
        }

        return (minEntry != null) ? minEntry.getKey() : null;
    }
}