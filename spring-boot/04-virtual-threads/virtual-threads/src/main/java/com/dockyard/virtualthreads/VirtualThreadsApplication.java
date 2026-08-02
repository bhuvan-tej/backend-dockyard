package com.dockyard.virtualthreads;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * VirtualThreadsApplication — entry point for the virtual-threads demo.
 *
 * WHAT THIS APP DEMONSTRATES:
 *   The DIFFERENCE between a platform thread (a real, expensive OS thread —
 *   typically a few thousand of these is the practical ceiling for a JVM) and
 *   a virtual thread (a cheap, JVM-managed thread — millions can exist at once,
 *   because blocking operations like {@code Thread.sleep} or blocking I/O
 *   automatically UNMOUNT the virtual thread from its carrier platform thread,
 *   freeing that carrier to run other virtual threads in the meantime).
 *   The result: code that LOOKS exactly like normal blocking, single-threaded
 *   Java (no reactive types, no callbacks) but SCALES like async code under
 *   high-concurrency, I/O-bound workloads (many slow downstream calls, DB
 *   queries, etc. all waiting at once).
 *
 * See {@code ThreadingConfig} for the two executor beans this app compares,
 * and {@code application.yml}'s {@code spring.threads.virtual.enabled} flag
 * for the one-line way to switch the WHOLE embedded Tomcat server over to
 * virtual threads for every incoming HTTP request.
 */
@SpringBootApplication
public class VirtualThreadsApplication {

    public static void main(String[] args) {
        SpringApplication.run(VirtualThreadsApplication.class, args);
    }

}