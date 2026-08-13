/**
 * TWO SUM
 * -------
 * Given an array of integers and a target, return the indices of the two
 * numbers that add up to the target.
 *
 * We show BOTH approaches so you can feel the optimization:
 *   1) Brute force  -> O(n^2) time, O(1) space
 *   2) Optimized    -> O(n)   time, O(n) space  (HashMap)
 *
 * TIP TO REMEMBER:
 *   Brute force asks "is my partner somewhere AHEAD of me?" (inner loop).
 *   Optimized asks   "did my partner already PASS by?"      (HashMap lookup).
 *   We trade O(n) memory to delete an entire loop.
 */

import java.util.HashMap;
import java.util.Map;

public class TwoSum {

    // ---------- 1) BRUTE FORCE: O(n^2) time, O(1) space ----------
    static int[] bruteForce(int[] nums, int target) {
        for (int i = 0; i < nums.length; i++) {
            for (int j = i + 1; j < nums.length; j++) {
                if (nums[i] + nums[j] == target) {
                    return new int[]{i, j};
                }
            }
        }
        return new int[]{-1, -1}; // not found
    }

    // ---------- 2) OPTIMIZED: O(n) time, O(n) space ----------
    static int[] optimized(int[] nums, int target) {
        Map<Integer, Integer> seen = new HashMap<>(); // value -> index
        for (int i = 0; i < nums.length; i++) {
            int need = target - nums[i];
            if (seen.containsKey(need)) {
                return new int[]{seen.get(need), i};
            }
            seen.put(nums[i], i);
        }
        return new int[]{-1, -1};
    }

    public static void main(String[] args) {
        int[] nums = {2, 7, 11, 15};
        int target = 9;

        int[] a = bruteForce(nums, target);
        int[] b = optimized(nums, target);

        System.out.println("Array : [2, 7, 11, 15], target = 9");
        System.out.printf("Brute force  -> indices [%d, %d]%n", a[0], a[1]);
        System.out.printf("Optimized    -> indices [%d, %d]%n", b[0], b[1]);
        System.out.println("Expected     -> indices [0, 1]  (2 + 7 = 9)");
    }

}