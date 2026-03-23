/**
 *
 * A simple Java HTTP server that:
 *  - Connects to PostgreSQL and creates a visits table
 *  - Increments a visit counter on every request
 *  - Returns a JSON response with the visit count
 *
 * This is intentionally simple — no Spring Boot yet.
 * The focus is on Docker Compose wiring, not the application code.
 *
 * You will see how:
 *  - The app connects to postgres using the container NAME as hostname
 *  - Environment variables from docker-compose.yml are read here
 *  - The app waits for postgres to be ready before starting
 */

import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.sql.*;

public class Main {

    // Read database connection details from environment variables
    // These are set in docker-compose.yml under the app service environment section
    // Using environment variables means we never hardcode credentials in code
    private static final String DB_URL  = System.getenv("DB_URL");
    private static final String DB_USER = System.getenv("DB_USER");
    private static final String DB_PASS = System.getenv("DB_PASS");

    public static void main(String[] args) throws Exception {

        System.out.println("Starting app...");
        System.out.println("Connecting to database at: " + DB_URL);

        // Wait for PostgreSQL to be ready
        // PostgreSQL takes a few seconds to start up inside the container
        // Without this wait the app would crash trying to connect too early
        waitForDatabase();

        // Set up the visits table in PostgreSQL
        setupDatabase();

        // Start a simple HTTP server on port 8080
        // This is what docker-compose maps to your Windows port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Handle all requests to the root path /
        server.createContext("/", exchange -> {

            // Every time someone hits the URL increment the visit count
            int visits = incrementAndGetVisits();

            // Build a simple JSON response
            String response = "{\"message\": \"Hello from Docker Compose!\", \"visits\": " + visits + "}";

            // Send the response back
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });

        server.start();
        System.out.println("App running on port 8080");
        System.out.println("Open http://localhost:8080 in your browser");
    }

    // Try to connect to the database every 2 seconds until it succeeds
    // This handles the case where the app container starts before postgres is ready
    private static void waitForDatabase() throws InterruptedException {
        System.out.println("Waiting for database to be ready...");
        int attempts = 0;
        while (attempts < 30) {
            try {
                Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                conn.close();
                System.out.println("Database is ready!");
                return;
            } catch (SQLException e) {
                attempts++;
                System.out.println("Database not ready yet, attempt " + attempts + "/30, retrying in 2 seconds...");
                Thread.sleep(2000);
            }
        }
        throw new RuntimeException("Could not connect to database after 30 attempts");
    }

    // Create the visits table if it does not already exist
    private static void setupDatabase() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS visits (id SERIAL PRIMARY KEY, count INTEGER DEFAULT 0)");
            stmt.execute("INSERT INTO visits (count) SELECT 0 WHERE NOT EXISTS (SELECT 1 FROM visits)");
            System.out.println("Database setup complete");
        }
    }

    // Increment the visit counter in the database and return the new value
    private static int incrementAndGetVisits() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement()) {
            stmt.execute("UPDATE visits SET count = count + 1");
            ResultSet rs = stmt.executeQuery("SELECT count FROM visits");
            rs.next();
            return rs.getInt("count");
        }
    }
}