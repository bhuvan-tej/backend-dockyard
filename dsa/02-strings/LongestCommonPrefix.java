/**
 * LONGEST COMMON PREFIX
 * ---------------------
 * Find the longest common prefix among an array of strings.
 * e.g. ["flower","flow","flight"] -> "fl"
 *
 * Approaches:
 *   1) Brute force -> compare char columns across all words: O(n * m)
 *   2) "Optimized" -> shrink a candidate prefix against each word: O(n * m)
 *      (same big-O; the vertical-scan version short-circuits earliest)
 *
 * TIP TO REMEMBER:
 *   Scan COLUMN by column (index 0, then 1, ...). Stop at the first column
 *   where any word ends or a character differs.
 */

public class LongestCommonPrefix {

    // ---------- VERTICAL SCAN: O(n * m) time, O(1) space ----------
    static String verticalScan(String[] strs) {
        if (strs == null || strs.length == 0) return "";
        for (int col = 0; col < strs[0].length(); col++) {
            char c = strs[0].charAt(col);
            for (int row = 1; row < strs.length; row++) {
                if (col == strs[row].length() || strs[row].charAt(col) != c) {
                    return strs[0].substring(0, col);
                }
            }
        }
        return strs[0];
    }

    // ---------- HORIZONTAL SCAN: shrink prefix against each word ----------
    static String horizontalScan(String[] strs) {
        if (strs == null || strs.length == 0) return "";
        String prefix = strs[0];
        for (int i = 1; i < strs.length; i++) {
            while (!strs[i].startsWith(prefix)) {
                prefix = prefix.substring(0, prefix.length() - 1);
                if (prefix.isEmpty()) return "";
            }
        }
        return prefix;
    }

    public static void main(String[] args) {
        String[] a = {"flower", "flow", "flight"};
        String[] b = {"dog", "racecar", "car"};

        System.out.println("Input        -> [flower, flow, flight]");
        System.out.println("Vertical     -> \"" + verticalScan(a) + "\"");
        System.out.println("Horizontal   -> \"" + horizontalScan(a) + "\"");
        System.out.println("Expected     -> \"fl\"");
        System.out.println();
        System.out.println("[dog, racecar, car] -> \"" + verticalScan(b) + "\" (expected \"\")");
    }
}