/**
 * MAXIMUM SUBARRAY SUM
 * --------------------
 * Find the largest sum of any CONTIGUOUS subarray.
 * e.g. [-2, 1, -3, 4, -1, 2, 1, -5, 4] -> 6  (from subarray [4, -1, 2, 1])
 *
 * Approaches:
 *   1) Brute force -> O(n^2) time, O(1) space  (try every subarray)
 *   2) Kadane's    -> O(n)   time, O(1) space  (one pass)
 *
 * TIP TO REMEMBER (Kadane's decision):
 *   At each element ask: "Is it better to EXTEND the running subarray,
 *   or START FRESH from here?"  If the running sum goes negative, it can
 *   only drag future sums down -> drop it and restart.
 */

public class MaxSubarraySum {

    // ---------- 1) BRUTE FORCE: O(n^2) time, O(1) space ----------
    static int bruteForce(int[] nums) {
        int best = Integer.MIN_VALUE;
        for (int i = 0; i < nums.length; i++) {
            int runningSum = 0;
            for (int j = i; j < nums.length; j++) {
                runningSum += nums[j];          // sum of subarray nums[i..j]
                best = Math.max(best, runningSum);
            }
        }
        return best;
    }

    // ---------- 2) KADANE'S ALGORITHM: O(n) time, O(1) space ----------
    static int kadane(int[] nums) {
        int best = nums[0];
        int current = nums[0];
        for (int i = 1; i < nums.length; i++) {
            // extend the previous subarray OR restart at nums[i]
            current = Math.max(nums[i], current + nums[i]);
            best = Math.max(best, current);
        }
        return best;
    }

    public static void main(String[] args) {
        int[] nums = {-2, 1, -3, 4, -1, 2, 1, -5, 4};

        System.out.println("Array: [-2, 1, -3, 4, -1, 2, 1, -5, 4]");
        System.out.println("Brute force -> " + bruteForce(nums));
        System.out.println("Kadane's    -> " + kadane(nums));
        System.out.println("Expected    -> 6  (subarray [4, -1, 2, 1])");
    }

}