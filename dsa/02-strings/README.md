# 02 — Strings 🔤

> **A string is just an array of characters.** Almost everything you learned in Arrays (two pointers, hashing, sliding window) applies here — plus a few string-only tricks (frequency counts, expand-around-center, stacks for matching).

---

## 1. What Is a String?

An ordered, **immutable** sequence of characters. In Java a `String` cannot be changed once created — every "edit" makes a **new** String.

```
Index:    0    1    2    3    4
        +----+----+----+----+----+
Char:   | h  | e  | l  | l  | o  |
        +----+----+----+----+----+
        "hello".charAt(2) == 'l'
```

> 🧠 **The single most important Java fact:** `String` is **immutable**. Doing `s += c` inside a loop secretly builds a brand-new string every time → **O(n²)**. Use `StringBuilder` to edit in a loop → **O(n)**.

---

## 2. The Complexity Profile (know this cold)

| Operation                      | Time               | Why                                   |
|--------------------------------|--------------------|---------------------------------------|
| `s.charAt(i)`                  | **O(1)**           | Direct index into a char array        |
| `s.length()`                   | **O(1)**           | Stored, not counted                   |
| `s + t` (concat)               | **O(n + m)**       | Copies both into a new string         |
| `s += c` in a loop             | **O(n²)** ⚠️       | New string *every* iteration — avoid! |
| `sb.append(c)` (StringBuilder) | **O(1)** amortized | Mutable buffer, no copy               |
| `s.substring(a, b)`            | **O(b − a)**       | Copies the range (Java 7+)            |
| `s.equals(t)`                  | **O(n)**           | Compares char by char                 |
| Compare / search (unsorted)    | **O(n·m)**         | Naive substring search                |

**Space:** the string itself is `O(n)`. Most string *algorithms* aim for `O(1)` extra (two pointers) or `O(k)` where `k` = alphabet size (a 26- or 128-slot frequency array — effectively O(1)).

> 🧠 **Tip to remember:** *"Reading a string is cheap, rebuilding it is expensive."* Read with `charAt` (O(1)); build with `StringBuilder`, never `+=` in a loop.

---

## 3. Java String Survival Kit

```
String s = "hello";
s.length();               // 5
s.charAt(1);              // 'e'
s.substring(1, 3);        // "el"  (start inclusive, end exclusive)
s.toCharArray();          // ['h','e','l','l','o']  → mutable!
s.equals("hello");        // true  (NEVER use == for content)
Character.isLetterOrDigit(c);
Character.toLowerCase(c);
c - 'a';                  // 0..25 index for a lowercase letter

// Building strings the RIGHT way:
StringBuilder sb = new StringBuilder();
sb.append('x');
sb.reverse();
String out = sb.toString();
```

> 🧠 **Two traps:**
> 1. `==` compares *references*, not content. Always use `.equals()`.
> 2. To mutate characters, convert to `char[]` with `toCharArray()` or use `StringBuilder`.

---

## 4. The 5 String Patterns That Solve Most Problems

Learn the *pattern name* — that's your recall hook.

### 🔹 Pattern 1: Two Pointers (ends → middle)
Reverse, palindrome checks, comparing from both sides.
> Hook: *"Symmetry / reverse / palindrome" → one pointer at each end, walk inward.*

### 🔹 Pattern 2: Frequency Count (int[26] or int[128] or HashMap)
Anagrams, counting, "same characters?" questions.
> Hook: *"Are these made of the same letters?" → count and compare.*

### 🔹 Pattern 3: Sliding Window
"Longest / shortest substring with property X." Grow the window from the right, shrink from the left when a rule breaks.
> Hook: *"Longest/shortest substring where…" → sliding window.*

### 🔹 Pattern 4: Expand Around Center
Palindromic substrings — treat every index (and every gap) as a possible center and expand outward.
> Hook: *"Palindromic substring" → pick a center, mirror outward.*

### 🔹 Pattern 5: Stack for Matching
Parentheses/brackets, "undo," nested structure.
> Hook: *"Matching pairs / nesting" → push opens, pop on a close.*

---

## 5. Worked Examples: Brute Force → Optimized

Each is implemented and runnable in the `.java` files here.

### Example A — Valid Anagram (`ValidAnagram.java`)
> *Do two strings contain the exact same characters?*

**Brute force** — sort both and compare: `O(n log n)`.

**Optimized — frequency array** — one `int[26]`, `++` for the first string, `--` for the second; all zeros → anagram:
```
int[] count = new int[26];
for (char c : s.toCharArray()) count[c - 'a']++;
for (char c : t.toCharArray()) count[c - 'a']--;
for (int x : count) if (x != 0) return false;
// Time O(n), Space O(1)  (26 is constant)
```
> 🧠 **The insight:** you don't need order, just *how many of each letter*. Counting up then down must cancel to zero.

---

### Example B — Longest Substring Without Repeating Characters (`LongestUniqueSubstring.java`)
> *Length of the longest substring with all distinct characters.*

**Brute force** — check every substring for uniqueness: `O(n²)` (or worse).

**Optimized — Sliding Window + HashSet/last-seen map** — `O(n)`:
```
int left = 0, best = 0;
Map<Character,Integer> last = new HashMap<>();
for (int right = 0; right < n; right++) {
    char c = s.charAt(right);
    if (last.containsKey(c) && last.get(c) >= left)
        left = last.get(c) + 1;          // jump left past the duplicate
    last.put(c, right);
    best = Math.max(best, right - left + 1);
}
// Time O(n), Space O(min(n, alphabet))
```
> 🧠 **The insight:** never re-scan. When a repeat appears, *slide the left edge* just past the previous copy. The window always holds a valid (unique) substring.

---

### Example C — Valid Palindrome (`ValidPalindrome.java`)
> *Is it a palindrome, ignoring case and non-alphanumerics?*

**Brute force** — clean the string, reverse it, compare: `O(n)` time but **O(n) space**.

**Optimized — Two Pointers** — `O(1)` space:
```
int i = 0, j = s.length() - 1;
while (i < j) {
    while (i < j && !Character.isLetterOrDigit(s.charAt(i))) i++;
    while (i < j && !Character.isLetterOrDigit(s.charAt(j))) j--;
    if (Character.toLowerCase(s.charAt(i)) != Character.toLowerCase(s.charAt(j)))
        return false;
    i++; j--;
}
```
> 🧠 **The insight:** compare from both ends inward, skipping junk — no need to build a cleaned copy.

---

## 6. ⚠️ Common Bugs & Gotchas

- **`==` vs `.equals()`** — `==` checks reference identity, not text. Use `.equals()`.
- **`s += c` in a loop** — silently O(n²). Use `StringBuilder`.
- **`substring` bounds** — end index is **exclusive**: `"hello".substring(1,3)` = `"el"`.
- **Char arithmetic** — `c - 'a'` gives 0–25 *only for lowercase*; normalize case first.
- **Unicode** — `int[26]` assumes lowercase a–z; use `int[128]` for ASCII or a HashMap for full Unicode.
- **Empty / single char** — palindromes and windows must handle `n == 0` and `n == 1`.

---

## 7. ✅ Self-Check Before Moving On

1. Why is `s += c` in a loop O(n²), and what fixes it?
2. When do you reach for a frequency array vs a HashMap?
3. Explain the sliding-window "slide left past the duplicate" idea.
4. Why does expand-around-center need to handle *two* kinds of centers?
5. Why must you always use `.equals()` for string content?