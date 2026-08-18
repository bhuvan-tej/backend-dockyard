/**
 * CONTAINS DUPLICATE  (Q5)
 * ------------------------
 * Return true if any value appears at least twice. e.g. [1,2,3,1] -> true.
 *
 * APPROACH:
 *   1) Brute force  -> compare every pair.  O(n^2) time, O(1) space.
 *   2) Optimized    -> HashSet; if add() returns false, it's a duplicate.
 *                      O(n) time, O(n) space.
 *
 * HINT TO REMEMBER: "Have I seen this before?" -> HashSet.
 *   (Alt: sort first, then check neighbors -> O(n log n) time, O(1) space.)
 */
import java.util.HashSet;
import java.util.Set;

public class ContainsDuplicate {

    static boolean bruteForce(int[] nums) {
        for (int i = 0; i < nums.length; i++)
            for (int j = i + 1; j < nums.length; j++)
                if (nums[i] == nums[j]) return true;
        return false;
    }

    static boolean optimized(int[] nums) {
        Set<Integer> seen = new HashSet<>();
        for (int n : nums)
            if (!seen.add(n)) return true; // add() is false if already present
        return false;
    }

    public static void main(String[] args) {
        int[] nums = {1, 2, 3, 1};
        System.out.println("Array: [1, 2, 3, 1]");
        System.out.println("Brute force -> " + bruteForce(nums));
        System.out.println("Optimized   -> " + optimized(nums));
        System.out.println("Expected    -> true");
    }
}