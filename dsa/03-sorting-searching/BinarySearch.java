/**
 * BINARY SEARCH  (Q1)
 * -------------------
 * Given a SORTED array of integers and a target, return the index of the
 * target, or -1 if it is not present.
 * e.g. [-1,0,3,5,9,12], target = 9 -> index 4.
 *
 * Approaches:
 *   1) Brute force -> scan every element left to right: O(n) time, O(1) space
 *   2) Optimized   -> discard half the range each step: O(log n) time, O(1) space
 *
 * TIP TO REMEMBER:
 *   Looking up a name in a phone book. You don't read page 1 onward — you open
 *   the middle, decide "earlier or later", and throw away half the book.
 *   Sorted input is the permission slip that lets you throw half away.
 */

public class BinarySearch {

    // ---------- 1) BRUTE FORCE: O(n) time, O(1) space ----------
    static int bruteForce(int[] nums, int target) {
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] == target) {
                return i;
            }
        }
        return -1;
    }

    // ---------- 2) OPTIMIZED: O(log n) time, O(1) space ----------
    static int optimized(int[] nums, int target) {
        int lo = 0;
        int hi = nums.length - 1; // closed range [lo, hi]

        while (lo <= hi) {
            // lo + (hi - lo) / 2 instead of (lo + hi) / 2:
            // the latter can overflow int when lo and hi are both huge.
            int mid = lo + (hi - lo) / 2;

            if (nums[mid] == target) {
                return mid;
            } else if (nums[mid] < target) {
                lo = mid + 1; // target must be in the right half
            } else {
                hi = mid - 1; // target must be in the left half
            }
        }
        return -1; // range collapsed without a hit
    }

    public static void main(String[] args) {
        int[] nums = {-1, 0, 3, 5, 9, 12};
        int target = 9;

        System.out.println("Array  : [-1, 0, 3, 5, 9, 12], target = 9");
        System.out.println("Brute force  -> index " + bruteForce(nums, target));
        System.out.println("Optimized    -> index " + optimized(nums, target));
        System.out.println("Expected     -> index 4");

        int missing = 2;
        System.out.println();
        System.out.println("Missing target = 2");
        System.out.println("Brute force  -> index " + bruteForce(nums, missing));
        System.out.println("Optimized    -> index " + optimized(nums, missing));
        System.out.println("Expected     -> index -1");
    }

}