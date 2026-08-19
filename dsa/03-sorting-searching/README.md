# 03 — Sorting & Searching 🔍

> **Ordering is an optimization.** Most "clever" array tricks are really just *"sort it first, then the answer becomes obvious"* or *"the data is sorted, so throw half of it away each step."*

---

## 1. Why This Topic Comes Right After Arrays & Strings

You already met brute force `O(n²)` loops. Sorting is the first tool that systematically kills them:

| Problem shape                 | Unsorted    | Sorted                                   |
|-------------------------------|-------------|------------------------------------------|
| "Does value X exist?"         | O(n) scan   | **O(log n)** binary search               |
| "Are there duplicates?"       | O(n²) pairs | **O(n log n)** sort + scan neighbours    |
| "Find the k-th smallest"      | O(n²)       | **O(n log n)** sort, or O(n) quickselect |
| "Merge overlapping intervals" | hard        | **easy** once sorted by start            |

The cost is `O(n log n)` once — and then everything downstream gets cheaper.

---

## 2. The Sorting Complexity Profile (memorize this table)

| Algorithm      | Best       | Average    | Worst      | Space    | Stable? |
|----------------|------------|------------|------------|----------|---------|
| Bubble Sort    | O(n)       | O(n²)      | O(n²)      | O(1)     | ✅ Yes  |
| Selection Sort | O(n²)      | O(n²)      | O(n²)      | O(1)     | ❌ No   |
| Insertion Sort | O(n)       | O(n²)      | O(n²)      | O(1)     | ✅ Yes  |
| **Merge Sort** | O(n log n) | O(n log n) | O(n log n) | **O(n)** | ✅ Yes  |
| **Quick Sort** | O(n log n) | O(n log n) | **O(n²)**  | O(log n) | ❌ No   |
| Heap Sort      | O(n log n) | O(n log n) | O(n log n) | O(1)     | ❌ No   |
| Counting Sort  | O(n + k)   | O(n + k)   | O(n + k)   | O(k)     | ✅ Yes  |

**Stable** = equal elements keep their original relative order. It matters when you sort by
one key after already sorting by another.

### What Java actually uses
- `Arrays.sort(int[])` → **dual-pivot quicksort** (fast, in-place, *not* stable — fine for primitives, since equal ints are indistinguishable).
- `Arrays.sort(Object[])` / `Collections.sort(List)` → **TimSort** (merge sort + insertion sort hybrid, **stable**, O(n) on already-sorted data).

> ⚠️ Interview trap: "What's the worst case of `Arrays.sort` on an `int[]`?" → **O(n²)**, because it's quicksort. On `Integer[]` it's O(n log n), because that's TimSort.

---

## 3. Binary Search — The One Template To Rule Them All

The classic form, on a **sorted** array:

```
int lo = 0, hi = nums.length - 1;
while (lo <= hi) {
    int mid = lo + (hi - lo) / 2;   // NOT (lo + hi) / 2  — see gotchas
    if (nums[mid] == target) return mid;
    else if (nums[mid] < target) lo = mid + 1;   // answer is right
    else hi = mid - 1;                           // answer is left
}
return -1;
```

Every binary search answers one question: **"can I safely discard half?"**
If yes, you have a binary search — even if there's no array in sight (see Pattern 3).

---

## 4. The 4 Patterns That Solve Most Sort/Search Problems

### 🔹 Pattern 1: Sort, then the problem collapses
Sort first and the structure reveals itself. Used by Merge Intervals, Three Sum, Group Anagrams,
"find duplicates", "closest pair". Cost: `O(n log n)` — usually worth it.

### 🔹 Pattern 2: Boundary binary search (lower / upper bound)
Instead of "find the target", ask **"find the FIRST index where a condition becomes true."**
This finds insert positions and first/last occurrences. The trick: on a match, don't return —
record it and keep shrinking toward the side you want.

### 🔹 Pattern 3: Binary search on the ANSWER 🌟
The array isn't what you search — you search the *range of possible answers*.
Works whenever the answer space is **monotonic**: if speed `k` works, every speed `> k` also works.

```
Is 5 fast enough? no  → search higher
Is 8 fast enough? yes → maybe lower works, search lower
```
This is the single highest-value pattern in this topic (see `KokoEatingBananas.java`).

### 🔹 Pattern 4: Partition / Dutch National Flag
When there are only a few distinct values, you don't need a comparison sort at all —
sweep pointers and partition in **O(n)** (see `SortColors.java`). Quickselect uses the same
partition idea to find the k-th element in **O(n)** average.

---

## 5. Choosing Your Weapon

```
Is the array sorted (or can I sort it)?
├── Need a specific value?          → binary search           O(log n)
├── Need the k-th largest/smallest? → quickselect O(n) avg, or heap O(n log k)
├── Only 2–3 distinct values?       → partition pointers      O(n)
└── Answer is a NUMBER in a range?  → binary search on answer O(n log range)
```

---

## 6. ⚠️ Common Bugs & Gotchas

- **Overflow:** `(lo + hi) / 2` overflows when both are near `Integer.MAX_VALUE`.
  Always write `lo + (hi - lo) / 2`.
- **Infinite loops:** if you write `lo = mid` (instead of `mid + 1`), `lo` can stop moving.
  Every branch must *shrink* the range.
- **`<=` vs `<`:** `while (lo <= hi)` with `hi = length - 1` searches a closed range.
  `while (lo < hi)` with `hi = length` searches a half-open range. Pick one style and stay in it.
- **Binary search on unsorted data** silently returns garbage — it does not error. Verify sortedness.
- **Comparator contract:** `(a, b) -> a - b` overflows on large/negative ints. Use `Integer.compare(a, b)`.
- **Sorting a `long[]` by a comparator** isn't possible — no boxed comparator overload for primitives.

---

## 7. ✅ Self-Check Before Moving On

You should be able to answer these without looking:

1. Why is `mid = lo + (hi - lo) / 2` safer than `(lo + hi) / 2`?
2. Which Java sort is stable, and why does `Arrays.sort(int[])` not need to be?
3. What's the worst case of quicksort, and what causes it?
4. How do you find the **first** occurrence of a duplicate value with binary search?
5. How do you recognize a "binary search on the answer" problem?
6. Why is quickselect O(n) average but O(n²) worst case?