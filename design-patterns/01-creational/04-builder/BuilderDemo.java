/**
 * BUILDER PATTERN
 * ---------------
 * Construct a complex object step-by-step, then produce an immutable result.
 * e.g. build a HttpRequest with many optional fields without a 6-arg constructor.
 *
 * The pain (without it):
 *   Telescoping constructors — new Request(url, "GET", null, 30, true, null) —
 *   nobody can read what each arg means, and every optional combo needs an overload.
 *
 * The fix:
 *   A fluent builder names each field; build() returns a finished, immutable object.
 *
 * TIP TO REMEMBER:
 *   "Order at a counter, then serve." Chain the parts, call build() to finish.
 *   Lombok's @Builder generates exactly this for you.
 */

public class BuilderDemo {

    // ---------- THE RESULT: immutable, no setters ----------
    static final class HttpRequest {
        private final String url;      // required
        private final String method;   // optional (defaults)
        private final int timeoutSec;  // optional
        private final boolean followRedirects;

        private HttpRequest(Builder b) {
            this.url = b.url;
            this.method = b.method;
            this.timeoutSec = b.timeoutSec;
            this.followRedirects = b.followRedirects;
        }

        @Override public String toString() {
            return method + " " + url + " (timeout=" + timeoutSec + "s, redirects=" + followRedirects + ")";
        }

        // ---------- THE FIX: fluent builder with sensible defaults ----------
        static class Builder {
            private final String url;          // required -> constructor arg
            private String method = "GET";     // defaults
            private int timeoutSec = 30;
            private boolean followRedirects = true;

            Builder(String url) { this.url = url; }

            Builder method(String m)            { this.method = m; return this; }
            Builder timeoutSec(int t)           { this.timeoutSec = t; return this; }
            Builder followRedirects(boolean f)  { this.followRedirects = f; return this; }

            HttpRequest build() { return new HttpRequest(this); }
        }
    }

    public static void main(String[] args) {
        // Readable, only set what you need — the rest keep sane defaults.
        HttpRequest a = new HttpRequest.Builder("https://api.example.com/users").build();
        HttpRequest b = new HttpRequest.Builder("https://api.example.com/login")
                .method("POST")
                .timeoutSec(5)
                .followRedirects(false)
                .build();

        System.out.println(a);
        System.out.println(b);
        System.out.println("\nExpected -> GET with defaults; POST with timeout=5s, redirects=false");
    }
}