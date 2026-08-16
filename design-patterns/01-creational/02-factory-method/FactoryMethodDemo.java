/**
 * FACTORY METHOD PATTERN
 * ----------------------
 * Define an interface for creating an object, but let a factory decide WHICH
 * concrete class to instantiate — so callers depend on the abstraction, not `new`.
 * e.g. notify("EMAIL") -> EmailNotifier, notify("SMS") -> SmsNotifier.
 *
 * The pain (without it):
 *   Callers write `new EmailNotifier()` / `new SmsNotifier()` and sprinkle
 *   `if (type == ...)` everywhere. Adding a channel means editing every caller.
 *
 * The fix:
 *   A single factory method owns the `switch`. Callers just ask for a Notifier.
 *
 * TIP TO REMEMBER:
 *   "Ask the factory, don't call `new`." One place to change when a type is added.
 */

public class FactoryMethodDemo {

    // The product abstraction every caller depends on
    interface Notifier { void send(String msg); }

    static class EmailNotifier implements Notifier {
        public void send(String msg) { System.out.println("EMAIL: " + msg); }
    }
    static class SmsNotifier implements Notifier {
        public void send(String msg) { System.out.println("SMS:   " + msg); }
    }
    static class PushNotifier implements Notifier {
        public void send(String msg) { System.out.println("PUSH:  " + msg); }
    }

    // ---------- THE FIX: the factory method owns object creation ----------
    // Add a new channel? Touch ONLY this method — no caller changes.
    static Notifier create(String channel) {
        return switch (channel.toUpperCase()) {
            case "EMAIL" -> new EmailNotifier();
            case "SMS"   -> new SmsNotifier();
            case "PUSH"  -> new PushNotifier();
            default -> throw new IllegalArgumentException("Unknown channel: " + channel);
        };
    }

    public static void main(String[] args) {
        // Caller depends on Notifier + the factory, NOT on concrete classes.
        for (String channel : new String[]{"EMAIL", "SMS", "PUSH"}) {
            Notifier n = create(channel);
            n.send("your order shipped");
        }
        System.out.println("\nExpected -> EMAIL / SMS / PUSH each send the same message");
    }
}