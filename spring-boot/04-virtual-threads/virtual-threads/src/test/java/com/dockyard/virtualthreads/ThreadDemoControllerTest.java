package com.dockyard.virtualthreads;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ThreadDemoControllerTest — hits the real embedded Tomcat over HTTP
 * (webEnvironment = RANDOM_PORT), NOT MockMvc.
 *
 * WHY THIS MATTERS: MockMvc dispatches straight into the DispatcherServlet on
 * the CALLING thread — it never goes through Tomcat's actual connector/thread
 * pool, so it can NEVER prove {@code spring.threads.virtual.enabled} is doing
 * anything. A real HTTP round-trip through the embedded server is the only
 * way {@code /demo/request-thread} can honestly report whether the request
 * was served by a virtual thread.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ThreadDemoControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String url(String path) {
        return "http://localhost:" + port + "/api" + path;
    }

    @Test
    void runEndpointReturnsSummary() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                url("/demo/run?executor=VIRTUAL&tasks=10&delayMs=10"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"executor\":\"VIRTUAL\"", "\"taskCount\":10");
    }

    @Test
    void compareEndpointReturnsBothRuns() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                url("/demo/compare?tasks=10&delayMs=10"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("virtualRun", "platformRun", "speedupFactor");
    }

    @Test
    void requestThreadEndpointReportsVirtualThread() {
        // spring.threads.virtual.enabled=true means Tomcat itself hands this
        // request to a virtual thread — only observable through a REAL request.
        ResponseEntity<String> response = restTemplate.getForEntity(url("/demo/request-thread"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"virtualThread\":true");
    }

    @Test
    void outOfRangeTaskCountIsRejected() {
        ResponseEntity<String> response = restTemplate.getForEntity(url("/demo/run?tasks=999999"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}



