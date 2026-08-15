/**
 * REVERSE STRING
 * --------------
 * Reverse a string in place (given as a char[]).
 *
 * Approaches:
 *   1) Brute force -> build a new string backwards: O(n) time, O(n) space
 *   2) Optimized   -> two pointers swapping in place: O(n) time, O(1) space
 *
 * TIP TO REMEMBER:
 *   "Symmetry / reverse" -> one pointer at each end, swap and walk inward.
 */

public class ReverseString {

    // ---------- 1) BRUTE FORCE: O(n) time, O(n) space ----------
    static String bruteForce(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = s.length() - 1; i >= 0; i--) {
            sb.append(s.charAt(i));
        }
        return sb.toString();
    }

    // ---------- 2) OPTIMIZED: O(n) time, O(1) space (in place) ----------
    static void optimized(char[] s) {
        int i = 0, j = s.length - 1;
        while (i < j) {
            char tmp = s[i];
            s[i] = s[j];
            s[j] = tmp;
            i++;
            j--;
        }
    }

    public static void main(String[] args) {
        String input = "hello";
        System.out.println("Input        -> \"" + input + "\"");
        System.out.println("Brute force  -> \"" + bruteForce(input) + "\"");

        char[] chars = input.toCharArray();
        optimized(chars);
        System.out.println("Optimized    -> \"" + new String(chars) + "\"");
        System.out.println("Expected     -> \"olleh\"");
    }

}