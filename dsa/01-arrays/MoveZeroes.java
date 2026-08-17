/**
 * MOVE ZEROES
 * -----------
 * Move all 0's to the END of the array while keeping the relative order of
 * the non-zero elements. Must be done IN PLACE.
 * e.g. [0, 1, 0, 3, 12] -> [1, 3, 12, 0, 0]
 *
 * Approaches:
 *   1) Brute force  -> O(n) time, O(n) space  (build a new array, copy back)
 *   2) Two pointers -> O(n) time, O(1) space  (in place)
 *
 * TIP TO REMEMBER (slow/fast two pointers):
 *   One pointer (fast, "i") scans every element.
 *   The other (slow, "insertPos") marks WHERE the next non-zero belongs.
 *   Copy non-zeros forward, then fill the rest with zeros.
 */

import java.util.Arrays;

public class MoveZeroes {

    // ---------- 1) BRUTE FORCE: O(n) time, O(n) space ----------
    static int[] bruteForce(int[] nums) {
        int[] result = new int[nums.length];
        int pos = 0;
        for (int num : nums) {          // copy non-zeros first
            if (num != 0) result[pos++] = num;
        }
        // remaining slots are already 0 (Java default) -> done
        return result;
    }

    // ---------- 2) TWO POINTERS: O(n) time, O(1) space (in place) ----------
    static void twoPointers(int[] nums) {
        int insertPos = 0;
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] != 0) {
                nums[insertPos++] = nums[i];
            }
        }
        while (insertPos < nums.length) {
            nums[insertPos++] = 0;
        }
    }

    public static void main(String[] args) {
        int[] original = {0, 1, 0, 3, 12};

        System.out.println("Original     -> " + Arrays.toString(original));
        System.out.println("Brute force  -> " + Arrays.toString(bruteForce(original)));

        int[] copy = original.clone();
        twoPointers(copy);
        System.out.println("Two pointers -> " + Arrays.toString(copy));
        System.out.println("Expected     -> [1, 3, 12, 0, 0]");
    }

}