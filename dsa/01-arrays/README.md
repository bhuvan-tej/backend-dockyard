# 01 — Arrays 📦

> **The most fundamental data structure.** Master arrays and you've unlocked the patterns behind strings, hashing, two-pointers, and sliding windows.

---

## 1. What Is an Array?

A contiguous block of memory holding elements of the same type, accessed by a numeric **index** starting at `0`.

```
Index:    0    1    2    3    4
        +----+----+----+----+----+
Value:  | 10 | 20 | 30 | 40 | 50 |
        +----+----+----+----+----+
```

Because the memory is contiguous, the computer can **jump directly** to any index using simple math (`base_address + index × element_size`). That's why array access is **O(1)** — instant, regardless of size.

---

## 2. The Complexity Profile (memorize this table)

| Operation                   | Time         | Why                                      |
|-----------------------------|--------------|------------------------------------------|
| Access by index `arr[i]`    | **O(1)**     | Direct memory jump                       |
| Update `arr[i] = x`         | **O(1)**     | Direct memory jump                       |
| Search (unsorted)           | **O(n)**     | Might check every element                |
| Search (sorted)             | **O(log n)** | Binary search                            |
| Insert/Delete at **end**    | **O(1)***    | No shifting needed (*if capacity exists) |
| Insert/Delete in **middle** | **O(n)**     | Must shift all following elements        |

> 🧠 **Tip to remember:** Arrays are **read-champions, edit-strugglers**. Reading any spot is instant; inserting/deleting in the middle forces everyone to scoot over → O(n).

**Space:** An array of n elements is `O(n)`. Most array *algorithms* aim for `O(1)` extra space (modify in place) or `O(n)` (use a helper HashMap/array).

---

## 3. Fixed Array vs ArrayList in Java

|            | `int[]` (array)       | `ArrayList<Integer>`           |
|------------|-----------------------|--------------------------------|
| Size       | Fixed at creation     | Grows dynamically              |
| Type       | Primitives or objects | Objects only (autoboxing)      |
| Access     | `arr[i]`              | `list.get(i)`                  |
| Add at end | ❌ (fixed)            | `list.add(x)` — O(1) amortized |

```java
int[] arr = new int[5];          // fixed size 5, defaults to 0
int[] nums = {10, 20, 30};       // literal
List<Integer> list = new ArrayList<>(); // dynamic
```

> 🧠 **Tip:** Use `int[]` when the size is known and you want speed/no boxing. Use `ArrayList` when the size changes.

---

## 4. The 4 Array Patterns That Solve 80% of Problems

These are your reusable mental tools. Learn the *pattern name* — it's the hook that lets you recall the solution instead of memorizing it.

### 🔹 Pattern 1: Brute Force with Nested Loops → then eliminate a loop
Almost every array problem *can* be solved by checking all pairs (`O(n²)`). The **optimization** is usually "can I avoid the inner loop with a HashMap or a pointer?"

### 🔹 Pattern 2: Two Pointers
Use two indices moving toward each other (or in the same direction). Great for **sorted arrays**, pairs, reversing, partitioning.
> Hook: *"Sorted array + find a pair" → start pointers at both ends and squeeze inward.*

### 🔹 Pattern 3: Hashing (HashMap/HashSet)
Trade `O(n)` space to remember what you've seen → turns `O(n²)` lookups into `O(n)`.
> Hook: *"Have I seen this before?" → HashSet. "What did it map to?" → HashMap.*

### 🔹 Pattern 4: Prefix Sum
Precompute running totals so any range-sum query becomes `O(1)`.
> Hook: *"Sum of a subarray asked many times" → prefix sums.*

---

## 5. Worked Examples: Brute Force → Optimized

Each of these is implemented and runnable in the `.java` files here.

### Example A — Two Sum (`TwoSum.java`)
> *Find two indices whose values add up to a target.*

**Brute force** — check every pair:
```
for (int i = 0; i < n; i++)
    for (int j = i + 1; j < n; j++)
        if (nums[i] + nums[j] == target) return new int[]{i, j};
// Time O(n²), Space O(1)
```

**Optimized** — a HashMap remembers what we need:
```
Map<Integer, Integer> seen = new HashMap<>();
for (int i = 0; i < n; i++) {
    int need = target - nums[i];
    if (seen.containsKey(need)) return new int[]{seen.get(need), i};
    seen.put(nums[i], i);
}
// Time O(n), Space O(n)
```
> 🧠 **The insight:** instead of asking *"is there a partner ahead of me?"* (inner loop), we ask *"did my partner already pass by?"* (HashMap). We buy `O(n)` space to delete an entire loop.

---

### Example B — Maximum Subarray Sum (`MaxSubarraySum.java`)
> *Find the largest sum of any contiguous subarray.*

**Brute force** — try every subarray:
```
for (int i = 0; i < n; i++)
    for (int j = i; j < n; j++)
        // sum nums[i..j], track max
// Time O(n²) (or O(n³) if you re-sum each time), Space O(1)
```

**Optimized — Kadane's Algorithm** — one pass:
```
int best = nums[0], current = nums[0];
for (int i = 1; i < n; i++) {
    current = Math.max(nums[i], current + nums[i]); // extend or restart
    best = Math.max(best, current);
}
// Time O(n), Space O(1)
```
> 🧠 **The insight (Kadane):** at each element decide — *"is it better to extend the previous subarray, or start fresh here?"* If the running sum ever goes negative, it can only hurt future sums, so drop it.

---

### Example C — Move Zeroes (`MoveZeroes.java`)
> *Push all zeros to the end, keep the order of non-zeros. In place.*

**Brute force** — build a new array, then copy back: `O(n)` time, **`O(n)` space**.

**Optimized — Two Pointers** — `O(1)` space:
```
int insertPos = 0;
for (int i = 0; i < n; i++)
    if (nums[i] != 0) nums[insertPos++] = nums[i];
while (insertPos < n) nums[insertPos++] = 0;
```
> 🧠 **The insight:** one pointer scans, the other marks "where the next non-zero belongs." Classic **slow/fast two-pointer**.

---

## 6. ⚠️ Common Bugs & Gotchas

- **Off-by-one:** loop bounds `<` vs `<=`, and last index is `n-1`, not `n`.
- **ArrayIndexOutOfBoundsException:** always check `i < arr.length`.
- **Modifying while iterating:** don't remove from an `ArrayList` inside a for-each loop.
- **Integer overflow:** `left + right` can overflow; prefer `left + (right - left) / 2` for midpoints.
- **Empty array:** handle `n == 0` before touching `arr[0]`.

---

## 7. ✅ Self-Check Before Moving On

1. Why is array access O(1) but middle-insertion O(n)?
2. Name the 4 core array patterns and one problem each solves.
3. Explain Kadane's decision ("extend or restart") in your own words.
4. How does a HashMap turn Two Sum from O(n²) into O(n)?

➡️ **Next up:** Strings (an array of characters — you'll reuse everything here).

---

All important questions have a full runnable solution file here (brute force **and** optimized, with the approach + hints in the comments).