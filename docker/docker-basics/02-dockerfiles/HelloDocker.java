/**
 *
 * Simple Java program to learn Dockerfile basics.
 * Focus is on the Dockerfile, not this code.
 *
 * Things to notice when this runs inside Docker:
 *  - System.getenv() reads ENV variables set in Dockerfile or docker run -e
 *  - os.name prints "Linux" even though your host is Windows
 *    because containers run Linux via WSL2
 *  - The loop keeps the container alive so you can docker exec into it
 */
public class HelloDocker {

    public static void main(String[] args) throws InterruptedException {

        String name = System.getenv("MY_NAME");
        String env  = System.getenv("APP_ENV");

        if (name == null) name = "World";
        if (env  == null) env  = "not set";

        System.out.println("======================================");
        System.out.println("  Hello from inside Docker!");
        System.out.println("======================================");
        System.out.println("Name        : " + name);
        System.out.println("Environment : " + env);
        System.out.println("Java version: " + System.getProperty("java.version"));
        System.out.println("OS inside   : " + System.getProperty("os.name"));
        System.out.println();

        // Keeps the container running so you can explore inside with:
        // docker exec -it day2 sh
        int tick = 0;
        while (true) {
            System.out.printf("[tick %d] %s%n", ++tick, java.time.LocalTime.now());
            Thread.sleep(5_000);
        }
    }

}