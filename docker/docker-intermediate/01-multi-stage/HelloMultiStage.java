/**
 *
 * Simple Java program used to demonstrate multi-stage Docker builds.
 * The focus is on comparing the two Dockerfiles, not this code.
 *
 * When this runs inside the multi-stage container notice:
 *  - os.name prints Linux even though you are on Windows
 *  - The image contains only this .class file, not the .java source
 *  - The JDK compiler is not present in the final image
 */
public class HelloMultiStage {

    public static void main(String[] args) {

        // Read the APP_NAME environment variable set in the Dockerfile
        // If it is not set fall back to a default value
        String appName = System.getenv("APP_NAME");
        if (appName == null) appName = "Unknown";

        System.out.println("========================================");
        System.out.println("  Multi-Stage Build Demo");
        System.out.println("========================================");
        System.out.println("App name    : " + appName);
        System.out.println("Java version: " + System.getProperty("java.version"));
        System.out.println("OS inside   : " + System.getProperty("os.name"));
        System.out.println();
        System.out.println("If APP_NAME says 'Multi Stage App' you ran the");
        System.out.println("multi-stage image. If it says 'Single Stage App'");
        System.out.println("you ran the single stage image.");
        System.out.println();
        System.out.println("Now compare the image sizes:");
        System.out.println("  docker images app");
    }

}