/**
 * LONGEST SUBSTRING WITHOUT REPEATING CHARACTERS
 * ----------------------------------------------
 * Find the length of the longest substring with all distinct characters.
 * e.g. "abcabcbb" -> 3 ("abc")
 *
 * Approaches:
 *   1) Brute force -> check every substring for uniqueness: O(n^2)/O(n^3)
 *   2) Optimized   -> sliding window + last-seen map: O(n) time
 *
 * TIP TO REMEMBER:
 *   Never re-scan. When a repeat appears, SLIDE the left edge just past the
 *   previous copy. The window always holds a valid (unique) substring.
 */

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LongestUniqueSubstring {

    // ---------- 1) BRUTE FORCE: O(n^2) time (with a HashSet check) ----------
    static int bruteForce(String s) {
        int n = s.length(), best = 0;
        for (int i = 0; i < n; i++) {
            Set<Character> seen = new HashSet<>();
            int j = i;
            while (j < n && !seen.contains(s.charAt(j))) {
                seen.add(s.charAt(j));
                j++;
            }
            best = Math.max(best, j - i);
        }
        return best;
    }

    // ---------- 2) OPTIMIZED: O(n) time, O(min(n, alphabet)) space ----------
    static int optimized(String s) {
        Map<Character, Integer> last = new HashMap<>(); // char -> last index seen
        int left = 0, best = 0;
        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            if (last.containsKey(c) && last.get(c) >= left) {
                left = last.get(c) + 1;      // jump past the duplicate
            }
            last.put(c, right);
            best = Math.max(best, right - left + 1);
        }
        return best;
    }

    public static void main(String[] args) {
        String a = "abcabcbb", b = "bbbbb", c = "pwwkew";

        System.out.println("Input        -> \"" + a + "\"");
        System.out.println("Brute force  -> " + bruteForce(a));
        System.out.println("Optimized    -> " + optimized(a));
        System.out.println("Expected     -> 3");
        System.out.println();
        System.out.println("\"" + b + "\" -> " + optimized(b) + " (expected 1)");
        System.out.println("\"" + c + "\" -> " + optimized(c) + " (expected 3, \"wke\")");
    }
}

