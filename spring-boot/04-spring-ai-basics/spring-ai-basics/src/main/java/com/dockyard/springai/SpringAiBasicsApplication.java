package com.dockyard.springai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring AI Basics — the smallest honest introduction to Spring AI.
 *
 * <p>There is no database, no vector store and no RAG here on purpose. The single
 * goal is to show how you talk to a Large Language Model the "Spring way": you
 * inject one {@code ChatClient} and call it. Five controller endpoints show the
 * five things you'll do 90% of the time:
 *
 * <ol>
 *   <li><b>ask</b>        — send a message, get an answer</li>
 *   <li><b>persona</b>    — steer the model with a system prompt</li>
 *   <li><b>template</b>   — fill variables into a reusable prompt</li>
 *   <li><b>structured</b> — get a typed Java object back instead of a String</li>
 *   <li><b>stream</b>     — receive the answer token-by-token</li>
 * </ol>
 *
 * <p>Just like every other project in this repo, the third-party engine (Spring
 * AI) is isolated behind our own {@code AssistantEngine} — controllers never
 * import {@code org.springframework.ai.*}.
 */
@SpringBootApplication
public class SpringAiBasicsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiBasicsApplication.class, args);
    }
}

