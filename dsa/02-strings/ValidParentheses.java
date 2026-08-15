/**
 * VALID PARENTHESES
 * -----------------
 * Given a string of '()[]{}', return true if brackets are correctly matched
 * and nested. e.g. "([]{})" -> true, "(]" -> false.
 *
 * Approach:
 *   Stack -> push every opening bracket; on a closing bracket, the top of the
 *   stack must be its matching opener. O(n) time, O(n) space.
 *
 * TIP TO REMEMBER:
 *   "Matching pairs / nesting" -> STACK. Last opened must be first closed
 *   (LIFO). Empty stack at the end means everything matched.
 */

import java.util.ArrayDeque;
import java.util.Deque;

public class ValidParentheses {

    static boolean isValid(String s) {
        Deque<Character> stack = new ArrayDeque<>();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '(' -> stack.push(')');
                case '[' -> stack.push(']');
                case '{' -> stack.push('}');
                default -> {
                    // c is a closing bracket: it must match the expected top
                    if (stack.isEmpty() || stack.pop() != c) return false;
                }
            }
        }
        return stack.isEmpty();
    }

    public static void main(String[] args) {
        String[] tests = {"()", "()[]{}", "(]", "([)]", "([]{})", "((("};
        boolean[] expected = {true, true, false, false, true, false};
        for (int i = 0; i < tests.length; i++) {
            System.out.printf("\"%-8s\" -> %-5b (expected %b)%n",
                    tests[i], isValid(tests[i]), expected[i]);
        }
    }
}