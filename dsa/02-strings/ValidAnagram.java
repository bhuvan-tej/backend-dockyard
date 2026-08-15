/**
 * VALID ANAGRAM
 * -------------
 * Return true if t is an anagram of s (same characters, same counts).
 *
 * Approaches:
 *   1) Brute force -> sort both strings and compare: O(n log n) time
 *   2) Optimized   -> single frequency array int[26]: O(n) time, O(1) space
 *
 * TIP TO REMEMBER:
 *   Order doesn't matter, only HOW MANY of each letter. Count up for s,
 *   count down for t; everything must cancel to zero.
 */

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ValidAnagram {

    // ---------- 1) BRUTE FORCE: O(n log n) time ----------
    static boolean bruteForce(String s, String t) {
        if (s.length() != t.length()) return false;
        char[] a = s.toCharArray();
        char[] b = t.toCharArray();
        Arrays.sort(a);
        Arrays.sort(b);
        return Arrays.equals(a, b);
    }

    // ---------- 2) OPTIMIZED: O(n) time, O(1) space (26 is constant) ----------
    static boolean optimized(String s, String t) {
        if (s.length() != t.length()) return false;
        int[] count = new int[26];
        for (char c : s.toCharArray()) count[c - 'a']++;
        for (char c : t.toCharArray()) count[c - 'a']--;
        for (int x : count) if (x != 0) return false;
        return true;
    }

    //unicode version
    static boolean isAnagram(String s, String t) {
        if (s.length() != t.length()) {
            return false;
        }

        Map<Character, Integer> count = new HashMap<>();

        for (char c : s.toCharArray()) {
            count.put(c, count.getOrDefault(c, 0) + 1);
        }

        for (char c : t.toCharArray()) {
            if (!count.containsKey(c) || count.get(c) == 0) {
                return false;
            }
            count.put(c, count.get(c) - 1);
        }

        return true;
    }

    public static void main(String[] args) {
        String s = "anagram", t = "nagaram";
        String s2 = "rat", t2 = "car";

        System.out.println("Inputs       -> \"" + s + "\", \"" + t + "\"");
        System.out.println("Brute force  -> " + bruteForce(s, t));
        System.out.println("Optimized    -> " + optimized(s, t));
        System.out.println("Expected     -> true");
        System.out.println("Unicode      -> " + isAnagram(s, t));
        System.out.println();
        System.out.println("Inputs       -> \"" + s2 + "\", \"" + t2 + "\"");
        System.out.println("Optimized    -> " + optimized(s2, t2));
        System.out.println("Expected     -> false");
        System.out.println("Unicode      -> " + isAnagram(s2, t2));
    }

}