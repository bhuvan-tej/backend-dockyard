/**
 * SINGLETON PATTERN
 * -----------------
 * Guarantee a class has exactly ONE instance and give a global point of access.
 * e.g. a single AppConfig loaded once and shared everywhere.
 *
 * The pain (without it):
 *   Every caller does `new AppConfig()` -> the file is re-read every time,
 *   and there's no single source of truth for shared state.
 *
 * The fix:
 *   Private constructor + a single cached instance the class hands out.
 *
 * TIP TO REMEMBER:
 *   "There can be only one." Private constructor = nobody else can `new` it.
 *   In Spring you rarely write this by hand — a @Bean is a singleton by default.
 */

public class SingletonDemo {

    // ---------- THE PAIN: a config that reloads on every `new` ----------
    static class NaiveConfig {
        NaiveConfig() {
            // imagine an expensive disk/network read happening here EVERY time
            System.out.println("  (NaiveConfig loaded from disk...)");
        }
        String get(String k) { return "value-of-" + k; }
    }

    // ---------- THE FIX: initialization-on-demand holder idiom ----------
    // Thread-safe & lazy WITHOUT synchronized blocks: the JVM only loads the
    // Holder class (and builds INSTANCE) the first time getInstance() is called.
    static final class AppConfig {
        private AppConfig() {
            System.out.println("  (AppConfig loaded ONCE from disk...)");
        }
        private static final AppConfig INSTANCE = new AppConfig();

        static AppConfig getInstance() {
            return INSTANCE;
        }
        String get(String k) { return "value-of-" + k; }
    }

    public static void main(String[] args) {
        System.out.println("Naive (reloads every time):");
        new NaiveConfig();
        new NaiveConfig();          // loaded AGAIN — wasteful

        System.out.println("\nSingleton (loads once):");
        AppConfig a = AppConfig.getInstance();
        AppConfig b = AppConfig.getInstance();   // NOT reloaded
        System.out.println("  same instance? -> " + (a == b));
    }

}