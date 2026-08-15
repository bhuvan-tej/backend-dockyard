/**
 * VALID PALINDROME
 * ----------------
 * Return true if the string is a palindrome, ignoring case and
 * non-alphanumeric characters. e.g. "A man, a plan, a canal: Panama" -> true
 *
 * Approaches:
 *   1) Brute force -> clean + reverse + compare: O(n) time, O(n) space
 *   2) Optimized   -> two pointers from both ends:  O(n) time, O(1) space
 *
 * TIP TO REMEMBER:
 *   Compare from both ends inward, skipping junk. No cleaned copy needed.
 */

public class ValidPalindrome {

    // ---------- 1) BRUTE FORCE: O(n) time, O(n) space ----------
    static boolean bruteForce(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (Character.isLetterOrDigit(c)) sb.append(Character.toLowerCase(c));
        }
        String cleaned = sb.toString();
        return cleaned.equals(sb.reverse().toString());
    }

    // ---------- 2) OPTIMIZED: O(n) time, O(1) space ----------
    static boolean optimized(String s) {
        int i = 0, j = s.length() - 1;
        while (i < j) {
            while (i < j && !Character.isLetterOrDigit(s.charAt(i))) i++;
            while (i < j && !Character.isLetterOrDigit(s.charAt(j))) j--;
            if (Character.toLowerCase(s.charAt(i)) != Character.toLowerCase(s.charAt(j))) {
                return false;
            }
            i++;
            j--;
        }
        return true;
    }

    public static void main(String[] args) {
        String a = "A man, a plan, a canal: Panama";
        String b = "race a car";

        System.out.println("Input 1      -> \"" + a + "\"");
        System.out.println("Brute force  -> " + bruteForce(a));
        System.out.println("Optimized    -> " + optimized(a));
        System.out.println("Expected     -> true");
        System.out.println();
        System.out.println("Input 2      -> \"" + b + "\"");
        System.out.println("Optimized    -> " + optimized(b));
        System.out.println("Expected     -> false");
    }

}