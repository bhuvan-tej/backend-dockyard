/**
 * BEST TIME TO BUY AND SELL STOCK  (Q4)
 * -------------------------------------
 * Given daily prices, maximize profit from ONE buy followed by ONE sell.
 * You must buy before you sell. e.g. [7,1,5,3,6,4] -> 5 (buy at 1, sell at 6).
 *
 * APPROACH:
 *   1) Brute force  -> try every (buy, sell) pair.  O(n^2) time, O(1) space.
 *   2) Optimized    -> track the cheapest day seen so far; at each price,
 *                      profit = price - minSoFar.     O(n) time, O(1) space.
 *
 * HINT TO REMEMBER: "Cheapest day so far." Walk once, remember the minimum
 *   price behind you, and see how much you'd make selling today.
 */
public class BuyAndSellStock {

    static int bruteForce(int[] prices) {
        int best = 0;
        for (int buy = 0; buy < prices.length; buy++)
            for (int sell = buy + 1; sell < prices.length; sell++)
                best = Math.max(best, prices[sell] - prices[buy]);
        return best;
    }

    static int optimized(int[] prices) {
        int minSoFar = Integer.MAX_VALUE, best = 0;
        for (int price : prices) {
            minSoFar = Math.min(minSoFar, price);   // cheapest buy day so far
            best = Math.max(best, price - minSoFar); // best profit if we sell today
        }
        return best;
    }

    public static void main(String[] args) {
        int[] prices = {7, 1, 5, 3, 6, 4};
        System.out.println("Prices: [7, 1, 5, 3, 6, 4]");
        System.out.println("Brute force -> " + bruteForce(prices));
        System.out.println("Optimized   -> " + optimized(prices));
        System.out.println("Expected    -> 5  (buy 1, sell 6)");
    }

}